package com.postamedica.rest.application.services;

import com.postamedica.rest.domain.dtos.EnviarMensajeRequest;
import com.postamedica.rest.domain.model.CitaMedica;
import com.postamedica.rest.domain.model.MensajeChat;
import com.postamedica.rest.domain.repository.CitaMedicaRepository;
import com.postamedica.rest.domain.repository.MensajeChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final MensajeChatRepository mensajeChatRepository;
    private final CitaMedicaRepository citaMedicaRepository;

    public MensajeChat enviar(EnviarMensajeRequest request) {
        if (request.getContenido() == null || request.getContenido().isBlank())
            throw new RuntimeException("El mensaje no puede estar vacío");
        if (!List.of("PACIENTE", "ESPECIALISTA").contains(request.getRemitenteRol()))
            throw new RuntimeException("Rol inválido. Debe ser PACIENTE o ESPECIALISTA");

        // ✅ Validar que el chat esté habilitado antes de enviar
        if (!chatHabilitado(request.getPacienteId(), request.getEspecialistaId()))
            throw new RuntimeException("El chat no está habilitado para esta conversación");

        MensajeChat mensaje = new MensajeChat();
        mensaje.setPacienteId(request.getPacienteId());
        mensaje.setEspecialistaId(request.getEspecialistaId());
        mensaje.setRemitenteRol(request.getRemitenteRol());
        mensaje.setContenido(request.getContenido().trim());
        mensaje.setFechaEnvio(java.time.LocalDateTime.now());
        mensaje.setLeido(false);

        return mensajeChatRepository.save(mensaje);
    }

    @Transactional(readOnly = true)
    public List<MensajeChat> obtenerConversacion(Long pacienteId, Long especialistaId) {
        return mensajeChatRepository.findConversacion(pacienteId, especialistaId);
    }

    @Transactional(readOnly = true)
    public int contarNoLeidos(Long pacienteId) {
        return mensajeChatRepository.findNoLeidosByPaciente(pacienteId).size();
    }

    public void marcarComoLeidos(Long pacienteId, Long especialistaId) {
        mensajeChatRepository.marcarComoLeidos(pacienteId, especialistaId);
    }

    @Transactional(readOnly = true)
    public List<Long> especialistasConNoLeidos(Long pacienteId) {
        return mensajeChatRepository.findEspecialistasConNoLeidos(pacienteId);
    }

    @Transactional(readOnly = true)
    public boolean chatHabilitado(Long pacienteId, Long especialistaId) {
        LocalDate hoy = LocalDate.now();
        List<CitaMedica> citas = citaMedicaRepository.findByPacienteId(pacienteId);
        return citas.stream().anyMatch(c ->
            c.getEspecialistaId().equals(especialistaId) &&
            "PROGRAMADA".equals(c.getEstado()) &&
            !c.getFecha().isBefore(hoy)
        );
    }
}