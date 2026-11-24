package com.postamedica.rest.domain.repository;

import com.postamedica.rest.domain.model.Especialista;
import java.util.List;
import java.util.Optional;

public interface EspecialistaRepository {
    Especialista save(Especialista especialista);
    Optional<Especialista> findById(Long id);
    Optional<Especialista> findByCorreo(String correo);
    Optional<Especialista> findByDni(String dni);
    List<Especialista> findAll();
    List<Especialista> findByEstado(String estado);
    boolean existsById(Long id);
    boolean existsByCorreo(String correo);
    boolean existsByDni(String dni);
    void deleteById(Long id);
}