package com.postamedica.rest.domain.repository;

import com.postamedica.rest.domain.model.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {
    Usuario save(Usuario usuario);
    Optional<Usuario> findById(Long id);
    Optional<Usuario> findByDni(String dni);
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByTelefono(String telefono);
    List<Usuario> findAll();
    List<Usuario> findByRol(String rol);
    List<Usuario> findByEstado(String estado);
    boolean existsByDni(String dni);
    boolean existsByCorreo(String correo);
    boolean existsByTelefono(String telefono);
    boolean existsById(Long id);
    void deleteById(Long id);
}