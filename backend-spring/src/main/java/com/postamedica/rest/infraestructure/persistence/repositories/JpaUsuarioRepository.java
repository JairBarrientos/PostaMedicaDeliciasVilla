package com.postamedica.rest.infraestructure.persistence.repositories;

import com.postamedica.rest.domain.model.Usuario;
import com.postamedica.rest.domain.repository.UsuarioRepository;
import com.postamedica.rest.infraestructure.persistence.entities.UsuarioEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaUsuarioRepository implements UsuarioRepository {

    private final SpringDataUsuarioRepository springDataRepo;

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioEntity entity = UsuarioEntity.fromDomain(usuario);
        return springDataRepo.save(entity).toDomain();
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return springDataRepo.findById(id).map(UsuarioEntity::toDomain);
    }

    @Override
    public Optional<Usuario> findByDni(String dni) {
        return springDataRepo.findByDni(dni).map(UsuarioEntity::toDomain);
    }

    @Override
    public Optional<Usuario> findByCorreo(String correo) {
        return springDataRepo.findByCorreo(correo).map(UsuarioEntity::toDomain);
    }

    @Override
    public Optional<Usuario> findByTelefono(String telefono) {
        return springDataRepo.findByTelefono(telefono).map(UsuarioEntity::toDomain);
    }

    @Override
    public List<Usuario> findAll() {
        return springDataRepo.findAll().stream()
                .map(UsuarioEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Usuario> findByRol(String rol) {
        return springDataRepo.findByRol(rol).stream()
                .map(UsuarioEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Usuario> findByEstado(String estado) {
        return springDataRepo.findByEstado(estado).stream()
                .map(UsuarioEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByDni(String dni) {
        return springDataRepo.existsByDni(dni);
    }

    @Override
    public boolean existsByCorreo(String correo) {
        return springDataRepo.existsByCorreo(correo);
    }

    @Override
    public boolean existsByTelefono(String telefono) {
        return springDataRepo.existsByTelefono(telefono);
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