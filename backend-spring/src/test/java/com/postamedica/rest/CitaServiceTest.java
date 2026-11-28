
package com.postamedica.rest;

import com.postamedica.rest.application.events.NotificacionCitaEvent;
import com.postamedica.rest.application.services.CitaService;
import com.postamedica.rest.domain.dtos.CancelarCitaRequest;
import com.postamedica.rest.domain.dtos.CrearCitaRequest;
import com.postamedica.rest.domain.model.CitaMedica;
import com.postamedica.rest.domain.model.Especialista;
import com.postamedica.rest.domain.repository.CitaMedicaRepository;
import com.postamedica.rest.domain.repository.EspecialistaRepository;
import com.postamedica.rest.domain.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitaServiceTest {

    @Mock
    private CitaMedicaRepository citaMedicaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EspecialistaRepository especialistaRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private CitaService citaService;

    private CrearCitaRequest requestValido;
    private Especialista especialista;

    @BeforeEach
    void setUp() {
        citaService = new CitaService(
                citaMedicaRepository,
                usuarioRepository,
                especialistaRepository,
                eventPublisher
        );

        requestValido = new CrearCitaRequest();
        requestValido.setPacienteId(1L);
        requestValido.setEspecialistaId(10L);
        requestValido.setFecha(LocalDate.now().plusDays(5));
        requestValido.setHora(LocalTime.of(10, 0));

        especialista = new Especialista();
        especialista.setId(10L);
        especialista.setNombres("Carlos");
        especialista.setApellidos("Mendoza");
        especialista.setEspecialidad("Cardiología");
    }

    @Test
    @DisplayName("Agendar cita exitosamente con datos válidos")
    void agendar_exitoso() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(especialistaRepository.existsById(10L)).thenReturn(true);
        when(citaMedicaRepository.findByPacienteId(1L)).thenReturn(List.of());
        when(especialistaRepository.findById(10L)).thenReturn(Optional.of(especialista));
        when(citaMedicaRepository.existeConflicto(eq(10L), any(), any())).thenReturn(false);

        CitaMedica guardada = new CitaMedica();
        guardada.setId(100L);
        guardada.setEstado("POR_CONFIRMAR");
        guardada.setFecha(requestValido.getFecha());
        guardada.setHora(requestValido.getHora());
        guardada.setPacienteId(1L);
        guardada.setEspecialistaId(10L);

        when(citaMedicaRepository.save(any(CitaMedica.class))).thenReturn(guardada);

        CitaMedica resultado = citaService.agendar(requestValido);

        assertNotNull(resultado);
        assertEquals("POR_CONFIRMAR", resultado.getEstado());

        verify(citaMedicaRepository).save(any(CitaMedica.class));
        verify(eventPublisher).publishEvent(any(NotificacionCitaEvent.class));
    }

    @Test
    @DisplayName("Agendar falla si el paciente no existe")
    void agendar_pacienteNoExiste_lanzaExcepcion() {
        when(usuarioRepository.existsById(1L)).thenReturn(false);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> citaService.agendar(requestValido)
        );

        assertEquals("Paciente no encontrado", ex.getMessage());
        verify(citaMedicaRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Agendar falla si el especialista no existe")
    void agendar_especialistaNoExiste_lanzaExcepcion() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(especialistaRepository.existsById(10L)).thenReturn(false);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> citaService.agendar(requestValido)
        );

        assertEquals("Especialista no encontrado", ex.getMessage());
    }

    @Test
    @DisplayName("Agendar falla si la fecha es anterior a hoy")
    void agendar_fechaPasada_lanzaExcepcion() {
        requestValido.setFecha(LocalDate.now().minusDays(1));

        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(especialistaRepository.existsById(10L)).thenReturn(true);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> citaService.agendar(requestValido)
        );

        assertEquals(
                "No se pueden agendar citas en fechas pasadas",
                ex.getMessage()
        );
    }

    @Test
    @DisplayName("Agendar genera un código de cita con formato correcto")
    void agendar_generaCodigoUnico() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(especialistaRepository.existsById(10L)).thenReturn(true);
        when(citaMedicaRepository.findByPacienteId(1L)).thenReturn(List.of());
        when(especialistaRepository.findById(10L)).thenReturn(Optional.of(especialista));
        when(citaMedicaRepository.existeConflicto(eq(10L), any(), any())).thenReturn(false);
        when(citaMedicaRepository.save(any(CitaMedica.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CitaMedica resultado = citaService.agendar(requestValido);

        assertNotNull(resultado.getCodigoCita());
        assertTrue(resultado.getCodigoCita().startsWith("CM-"));
        assertTrue(resultado.getCodigoCita().endsWith("-10"));
    }

    @Test
    @DisplayName("Agendar falla si el especialista ya tiene una cita a esa hora")
    void agendar_horarioOcupado_lanzaExcepcion() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(especialistaRepository.existsById(10L)).thenReturn(true);
        when(citaMedicaRepository.findByPacienteId(1L)).thenReturn(List.of());
        when(especialistaRepository.findById(10L)).thenReturn(Optional.of(especialista));
        when(citaMedicaRepository.existeConflicto(eq(10L), any(), any())).thenReturn(true);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> citaService.agendar(requestValido)
        );

        assertEquals(
                "El especialista ya tiene una cita a esa hora",
                ex.getMessage()
        );

        verify(citaMedicaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Agendar falla si el paciente alcanzó el límite de 8 citas en el mes")
    void agendar_limiteMensualAlcanzado_lanzaExcepcion() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(especialistaRepository.existsById(10L)).thenReturn(true);

        List<CitaMedica> citasDelMes = new java.util.ArrayList<>();

        for (int i = 0; i < 8; i++) {
            CitaMedica c = new CitaMedica();
            c.setEstado("PROGRAMADA");
            c.setFecha(
                    requestValido.getFecha().withDayOfMonth(1).plusDays(i)
            );
            citasDelMes.add(c);
        }

        when(citaMedicaRepository.findByPacienteId(1L))
                .thenReturn(citasDelMes);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> citaService.agendar(requestValido)
        );

        assertEquals(
                "Has alcanzado el límite de 8 citas para este mes.",
                ex.getMessage()
        );
    }

    @Test
    @DisplayName("Cancelar cita exitosamente con más de 24 horas de anticipación")
    void cancelar_exitoso_masDe24Horas() {
        CitaMedica cita = new CitaMedica();
        cita.setId(100L);
        cita.setEstado("PROGRAMADA");
        cita.setFecha(LocalDate.now().plusDays(3));
        cita.setHora(LocalTime.of(10, 0));
        cita.setPacienteId(1L);
        cita.setEspecialistaId(10L);

        CancelarCitaRequest request = new CancelarCitaRequest();
        request.setMensaje("El paciente ya no puede asistir");

        when(citaMedicaRepository.findById(100L)).thenReturn(Optional.of(cita));
        when(citaMedicaRepository.save(any(CitaMedica.class))).thenReturn(cita);

        CitaMedica resultado = citaService.cancelar(100L, request);

        assertEquals("CANCELADA", resultado.getEstado());
        assertEquals(
                "El paciente ya no puede asistir",
                resultado.getMensajeCancelacion()
        );

        verify(eventPublisher).publishEvent(any(NotificacionCitaEvent.class));
    }

    @Test
    @DisplayName("Cancelar falla si faltan menos de 24 horas para la cita")
    void cancelar_menosDe24Horas_lanzaExcepcion() {
        CitaMedica cita = new CitaMedica();
        cita.setId(100L);
        cita.setEstado("PROGRAMADA");

        LocalDateTime enCincoHoras = LocalDateTime.now().plusHours(5);

        cita.setFecha(enCincoHoras.toLocalDate());
        cita.setHora(enCincoHoras.toLocalTime());

        CancelarCitaRequest request = new CancelarCitaRequest();
        request.setMensaje("Ya no puedo asistir");

        when(citaMedicaRepository.findById(100L)).thenReturn(Optional.of(cita));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> citaService.cancelar(100L, request)
        );

        assertEquals(
                "Solo se puede cancelar con al menos 24 horas de anticipación",
                ex.getMessage()
        );

        verify(citaMedicaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cancelar falla si la cita no está en estado activo")
    void cancelar_citaNoActiva_lanzaExcepcion() {
        CitaMedica cita = new CitaMedica();
        cita.setId(100L);
        cita.setEstado("ATENDIDA");
        cita.setFecha(LocalDate.now().plusDays(5));
        cita.setHora(LocalTime.of(10, 0));

        CancelarCitaRequest request = new CancelarCitaRequest();
        request.setMensaje("Motivo");

        when(citaMedicaRepository.findById(100L)).thenReturn(Optional.of(cita));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> citaService.cancelar(100L, request)
        );

        assertEquals("Solo se pueden cancelar citas activas", ex.getMessage());
    }

    @Test
    @DisplayName("Cancelar falla si la cita no existe")
    void cancelar_citaNoExiste_lanzaExcepcion() {
        CancelarCitaRequest request = new CancelarCitaRequest();
        request.setMensaje("Motivo");

        when(citaMedicaRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> citaService.cancelar(999L, request)
        );

        assertEquals("Cita no encontrada", ex.getMessage());
    }

    @Test
    @DisplayName("Listar por paciente retorna el historial de citas")
    void listarPorPaciente_retornaCitas() {
        CitaMedica c1 = new CitaMedica();
        c1.setId(1L);
        c1.setPacienteId(1L);
        c1.setEspecialistaId(10L);
        c1.setEstado("ATENDIDA");

        when(citaMedicaRepository.findByPacienteId(1L)).thenReturn(List.of(c1));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());
        when(especialistaRepository.findById(10L)).thenReturn(Optional.of(especialista));

        List<CitaMedica> resultado = citaService.listarPorPaciente(1L);

        assertEquals(1, resultado.size());
        assertEquals("Carlos Mendoza", resultado.get(0).getEspecialistaNombre());
    }

    @Test
    @DisplayName("Listar por paciente retorna lista vacía si no tiene citas")
    void listarPorPaciente_sinCitas_retornaVacio() {
        when(citaMedicaRepository.findByPacienteId(5L)).thenReturn(List.of());

        List<CitaMedica> resultado = citaService.listarPorPaciente(5L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Listar por especialista retorna sus citas programadas")
    void listarPorEspecialista_retornaCitas() {
        CitaMedica c1 = new CitaMedica();
        c1.setId(1L);
        c1.setPacienteId(1L);
        c1.setEspecialistaId(10L);
        c1.setEstado("PROGRAMADA");

        when(citaMedicaRepository.findByEspecialistaId(10L)).thenReturn(List.of(c1));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());
        when(especialistaRepository.findById(10L)).thenReturn(Optional.of(especialista));

        List<CitaMedica> resultado = citaService.listarPorEspecialista(10L);

        assertEquals(1, resultado.size());
        assertEquals("PROGRAMADA", resultado.get(0).getEstado());
    }

    @Test
    @DisplayName("Aceptar cita cambia el estado a PROGRAMADA")
    void aceptar_exitoso() {
        CitaMedica cita = new CitaMedica();
        cita.setId(100L);
        cita.setEstado("POR_CONFIRMAR");
        cita.setFecha(LocalDate.now().plusDays(5));
        cita.setHora(LocalTime.of(10, 0));
        cita.setPacienteId(1L);
        cita.setEspecialistaId(10L);

        when(citaMedicaRepository.findById(100L)).thenReturn(Optional.of(cita));
        when(citaMedicaRepository.save(any())).thenReturn(cita);
        when(especialistaRepository.findById(10L)).thenReturn(Optional.of(especialista));

        CitaMedica resultado = citaService.aceptar(100L);

        assertEquals("PROGRAMADA", resultado.getEstado());

        verify(eventPublisher).publishEvent(any(NotificacionCitaEvent.class));
    }

    @Test
    @DisplayName("Aceptar falla si la cita no está en estado POR_CONFIRMAR")
    void aceptar_estadoInvalido_lanzaExcepcion() {
        CitaMedica cita = new CitaMedica();
        cita.setId(100L);
        cita.setEstado("PROGRAMADA");

        when(citaMedicaRepository.findById(100L)).thenReturn(Optional.of(cita));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> citaService.aceptar(100L)
        );

        assertEquals(
                "Solo se pueden aceptar citas en estado POR_CONFIRMAR",
                ex.getMessage()
        );
    }
}
