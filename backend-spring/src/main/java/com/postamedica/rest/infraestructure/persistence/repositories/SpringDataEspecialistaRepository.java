package com.postamedica.rest.infraestructure.persistence.repositories;

import com.postamedica.rest.infraestructure.persistence.entities.EspecialistaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataEspecialistaRepository extends JpaRepository<EspecialistaEntity, Long> {

    Optional<EspecialistaEntity> findByCorreo(String correo);

    Optional<EspecialistaEntity> findByDni(String dni);

    boolean existsByCorreo(String correo);

    boolean existsByDni(String dni);

    List<EspecialistaEntity> findByEstado(String estado);
}