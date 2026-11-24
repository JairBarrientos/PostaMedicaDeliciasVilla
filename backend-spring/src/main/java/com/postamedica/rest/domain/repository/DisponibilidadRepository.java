package com.postamedica.rest.domain.repository;

import com.postamedica.rest.domain.model.Disponibilidad;
import java.time.LocalDate;
import java.util.List;

public interface DisponibilidadRepository {
    Disponibilidad save(Disponibilidad disponibilidad);
    List<Disponibilidad> findByEspecialistaId(Long especialistaId);
    boolean existsByEspecialistaIdAndFechaAndTipo(Long especialistaId, LocalDate fecha, String tipo);
    boolean existsById(Long id);
    void deleteById(Long id);
}