
package com.postamedica.rest.application.services;

import com.postamedica.rest.domain.model.MensajeChat;
import com.postamedica.rest.domain.repository.MensajeChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final MensajeChatRepository mensajeChatRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enviar(Long pacienteId, Long especialistaId, String texto) {
        MensajeChat msg = new MensajeChat();
        msg.setPacienteId(pacienteId);
        msg.setEspecialistaId(especialistaId);
        msg.setRemitenteRol("SISTEMA");
        msg.setContenido(texto);
        msg.setFechaEnvio(LocalDateTime.now());
        msg.setLeido(false);

        try {
            mensajeChatRepository.save(msg);
        } catch (Exception e) {
            log.error(
                    "No se pudo guardar la notificación. pacienteId={}, especialistaId={}",
                    pacienteId, especialistaId, e
            );
            throw e;
        }
    }
}
