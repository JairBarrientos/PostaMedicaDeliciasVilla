package com.postamedica.rest.infraestructure.persistence.repositories;

import com.postamedica.rest.infraestructure.persistence.entities.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataUsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByDni(String dni);

    Optional<UsuarioEntity> findByCorreo(String correo);

    List<UsuarioEntity> findByRol(String rol);

    List<UsuarioEntity> findByEstado(String estado);

    boolean existsByDni(String dni);

    boolean existsByCorreo(String correo);

    boolean existsByTelefono(String telefono);
    Optional<UsuarioEntity> findByTelefono(String telefono);
}