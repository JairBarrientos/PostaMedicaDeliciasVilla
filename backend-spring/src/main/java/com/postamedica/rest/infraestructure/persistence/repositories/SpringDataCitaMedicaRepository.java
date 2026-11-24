package com.postamedica.rest.infraestructure.persistence.repositories;

import com.postamedica.rest.infraestructure.persistence.entities.CitaMedicaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface SpringDataCitaMedicaRepository extends JpaRepository<CitaMedicaEntity, Long> {

    List<CitaMedicaEntity> findByPacienteId(Long pacienteId);

    List<CitaMedicaEntity> findByEspecialistaId(Long especialistaId);

    @Query("""
        SELECT COUNT(c) > 0 FROM CitaMedicaEntity c
        WHERE c.especialistaId = :especialistaId
          AND c.fecha = :fecha
          AND c.hora = :hora
          AND c.estado IN ('PROGRAMADA', 'POR_CONFIRMAR', 'NO_ASISTIDA', 'ATENDIDA')
    """)
    boolean existeConflicto(
            @Param("especialistaId") Long especialistaId,
            @Param("fecha") LocalDate fecha,
            @Param("hora") LocalTime hora
    );
}