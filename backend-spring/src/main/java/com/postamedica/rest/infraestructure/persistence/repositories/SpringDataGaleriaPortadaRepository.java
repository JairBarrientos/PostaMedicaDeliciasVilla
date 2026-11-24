package com.postamedica.rest.infraestructure.persistence.repositories;

import com.postamedica.rest.infraestructure.persistence.entities.GaleriaPortadaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SpringDataGaleriaPortadaRepository extends JpaRepository<GaleriaPortadaEntity, Long> {
    List<GaleriaPortadaEntity> findAllByOrderByOrdenAsc();
    boolean existsByOrden(Integer orden);
}