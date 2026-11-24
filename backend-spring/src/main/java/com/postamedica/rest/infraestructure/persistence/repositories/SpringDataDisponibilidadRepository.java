package com.postamedica.rest.infraestructure.persistence.repositories;

import com.postamedica.rest.infraestructure.persistence.entities.DisponibilidadEspecialistaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SpringDataDisponibilidadRepository extends JpaRepository<DisponibilidadEspecialistaEntity, Long> {
    List<DisponibilidadEspecialistaEntity> findByEspecialistaId(Long especialistaId);
    boolean existsByEspecialistaIdAndFecha(Long especialistaId, LocalDate fecha);
    boolean existsByEspecialistaIdAndFechaAndTipo(Long especialistaId, LocalDate fecha, String tipo);
    void deleteByEspecialistaIdAndFecha(Long especialistaId, LocalDate fecha);
}