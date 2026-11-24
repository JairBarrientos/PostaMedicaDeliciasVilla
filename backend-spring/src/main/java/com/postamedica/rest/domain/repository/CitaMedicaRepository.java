package com.postamedica.rest.domain.repository;

import com.postamedica.rest.domain.model.CitaMedica;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface CitaMedicaRepository {
    CitaMedica save(CitaMedica cita);
    Optional<CitaMedica> findById(Long id);
    List<CitaMedica> findAll();
    List<CitaMedica> findByPacienteId(Long pacienteId);
    List<CitaMedica> findByEspecialistaId(Long especialistaId);
    boolean existeConflicto(Long especialistaId, LocalDate fecha, LocalTime hora);
    boolean existsById(Long id);
    void deleteById(Long id);
}