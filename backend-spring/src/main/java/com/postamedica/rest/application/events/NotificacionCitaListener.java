
package com.postamedica.rest.application.events;

import com.postamedica.rest.application.services.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacionCitaListener {

    private final NotificacionService notificacionService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void procesar(NotificacionCitaEvent event) {
        try {
            notificacionService.enviar(
                    event.pacienteId(),
                    event.especialistaId(),
                    event.texto()
            );
        } catch (Exception e) {
            log.error("Error al procesar notificación de cita: {}",
                    e.getMessage(), e);
        }
    }
}
