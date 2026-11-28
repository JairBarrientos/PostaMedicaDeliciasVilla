package com.postamedica.rest.infraestructure.persistence.repositories;

import com.postamedica.rest.domain.model.MensajeChat;
import com.postamedica.rest.domain.repository.MensajeChatRepository;
import com.postamedica.rest.infraestructure.persistence.entities.MensajeChatEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaMensajeChatRepository implements MensajeChatRepository {

    private final SpringDataMensajeChatRepository springDataRepo;

    @Override
    public MensajeChat save(MensajeChat mensaje) {
        MensajeChatEntity entity = MensajeChatEntity.fromDomain(mensaje);
        return springDataRepo.save(entity).toDomain();
    }

    @Override
    public Optional<MensajeChat> findById(Long id) {
        return springDataRepo.findById(id).map(MensajeChatEntity::toDomain);
    }

    @Override
    public List<MensajeChat> findConversacion(Long pacienteId, Long especialistaId) {
        return springDataRepo.findConversacion(pacienteId, especialistaId).stream()
                .map(MensajeChatEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MensajeChat> findNoLeidosByPaciente(Long pacienteId) {
        return springDataRepo.findNoLeidosByPaciente(pacienteId).stream()
                .map(MensajeChatEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void marcarComoLeidos(Long pacienteId, Long especialistaId) {
        springDataRepo.marcarComoLeidos(pacienteId, especialistaId);
    }

    // ✅ NUEVO
    @Override
    public List<Long> findEspecialistasConNoLeidos(Long pacienteId) {
        return springDataRepo.findEspecialistasConNoLeidos(pacienteId);
    }

    @Override
    public Map<Long, Long> contarNoLeidosPorPaciente(Long especialistaId) {
        Map<Long, Long> resultado = new HashMap<>();
        for (Object[] fila : springDataRepo.contarNoLeidosPorPaciente(especialistaId)) {
            resultado.put(((Number) fila[0]).longValue(), ((Number) fila[1]).longValue());
        }
        return resultado;
    }

    @Override
    public void marcarComoLeidosPorEspecialista(Long pacienteId, Long especialistaId) {
        springDataRepo.marcarComoLeidosPorEspecialista(pacienteId, especialistaId);
    }
}