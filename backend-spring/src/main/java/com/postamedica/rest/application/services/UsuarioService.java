package com.postamedica.rest.application.services;

import com.postamedica.rest.domain.dtos.LoginRequest;
import com.postamedica.rest.domain.dtos.LoginResponse;
import com.postamedica.rest.domain.dtos.RegistrarPacienteRequest;
import com.postamedica.rest.domain.model.Usuario;
import com.postamedica.rest.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private static final int MAX_INTENTOS = 3;
    private static final int MESES_ESPERA = 1;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder   passwordEncoder;

    // =====================================================
    // REGISTRO DE PACIENTE
    // Búsqueda por DNI únicamente.
    // Si existe con RECHAZADO → reactivar (respetando límites).
    // =====================================================
    public Usuario registrarPaciente(RegistrarPacienteRequest request) {

        Optional<Usuario> porDni = usuarioRepository.findByDni(request.getDni());

        if (porDni.isPresent()) {
            Usuario existente = porDni.get();
            if ("RECHAZADO".equalsIgnoreCase(existente.getEstado())) {
                return reactivarPaciente(existente, request);
            }
            throw new RuntimeException("Ya existe un usuario registrado con ese DNI");
        }

        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new RuntimeException("Ya existe un usuario con ese correo");
        }

        if (request.getTelefono() != null && usuarioRepository.existsByTelefono(request.getTelefono())) {
            throw new RuntimeException("Ya existe un usuario con ese número de teléfono");
        }


        Usuario usuario = new Usuario();
        usuario.setNombres(request.getNombres());
        usuario.setApellidos(request.getApellidos());
        usuario.setDni(request.getDni());
        usuario.setCorreo(request.getCorreo());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setTelefono(request.getTelefono());
        usuario.setRol("PACIENTE");
        usuario.setEstado("PENDIENTE");
        usuario.setAfiliadoSis(request.getAfiliadoSis() != null && request.getAfiliadoSis());
        usuario.setIntentosRegistro(0);
        usuario.setFechaUltimoRechazo(null);

        return usuarioRepository.save(usuario);
    }

    // ── Reactivar paciente rechazado ──────────────────────────────────────
    private Usuario reactivarPaciente(Usuario existente, RegistrarPacienteRequest request) {
        int intentos = existente.getIntentosRegistro() != null ? existente.getIntentosRegistro() : 0;

        // Si agotó los 3 intentos, verificar si pasó 1 mes
        if (intentos >= MAX_INTENTOS) {
            LocalDateTime fechaRechazo = existente.getFechaUltimoRechazo();
            if (fechaRechazo != null && fechaRechazo.plusMonths(MESES_ESPERA).isAfter(LocalDateTime.now())) {
                long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(
                    LocalDateTime.now(), fechaRechazo.plusMonths(MESES_ESPERA));
                throw new RuntimeException("BLOQUEADO:" + diasRestantes);
            }
            // Pasó el mes → resetear contador
            intentos = 0;
        }

        // Verificar si el nuevo correo ya lo usa OTRO usuario
        Optional<Usuario> porCorreo = usuarioRepository.findByCorreo(request.getCorreo());
        if (porCorreo.isPresent() && !porCorreo.get().getId().equals(existente.getId())) {
            throw new RuntimeException("Ya existe un usuario con ese correo");
        }

        // Verificar si el nuevo teléfono ya lo usa OTRO usuario
        if (request.getTelefono() != null) {
            Optional<Usuario> porTelefono = usuarioRepository.findByTelefono(request.getTelefono());
            if (porTelefono.isPresent() && !porTelefono.get().getId().equals(existente.getId())) {
                throw new RuntimeException("Ya existe un usuario con ese número de teléfono");
            }
        }

        // Actualizar datos (el DNI no cambia)
        existente.setNombres(request.getNombres());
        existente.setApellidos(request.getApellidos());
        existente.setCorreo(request.getCorreo());
        existente.setPassword(passwordEncoder.encode(request.getPassword()));
        existente.setTelefono(request.getTelefono());
        existente.setAfiliadoSis(request.getAfiliadoSis() != null && request.getAfiliadoSis());
        existente.setEstado("PENDIENTE");
        existente.setIntentosRegistro(intentos);

        return usuarioRepository.save(existente);
    }

    // =====================================================
    // LOGIN
    // =====================================================
    @Transactional(readOnly = true)
    public Optional<LoginResponse> login(LoginRequest request) {

        String identificador = request.getIdentificador().trim();

        Optional<Usuario> usuarioOpt = identificador.contains("@")
                ? usuarioRepository.findByCorreo(identificador)
                : usuarioRepository.findByDni(identificador);

        if (usuarioOpt.isEmpty()) return Optional.empty();

        Usuario usuario = usuarioOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            return Optional.empty();
        }

        if ("PACIENTE".equalsIgnoreCase(usuario.getRol())) {
            if ("RECHAZADO".equalsIgnoreCase(usuario.getEstado())) {
                int intentos  = usuario.getIntentosRegistro() != null ? usuario.getIntentosRegistro() : 0;
                int restantes = Math.max(0, MAX_INTENTOS - intentos);
                LocalDateTime fechaRechazo = usuario.getFechaUltimoRechazo();

                if (intentos >= MAX_INTENTOS && fechaRechazo != null
                        && fechaRechazo.plusMonths(MESES_ESPERA).isAfter(LocalDateTime.now())) {
                    long dias = java.time.temporal.ChronoUnit.DAYS.between(
                        LocalDateTime.now(), fechaRechazo.plusMonths(MESES_ESPERA));
                    throw new RuntimeException("BLOQUEADO:" + dias);
                }
                throw new RuntimeException("RECHAZADO:" + restantes);
            }
            if (!usuario.isAprobado()) {
                throw new RuntimeException("Tu cuenta está pendiente de aprobación por el administrador.");
            }
        }

        LoginResponse response = LoginResponse.builder()
                .id(usuario.getId())
                .nombreCompleto(usuario.getNombreCompleto())
                .correo(usuario.getCorreo())
                .dni(usuario.getDni())
                .rol(usuario.getRol())
                .estado(usuario.getEstado())
                .build();

        return Optional.of(response);
    }

    // =====================================================
    // APROBAR / RECHAZAR
    // =====================================================
    public Usuario aprobarPaciente(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (!"PACIENTE".equalsIgnoreCase(usuario.getRol()))
            throw new RuntimeException("Solo se pueden aprobar pacientes");
        usuario.setEstado("APROBADO");
        return usuarioRepository.save(usuario);
    }

    public Usuario rechazarPaciente(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (!"PACIENTE".equalsIgnoreCase(usuario.getRol()))
            throw new RuntimeException("Solo se pueden rechazar pacientes");
        int intentos = usuario.getIntentosRegistro() != null ? usuario.getIntentosRegistro() : 0;
        usuario.setEstado("RECHAZADO");
        usuario.setIntentosRegistro(intentos + 1);
        usuario.setFechaUltimoRechazo(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    // =====================================================
    // CONSULTAS
    // =====================================================
    @Transactional(readOnly = true)
    public List<Usuario> findAll() { return usuarioRepository.findAll(); }

    @Transactional(readOnly = true)
    public List<Usuario> findByRol(String rol) { return usuarioRepository.findByRol(rol); }

    @Transactional(readOnly = true)
    public List<Usuario> findByEstado(String estado) { return usuarioRepository.findByEstado(estado); }

    @Transactional(readOnly = true)
    public Optional<Usuario> findById(Long id) { return usuarioRepository.findById(id); }

    public void deleteById(Long id) {
        if (!usuarioRepository.existsById(id))
            throw new RuntimeException("Usuario no encontrado con id: " + id);
        usuarioRepository.deleteById(id);
    }
}