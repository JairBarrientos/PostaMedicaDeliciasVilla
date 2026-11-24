package com.postamedica.rest.application.services;

import com.postamedica.rest.domain.model.CitaMedica;
import com.postamedica.rest.domain.repository.CitaMedicaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmacionAutomaticaJob {

    private final CitaMedicaRepository citaMedicaRepository;

    @Scheduled(fixedDelay = 5 * 60 * 1000) // cada 5 minutos
    @Transactional
    public void confirmarCitasPendientes() {
        LocalDateTime hace1Hora = LocalDateTime.now().minusHours(1);

        List<CitaMedica> pendientes = citaMedicaRepository.findAll().stream()
            .filter(c -> "POR_CONFIRMAR".equals(c.getEstado()))
            .filter(c -> c.getFechaCreacion() != null && c.getFechaCreacion().isBefore(hace1Hora))
            .toList();

        if (pendientes.isEmpty()) return;

        for (CitaMedica cita : pendientes) {
            cita.setEstado("PROGRAMADA");
            citaMedicaRepository.save(cita);
            log.info("Cita {} confirmada automáticamente (creada: {})",
                cita.getCodigoCita(), cita.getFechaCreacion());
        }

        log.info("Job confirmación automática: {} cita(s) confirmada(s)", pendientes.size());
    }
}