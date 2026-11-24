package com.postamedica.rest.infraestructure.persistence.repositories;

import com.postamedica.rest.domain.model.Especialista;
import com.postamedica.rest.domain.repository.EspecialistaRepository;
import com.postamedica.rest.infraestructure.persistence.entities.EspecialistaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaEspecialistaRepository implements EspecialistaRepository {

    private final SpringDataEspecialistaRepository springDataRepo;

    @Override
    public Especialista save(Especialista especialista) {
        EspecialistaEntity entity = EspecialistaEntity.fromDomain(especialista);
        return springDataRepo.save(entity).toDomain();
    }

    @Override
    public Optional<Especialista> findById(Long id) {
        return springDataRepo.findById(id).map(EspecialistaEntity::toDomain);
    }

    @Override
    public Optional<Especialista> findByCorreo(String correo) {
        return springDataRepo.findByCorreo(correo).map(EspecialistaEntity::toDomain);
    }

    @Override
    public Optional<Especialista> findByDni(String dni) {
        return springDataRepo.findByDni(dni).map(EspecialistaEntity::toDomain);
    }

    @Override
    public List<Especialista> findAll() {
        return springDataRepo.findAll().stream()
                .map(EspecialistaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Especialista> findByEstado(String estado) {
        return springDataRepo.findByEstado(estado).stream()
                .map(EspecialistaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        return springDataRepo.existsById(id);
    }

    @Override
    public boolean existsByCorreo(String correo) {
        return springDataRepo.existsByCorreo(correo);
    }

    @Override
    public boolean existsByDni(String dni) {
        return springDataRepo.existsByDni(dni);
    }

    @Override
    public void deleteById(Long id) {
        springDataRepo.deleteById(id);
    }
}