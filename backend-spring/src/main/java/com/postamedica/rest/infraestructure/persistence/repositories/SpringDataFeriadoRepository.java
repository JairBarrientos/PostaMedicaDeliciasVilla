package com.postamedica.rest.infraestructure.persistence.repositories;

import com.postamedica.rest.infraestructure.persistence.entities.FeriadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SpringDataFeriadoRepository extends JpaRepository<FeriadoEntity, Long> {
    boolean existsByFecha(LocalDate fecha);
    void deleteByFecha(LocalDate fecha);
    List<FeriadoEntity> findByFechaBetween(LocalDate inicio, LocalDate fin);
}