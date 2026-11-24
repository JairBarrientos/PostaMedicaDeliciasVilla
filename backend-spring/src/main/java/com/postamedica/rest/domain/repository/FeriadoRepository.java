package com.postamedica.rest.domain.repository;

import com.postamedica.rest.domain.model.Feriado;
import java.time.LocalDate;
import java.util.List;

public interface FeriadoRepository {
    Feriado save(Feriado feriado);
    boolean existsByFecha(LocalDate fecha);
    void deleteByFecha(LocalDate fecha);
    List<Feriado> findByFechaBetween(LocalDate inicio, LocalDate fin);
}