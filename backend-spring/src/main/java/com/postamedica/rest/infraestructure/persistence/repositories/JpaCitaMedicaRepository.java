package com.postamedica.rest.infraestructure.persistence.repositories;

import com.postamedica.rest.domain.model.CitaMedica;
import com.postamedica.rest.domain.repository.CitaMedicaRepository;
import com.postamedica.rest.infraestructure.persistence.entities.CitaMedicaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaCitaMedicaRepository implements CitaMedicaRepository {

    private final SpringDataCitaMedicaRepository springDataRepo;

    @Override
    public CitaMedica save(CitaMedica cita) {
        CitaMedicaEntity entity = CitaMedicaEntity.fromDomain(cita);
        return springDataRepo.save(entity).toDomain();
    }

    @Override
    public Optional<CitaMedica> findById(Long id) {
        return springDataRepo.findById(id).map(CitaMedicaEntity::toDomain);
    }

    @Override
    public List<CitaMedica> findAll() {
        return springDataRepo.findAll().stream()
                .map(CitaMedicaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CitaMedica> findByPacienteId(Long pacienteId) {
        return springDataRepo.findByPacienteId(pacienteId).stream()
                .map(CitaMedicaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CitaMedica> findByEspecialistaId(Long especialistaId) {
        return springDataRepo.findByEspecialistaId(especialistaId).stream()
                .map(CitaMedicaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existeConflicto(Long especialistaId, LocalDate fecha, LocalTime hora) {
        return springDataRepo.existeConflicto(especialistaId, fecha, hora);
    }

    @Override
    public boolean existsById(Long id) {
        return springDataRepo.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        springDataRepo.deleteById(id);
    }
}