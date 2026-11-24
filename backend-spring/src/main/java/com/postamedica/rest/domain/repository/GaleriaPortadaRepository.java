package com.postamedica.rest.domain.repository;

import com.postamedica.rest.domain.model.GaleriaPortada;
import java.util.List;
import java.util.Optional;

public interface GaleriaPortadaRepository {
    GaleriaPortada save(GaleriaPortada galeria);
    Optional<GaleriaPortada> findById(Long id);
    List<GaleriaPortada> findAllOrdenado();
    boolean existsByOrden(Integer orden);
    boolean existsById(Long id);
    long count();
    void deleteById(Long id);
}