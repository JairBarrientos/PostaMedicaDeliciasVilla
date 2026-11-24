package com.postamedica.rest.infraestructure.persistence.repositories;

import com.postamedica.rest.domain.model.Disponibilidad;
import com.postamedica.rest.domain.repository.DisponibilidadRepository;
import com.postamedica.rest.infraestructure.persistence.entities.DisponibilidadEspecialistaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaDisponibilidadRepository implements DisponibilidadRepository {

    private final SpringDataDisponibilidadRepository springDataRepo;

    @Override
    public Disponibilidad save(Disponibilidad d) {
        DisponibilidadEspecialistaEntity entity = DisponibilidadEspecialistaEntity.builder()
                .id(d.getId())
                .especialistaId(d.getEspecialistaId())
                .fecha(d.getFecha())
                .motivo(d.getMotivo())
                .horaInicio(d.getHoraInicio())
                .horaFin(d.getHoraFin())
                .tipo(d.getTipo())
                .build();
        DisponibilidadEspecialistaEntity saved = springDataRepo.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<Disponibilidad> findByEspecialistaId(Long especialistaId) {
        return springDataRepo.findByEspecialistaId(especialistaId)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByEspecialistaIdAndFechaAndTipo(Long especialistaId, LocalDate fecha, String tipo) {
        return springDataRepo.existsByEspecialistaIdAndFechaAndTipo(especialistaId, fecha, tipo);
    }

    @Override
    public boolean existsById(Long id) {
        return springDataRepo.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        springDataRepo.deleteById(id);
    }

    private Disponibilidad toDomain(DisponibilidadEspecialistaEntity e) {
        Disponibilidad d = new Disponibilidad();
        d.setId(e.getId());
        d.setEspecialistaId(e.getEspecialistaId());
        d.setFecha(e.getFecha());
        d.setMotivo(e.getMotivo());
        d.setHoraInicio(e.getHoraInicio());
        d.setHoraFin(e.getHoraFin());
        d.setTipo(e.getTipo());
        return d;
    }
}