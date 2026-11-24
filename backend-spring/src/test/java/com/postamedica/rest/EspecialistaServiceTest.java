package com.postamedica.rest;

import com.postamedica.rest.application.services.EspecialistaService;
import com.postamedica.rest.domain.dtos.CrearEspecialistaRequest;
import com.postamedica.rest.domain.dtos.LoginRequest;
import com.postamedica.rest.domain.dtos.LoginResponse;
import com.postamedica.rest.domain.model.Especialista;
import com.postamedica.rest.domain.repository.EspecialistaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EspecialistaServiceTest {

    @Mock
    private EspecialistaRepository especialistaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private EspecialistaService especialistaService;

    private CrearEspecialistaRequest requestValido;

    @BeforeEach
    void setUp() {
        especialistaService = new EspecialistaService(especialistaRepository, passwordEncoder);

        requestValido = new CrearEspecialistaRequest();
        requestValido.setNombres("Carlos");
        requestValido.setApellidos("Mendoza");
        requestValido.setEspecialidad("Cardiología");
        requestValido.setCorreo("carlos.mendoza@postamedica.com");
        requestValido.setPassword("Especialista123");
        requestValido.setDni("45678912");
        requestValido.setTelefono("987123456");
    }


    @Test
    @DisplayName("Crear especialista exitosamente con datos válidos")
    void crear_exitoso() {
        when(especialistaRepository.existsByDni("45678912")).thenReturn(false);
        when(especialistaRepository.existsByCorreo("carlos.mendoza@postamedica.com")).thenReturn(false);
        when(passwordEncoder.encode("Especialista123")).thenReturn("hashedPass");

        Especialista guardado = new Especialista();
        guardado.setId(1L);
        guardado.setEstado("ACTIVO");
        guardado.setDni("45678912");
        when(especialistaRepository.save(any(Especialista.class))).thenReturn(guardado);

        Especialista resultado = especialistaService.crear(requestValido);

        assertNotNull(resultado);
        assertEquals("ACTIVO", resultado.getEstado());
        verify(especialistaRepository).save(any(Especialista.class));
    }

    @Test
    @DisplayName("Crear falla si el DNI ya existe")
    void crear_dniDuplicado_lanzaExcepcion() {
        when(especialistaRepository.existsByDni("45678912")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> especialistaService.crear(requestValido));

        assertEquals("Ya existe un especialista con ese DNI", ex.getMessage());
        verify(especialistaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear falla si el correo ya existe")
    void crear_correoDuplicado_lanzaExcepcion() {
        when(especialistaRepository.existsByDni("45678912")).thenReturn(false);
        when(especialistaRepository.existsByCorreo("carlos.mendoza@postamedica.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> especialistaService.crear(requestValido));

        assertEquals("Ya existe un especialista con ese correo", ex.getMessage());
        verify(especialistaRepository, never()).save(any());
    }


    @Test
    @DisplayName("Eliminar especialista existente exitosamente")
    void eliminar_exitoso() {
        when(especialistaRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> especialistaService.deleteById(1L));

        verify(especialistaRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Eliminar falla si el especialista no existe")
    void eliminar_noExiste_lanzaExcepcion() {
        when(especialistaRepository.existsById(99L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> especialistaService.deleteById(99L));

        assertTrue(ex.getMessage().contains("no encontrado"));
        verify(especialistaRepository, never()).deleteById(any());
    }

    // ===================================================================
    // R.F. 23 — Administrador gestiona especialistas (cambiar estado)
    // ===================================================================

    @Test
    @DisplayName("Cambiar estado a INACTIVO exitosamente")
    void cambiarEstado_exitoso() {
        Especialista especialista = new Especialista();
        especialista.setId(1L);
        especialista.setEstado("ACTIVO");

        when(especialistaRepository.findById(1L)).thenReturn(Optional.of(especialista));
        when(especialistaRepository.save(any())).thenReturn(especialista);

        Especialista resultado = especialistaService.cambiarEstado(1L, "INACTIVO");

        assertEquals("INACTIVO", resultado.getEstado());
    }

    @Test
    @DisplayName("Cambiar estado falla si el valor no es ACTIVO ni INACTIVO")
    void cambiarEstado_valorInvalido_lanzaExcepcion() {
        Especialista especialista = new Especialista();
        especialista.setId(1L);
        especialista.setEstado("ACTIVO");

        when(especialistaRepository.findById(1L)).thenReturn(Optional.of(especialista));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> especialistaService.cambiarEstado(1L, "SUSPENDIDO"));

        assertEquals("Estado inválido. Use ACTIVO o INACTIVO", ex.getMessage());
    }


    @Test
    @DisplayName("Login de especialista exitoso con DNI y contraseña correcta")
    void login_exitoso() {
        Especialista especialista = new Especialista();
        especialista.setId(1L);
        especialista.setNombres("Carlos");
        especialista.setApellidos("Mendoza");
        especialista.setDni("45678912");
        especialista.setCorreo("carlos.mendoza@postamedica.com");
        especialista.setPassword("hashedPass");
        especialista.setEstado("ACTIVO");
        especialista.setEspecialidad("Cardiología");

        LoginRequest request = new LoginRequest();
        request.setIdentificador("45678912");
        request.setPassword("Especialista123");

        when(especialistaRepository.findByDni("45678912")).thenReturn(Optional.of(especialista));
        when(passwordEncoder.matches("Especialista123", "hashedPass")).thenReturn(true);

        Optional<LoginResponse> resultado = especialistaService.login(request);

        assertTrue(resultado.isPresent());
        assertEquals("ESPECIALISTA", resultado.get().getRol());
        assertEquals("Cardiología", resultado.get().getEspecialidad());
    }

    @Test
    @DisplayName("Login de especialista falla si la contraseña es incorrecta")
    void login_passwordIncorrecto_retornaVacio() {
        Especialista especialista = new Especialista();
        especialista.setDni("45678912");
        especialista.setPassword("hashedPass");

        LoginRequest request = new LoginRequest();
        request.setIdentificador("45678912");
        request.setPassword("wrongpass");

        when(especialistaRepository.findByDni("45678912")).thenReturn(Optional.of(especialista));
        when(passwordEncoder.matches("wrongpass", "hashedPass")).thenReturn(false);

        Optional<LoginResponse> resultado = especialistaService.login(request);

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Login de especialista falla si la cuenta está INACTIVA")
    void login_cuentaInactiva_lanzaExcepcion() {
        Especialista especialista = new Especialista();
        especialista.setDni("45678912");
        especialista.setPassword("hashedPass");
        especialista.setEstado("INACTIVO");

        LoginRequest request = new LoginRequest();
        request.setIdentificador("45678912");
        request.setPassword("Especialista123");

        when(especialistaRepository.findByDni("45678912")).thenReturn(Optional.of(especialista));
        when(passwordEncoder.matches("Especialista123", "hashedPass")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> especialistaService.login(request));

        assertEquals("Tu cuenta de especialista está inactiva.", ex.getMessage());
    }


    @Test
    @DisplayName("Listar especialistas activos retorna solo los que están ACTIVO")
    void findActivos_retornaSoloActivos() {
        Especialista activo = new Especialista();
        activo.setId(1L);
        activo.setEstado("ACTIVO");

        when(especialistaRepository.findByEstado("ACTIVO")).thenReturn(List.of(activo));

        List<Especialista> resultado = especialistaService.findActivos();

        assertEquals(1, resultado.size());
        assertEquals("ACTIVO", resultado.get(0).getEstado());
    }
}