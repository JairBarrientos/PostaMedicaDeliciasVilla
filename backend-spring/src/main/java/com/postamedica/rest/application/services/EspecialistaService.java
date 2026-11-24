package com.postamedica.rest.application.services;

import com.postamedica.rest.domain.dtos.CrearEspecialistaRequest;
import com.postamedica.rest.domain.dtos.LoginRequest;
import com.postamedica.rest.domain.dtos.LoginResponse;
import com.postamedica.rest.domain.model.Especialista;
import com.postamedica.rest.domain.repository.EspecialistaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EspecialistaService {

    private final EspecialistaRepository especialistaRepository;
    private final PasswordEncoder passwordEncoder;

    // =====================================================
    // CREAR ESPECIALISTA (solo admin)
    // =====================================================
    public Especialista crear(CrearEspecialistaRequest request) {

    if (especialistaRepository.existsByDni(request.getDni())) {
        throw new RuntimeException("Ya existe un especialista con ese DNI");
    }
    if (especialistaRepository.existsByCorreo(request.getCorreo())) {
        throw new RuntimeException("Ya existe un especialista con ese correo");
    }

    Especialista especialista = new Especialista();
    especialista.setNombres(request.getNombres());
    especialista.setApellidos(request.getApellidos());
    especialista.setEspecialidad(request.getEspecialidad());
    especialista.setCorreo(request.getCorreo());
    especialista.setPassword(passwordEncoder.encode(request.getPassword()));
    especialista.setDni(request.getDni());
    especialista.setTelefono(request.getTelefono());
    especialista.setEstado("ACTIVO");

    return especialistaRepository.save(especialista);
}

    // =====================================================
    // SUBIR / ACTUALIZAR FOTO
    // =====================================================
    public String actualizarFoto(Long id, MultipartFile archivo) throws IOException {
        Especialista especialista = especialistaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Especialista no encontrado con id: " + id));

        // Crear directorio uploads si no existe
        Path uploadsDir = Paths.get("uploads");
        if (!Files.exists(uploadsDir)) {
            Files.createDirectories(uploadsDir);
        }

        // Guardar archivo con nombre único
        String nombreArchivo = UUID.randomUUID() + "_" + archivo.getOriginalFilename();
        Path destino = uploadsDir.resolve(nombreArchivo);
        Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        // Guardar URL relativa en la BD
        String fotoUrl = "/uploads/" + nombreArchivo;
        especialista.setFoto(fotoUrl);
        especialistaRepository.save(especialista);

        return fotoUrl;
    }

    // =====================================================
    // ACTUALIZAR CELULAR
    // =====================================================
    public void actualizarTelefono(Long id, String telefono) {
        Especialista especialista = especialistaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Especialista no encontrado con id: " + id));
        especialista.setTelefono(telefono);
        especialistaRepository.save(especialista);
    }

    // =====================================================
    // CAMBIAR ESTADO (ACTIVO / INACTIVO)
    // =====================================================
    public Especialista cambiarEstado(Long id, String nuevoEstado) {
        Especialista especialista = especialistaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Especialista no encontrado con id: " + id));
        if (!nuevoEstado.equals("ACTIVO") && !nuevoEstado.equals("INACTIVO")) {
            throw new RuntimeException("Estado inválido. Use ACTIVO o INACTIVO");
        }
        especialista.setEstado(nuevoEstado);
        return especialistaRepository.save(especialista);
    }

    // =====================================================
    // LOGIN ESPECIALISTA
    // =====================================================
    @Transactional(readOnly = true)
    public Optional<LoginResponse> login(LoginRequest request) {

        String identificador = request.getIdentificador().trim();

        Optional<Especialista> opt = identificador.contains("@")
                ? especialistaRepository.findByCorreo(identificador)
                : especialistaRepository.findByDni(identificador);

        if (opt.isEmpty()) return Optional.empty();

        Especialista especialista = opt.get();

        if (!passwordEncoder.matches(request.getPassword(), especialista.getPassword())) {
            return Optional.empty();
        }

        if (!"ACTIVO".equalsIgnoreCase(especialista.getEstado())) {
            throw new RuntimeException("Tu cuenta de especialista está inactiva.");
        }

        LoginResponse response = LoginResponse.builder()
                .id(especialista.getId())
                .nombreCompleto(especialista.getNombreCompleto())
                .correo(especialista.getCorreo())
                .dni(especialista.getDni())
                .rol("ESPECIALISTA")
                .estado(especialista.getEstado())
                .especialidad(especialista.getEspecialidad())
                .build();

        return Optional.of(response);
    }

    // =====================================================
    // CONSULTAS
    // =====================================================
    @Transactional(readOnly = true)
    public List<Especialista> findAll() {
        return especialistaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Especialista> findActivos() {
        return especialistaRepository.findByEstado("ACTIVO");
    }

    @Transactional(readOnly = true)
    public Optional<Especialista> findById(Long id) {
        return especialistaRepository.findById(id);
    }

    // =====================================================
    // ELIMINAR (solo admin)
    // =====================================================
    public void deleteById(Long id) {
        if (!especialistaRepository.existsById(id)) {
            throw new RuntimeException("Especialista no encontrado con id: " + id);
        }
        especialistaRepository.deleteById(id);
    }
}