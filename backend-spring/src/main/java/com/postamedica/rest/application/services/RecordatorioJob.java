package com.postamedica.rest.application.services;

import com.postamedica.rest.domain.model.CitaMedica;
import com.postamedica.rest.domain.model.MensajeChat;
import com.postamedica.rest.domain.repository.CitaMedicaRepository;
import com.postamedica.rest.domain.repository.MensajeChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordatorioJob {

    private final CitaMedicaRepository    citaMedicaRepository;
    private final MensajeChatRepository   mensajeChatRepository;

    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_HORA  = DateTimeFormatter.ofPattern("HH:mm");

    @Scheduled(fixedDelay = 10 * 60 * 1000) // cada 10 minutos
    @Transactional
    public void enviarRecordatorios() {
        LocalDateTime ahora = LocalDateTime.now();

        List<CitaMedica> citas = citaMedicaRepository.findAll().stream()
            .filter(c -> "PROGRAMADA".equals(c.getEstado()))
            .filter(c -> c.getFecha() != null && c.getHora() != null)
            .toList();

        for (CitaMedica cita : citas) {
            LocalDateTime fechaHoraCita = LocalDateTime.of(cita.getFecha(), cita.getHora());

            // Solo citas futuras
            if (!fechaHoraCita.isAfter(ahora)) continue;

            long minutosRestantes = java.time.Duration.between(ahora, fechaHoraCita).toMinutes();

            // ── Recordatorio 24h antes (ventana: entre 23h50 y 24h10) ──
            if (minutosRestantes >= 1430 && minutosRestantes <= 1450) {
                if (!yaEnvioRecordatorio(cita, "24H")) {
                    enviar(cita, buildMensaje24h(cita), "24H");
                }
            }

            // ── Recordatorio 1h antes (ventana: entre 50min y 70min) ──
            if (minutosRestantes >= 50 && minutosRestantes <= 70) {
                if (!yaEnvioRecordatorio(cita, "1H")) {
                    enviar(cita, buildMensaje1h(cita), "1H");
                }
            }
        }
    }

    // ── Construcción de mensajes ─────────────────────────────────────────────

    private String buildMensaje24h(CitaMedica cita) {
        return "🔔 *Recordatorio:* Tienes una cita programada mañana " +
               cita.getFecha().format(FMT_FECHA) + " a las " +
               cita.getHora().format(FMT_HORA) + " hrs. " +
               "Código: " + cita.getCodigoCita() + ". " +
               "Por favor preséntate con anticipación.";
    }

    private String buildMensaje1h(CitaMedica cita) {
        return "⏰ *Recordatorio:* Tu cita es en aproximadamente 1 hora — " +
               cita.getFecha().format(FMT_FECHA) + " a las " +
               cita.getHora().format(FMT_HORA) + " hrs. " +
               "Código: " + cita.getCodigoCita() + ". ¡No faltes!";
    }

    // ── Verificar si ya se envió ese recordatorio ────────────────────────────

    private boolean yaEnvioRecordatorio(CitaMedica cita, String tipo) {
        String marca = "[REC-" + tipo + "-" + cita.getCodigoCita() + "]";
        List<MensajeChat> conversacion = mensajeChatRepository
            .findConversacion(cita.getPacienteId(), cita.getEspecialistaId());
        return conversacion.stream()
            .anyMatch(m -> "SISTEMA".equals(m.getRemitenteRol()) &&
                          m.getContenido() != null &&
                          m.getContenido().contains(marca));
    }

    // ── Guardar el mensaje en el chat ────────────────────────────────────────

    private void enviar(CitaMedica cita, String texto, String tipo) {
        String marca = " [REC-" + tipo + "-" + cita.getCodigoCita() + "]";
        MensajeChat mensaje = new MensajeChat();
        mensaje.setPacienteId(cita.getPacienteId());
        mensaje.setEspecialistaId(cita.getEspecialistaId());
        mensaje.setRemitenteRol("SISTEMA");
        mensaje.setContenido(texto + marca);
        mensaje.setFechaEnvio(LocalDateTime.now());
        mensaje.setLeido(false);
        mensajeChatRepository.save(mensaje);
        log.info("Recordatorio {} enviado para cita {}", tipo, cita.getCodigoCita());
    }
}