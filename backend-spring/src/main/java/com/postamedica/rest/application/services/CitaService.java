
package com.postamedica.rest.application.services;

import com.postamedica.rest.application.events.NotificacionCitaEvent;
import com.postamedica.rest.domain.dtos.CancelarCitaRequest;
import com.postamedica.rest.domain.dtos.CrearCitaRequest;
import com.postamedica.rest.domain.model.CitaMedica;
import com.postamedica.rest.domain.repository.CitaMedicaRepository;
import com.postamedica.rest.domain.repository.EspecialistaRepository;
import com.postamedica.rest.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CitaService {

    private static final String POR_CONFIRMAR = "POR_CONFIRMAR";
    private static final String PROGRAMADA = "PROGRAMADA";
    private static final String CANCELADA = "CANCELADA";
    private static final String ATENDIDA = "ATENDIDA";
    private static final String NO_ASISTIDA = "NO_ASISTIDA";
    private static final String SIN_RESPUESTA = "SIN_RESPUESTA";

    private static final List<String> ACTIVOS =
            List.of(POR_CONFIRMAR, PROGRAMADA);

    private static final DateTimeFormatter FMT_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FMT_HORA =
            DateTimeFormatter.ofPattern("HH:mm");

    private final CitaMedicaRepository citaMedicaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EspecialistaRepository especialistaRepository;
    private final ApplicationEventPublisher eventPublisher;

    // GENERAR CÓDIGO
    private String generarCodigo(CrearCitaRequest request) {
        String fecha = request.getFecha()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String hora = request.getHora()
                .format(DateTimeFormatter.ofPattern("HHmm"));

        return "CM-" + fecha + "-" + hora + "-"
                + request.getEspecialistaId();
    }

    // AGENDAR
    @Transactional
    public CitaMedica agendar(CrearCitaRequest request) {

        if (!usuarioRepository.existsById(request.getPacienteId())) {
            throw new RuntimeException("Paciente no encontrado");
        }

        if (!especialistaRepository.existsById(request.getEspecialistaId())) {
            throw new RuntimeException("Especialista no encontrado");
        }

        if (request.getFecha().isBefore(LocalDate.now())) {
            throw new RuntimeException(
                    "No se pueden agendar citas en fechas pasadas"
            );
        }

        List<CitaMedica> todasCitas =
                citaMedicaRepository.findByPacienteId(request.getPacienteId());

        LocalDate primerDiaMes =
                request.getFecha().withDayOfMonth(1);

        LocalDate ultimoDiaMes =
                request.getFecha().withDayOfMonth(
                        request.getFecha().lengthOfMonth()
                );

        long citasMes = todasCitas.stream()
                .filter(c -> ACTIVOS.contains(c.getEstado()))
                .filter(c -> !c.getFecha().isBefore(primerDiaMes)
                        && !c.getFecha().isAfter(ultimoDiaMes))
                .count();

        if (citasMes >= 8) {
            throw new RuntimeException(
                    "Has alcanzado el límite de 8 citas para este mes."
            );
        }

        LocalDate lunes = request.getFecha()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        LocalDate domingo = lunes
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<CitaMedica> citasSemana = todasCitas.stream()
                .filter(c -> ACTIVOS.contains(c.getEstado()))
                .filter(c -> !c.getFecha().isBefore(lunes)
                        && !c.getFecha().isAfter(domingo))
                .toList();

        if (citasSemana.size() >= 2) {
            throw new RuntimeException(
                    "Ya tienes 2 citas esta semana. El límite es 2 por semana."
            );
        }

        String espSolicitada = especialistaRepository
                .findById(request.getEspecialistaId())
                .orElseThrow(() ->
                        new RuntimeException("Especialista no encontrado"))
                .getEspecialidad()
                .trim()
                .toLowerCase();

        for (CitaMedica c : citasSemana) {

            String espExistente = especialistaRepository
                    .findById(c.getEspecialistaId())
                    .map(e -> e.getEspecialidad().trim().toLowerCase())
                    .orElse("");

            if (espExistente.equals(espSolicitada)) {
                throw new RuntimeException(
                        "Ya tienes una cita de "
                                + espSolicitada + " esta semana."
                );
            }
        }

        if (citaMedicaRepository.existeConflicto(
                request.getEspecialistaId(),
                request.getFecha(),
                request.getHora())) {

            throw new RuntimeException(
                    "El especialista ya tiene una cita a esa hora"
            );
        }

        CitaMedica cita = new CitaMedica();

        cita.setFecha(request.getFecha());
        cita.setHora(request.getHora());
        cita.setCodigoCita(generarCodigo(request));
        cita.setEstado(POR_CONFIRMAR);
        cita.setPacienteId(request.getPacienteId());
        cita.setEspecialistaId(request.getEspecialistaId());
        cita.setFechaCreacion(LocalDateTime.now());

        CitaMedica guardada = citaMedicaRepository.save(cita);

        enviarNotificacion(
                guardada,
                "✅ Tu cita ha sido registrada exitosamente para el "
                        + guardada.getFecha().format(FMT_FECHA)
                        + " a las "
                        + guardada.getHora().format(FMT_HORA)
                        + " hrs. Código: "
                        + guardada.getCodigoCita()
                        + ". Está pendiente de confirmación por el especialista."
        );

        return guardada;
    }

    // ACEPTAR
    @Transactional
    public CitaMedica aceptar(Long id) {

        CitaMedica cita = citaMedicaRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Cita no encontrada"));

        if (!POR_CONFIRMAR.equals(cita.getEstado())) {
            throw new RuntimeException(
                    "Solo se pueden aceptar citas en estado POR_CONFIRMAR"
            );
        }

        cita.setEstado(PROGRAMADA);

        CitaMedica guardada = citaMedicaRepository.save(cita);

        String nombreEsp = especialistaRepository
                .findById(cita.getEspecialistaId())
                .map(e -> "Dr(a). " + e.getNombreCompleto())
                .orElse("el especialista");

        enviarNotificacion(
                guardada,
                "🎉 ¡Tu cita ha sido CONFIRMADA! "
                        + "Fecha: "
                        + guardada.getFecha().format(FMT_FECHA)
                        + " a las "
                        + guardada.getHora().format(FMT_HORA)
                        + " hrs. Especialista: "
                        + nombreEsp
                        + ". Código: "
                        + guardada.getCodigoCita()
                        + ". Recuerda presentarte con anticipación."
        );

        return guardada;
    }

    // CANCELAR
    @Transactional
    public CitaMedica cancelar(
            Long id,
            CancelarCitaRequest request) {

        CitaMedica cita = citaMedicaRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Cita no encontrada"));

        if (!ACTIVOS.contains(cita.getEstado())) {
            throw new RuntimeException(
                    "Solo se pueden cancelar citas activas"
            );
        }

        LocalDateTime fechaHoraCita =
                LocalDateTime.of(cita.getFecha(), cita.getHora());

        if (LocalDateTime.now().isAfter(
                fechaHoraCita.minusHours(24))) {

            throw new RuntimeException(
                    "Solo se puede cancelar con al menos 24 horas de anticipación"
            );
        }

        cita.setEstado(CANCELADA);
        cita.setMensajeCancelacion(request.getMensaje());

        CitaMedica guardada = citaMedicaRepository.save(cita);

        enviarNotificacion(
                guardada,
                "❌ Tu cita del "
                        + guardada.getFecha().format(FMT_FECHA)
                        + " a las "
                        + guardada.getHora().format(FMT_HORA)
                        + " hrs ha sido cancelada. Motivo: "
                        + request.getMensaje()
                        + ". Puedes reagendar cuando quieras."
        );

        return guardada;
    }

    // ACTUALIZAR ESTADO
    @Transactional
    public CitaMedica actualizarEstado(
            Long id,
            String nuevoEstado) {

        CitaMedica cita = citaMedicaRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Cita no encontrada"));

        if (!List.of(ATENDIDA, NO_ASISTIDA).contains(nuevoEstado)) {
            throw new RuntimeException(
                    "Estado inválido. Solo se permite ATENDIDA o NO_ASISTIDA"
            );
        }

        if (!List.of(
                PROGRAMADA,
                SIN_RESPUESTA,
                ATENDIDA,
                NO_ASISTIDA
        ).contains(cita.getEstado())) {

            throw new RuntimeException(
                    "No se puede modificar el estado de esta cita"
            );
        }

        cita.setEstado(nuevoEstado);

        return citaMedicaRepository.save(cita);
    }

    // JOB: MARCAR SIN_RESPUESTA
    @Transactional
    public void marcarSinRespuestaVencidas() {

        LocalDateTime ahora = LocalDateTime.now();

        citaMedicaRepository.findAll().stream()
                .filter(c -> PROGRAMADA.equals(c.getEstado()))
                .filter(c -> {
                    LocalDateTime fin = LocalDateTime
                            .of(c.getFecha(), c.getHora())
                            .plusMinutes(30);

                    return ahora.isAfter(fin);
                })
                .forEach(c -> {
                    c.setEstado(SIN_RESPUESTA);
                    citaMedicaRepository.save(c);
                });
    }

    // LISTAR
    @Transactional(readOnly = true)
    public List<CitaMedica> listarTodas() {
        return citaMedicaRepository.findAll()
                .stream()
                .map(this::enriquecer)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CitaMedica> listarPorPaciente(Long pacienteId) {
        return citaMedicaRepository.findByPacienteId(pacienteId)
                .stream()
                .map(this::enriquecer)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CitaMedica> listarPorEspecialista(Long especialistaId) {
        return citaMedicaRepository.findByEspecialistaId(especialistaId)
                .stream()
                .map(this::enriquecer)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<CitaMedica> findById(Long id) {
        return citaMedicaRepository.findById(id)
                .map(this::enriquecer);
    }

    // NOTIFICACIÓN MEDIANTE EVENTO
    private void enviarNotificacion(CitaMedica cita, String texto) {

        eventPublisher.publishEvent(
                new NotificacionCitaEvent(
                        cita.getPacienteId(),
                        cita.getEspecialistaId(),
                        texto
                )
        );
    }

    // ENRIQUECER
    private CitaMedica enriquecer(CitaMedica cita) {

        usuarioRepository.findById(cita.getPacienteId())
                .ifPresent(u -> {
                    cita.setPacienteNombre(u.getNombreCompleto());
                    cita.setPacienteDni(u.getDni());
                });

        especialistaRepository.findById(cita.getEspecialistaId())
                .ifPresent(e -> {
                    cita.setEspecialistaNombre(e.getNombreCompleto());
                    cita.setEspecialistaEspecialidad(e.getEspecialidad());
                });

        return cita;
    }
}
