package com.postamedica.rest.domain.repository;

import com.postamedica.rest.domain.model.MensajeChat;
import java.util.List;
import java.util.Optional;

public interface MensajeChatRepository {
    MensajeChat save(MensajeChat mensaje);
    Optional<MensajeChat> findById(Long id);
    List<MensajeChat> findConversacion(Long pacienteId, Long especialistaId);
    List<MensajeChat> findNoLeidosByPaciente(Long pacienteId);
    void marcarComoLeidos(Long pacienteId, Long especialistaId);
    List<Long> findEspecialistasConNoLeidos(Long pacienteId);
}