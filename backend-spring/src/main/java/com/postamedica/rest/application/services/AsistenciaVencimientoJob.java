package com.postamedica.rest.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsistenciaVencimientoJob {

    private final CitaService citaService;

    /**
     * Cada minuto revisa si alguna cita PROGRAMADA ya venció su ventana de 30 min
     * sin que el especialista marcara asistencia.
     * Si es así, la pasa automáticamente a SIN_RESPUESTA.
     *
     * Ejemplo: cita a las 7:00 → si a las 7:30:01 sigue PROGRAMADA → pasa a SIN_RESPUESTA.
     */
    @Scheduled(fixedDelay = 60 * 1000) // cada 1 minuto
    public void marcarVencidas() {
        citaService.marcarSinRespuestaVencidas();
        log.debug("Job AsistenciaVencimiento ejecutado");
    }
}