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

    /**
     * Envía una notificación de sistema guardando un MensajeChat.
     * <p>
     * Se ejecuta siempre en una transacción COMPLETAMENTE INDEPENDIENTE
     * (REQUIRES_NEW): si este método falla por cualquier motivo —constraint
     * de BD, timeout, conexión caída— su propia transacción se revierte
     * sin contaminar jamás la transacción de la llamada principal.
     * El error queda registrado en los logs con suficiente detalle para
     * diagnosticarlo; nunca se relanza la excepción.
     *
     * @param pacienteId     ID del paciente destinatario
     * @param especialistaId ID del especialista involucrado
     * @param texto          Contenido del mensaje a enviar
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enviar(Long pacienteId, Long especialistaId, String texto) {
        try {
            MensajeChat msg = new MensajeChat();
            msg.setPacienteId(pacienteId);
            msg.setEspecialistaId(especialistaId);
            msg.setRemitenteRol("SISTEMA");
            msg.setContenido(texto);
            msg.setFechaEnvio(LocalDateTime.now());
            msg.setLeido(false);
            mensajeChatRepository.save(msg);
        } catch (Exception e) {
            log.warn(
                "No se pudo enviar notificación de sistema. " +
                "pacienteId={}, especialistaId={}, error={}",
                pacienteId, especialistaId, e.getMessage()
            );
        }
    }
}