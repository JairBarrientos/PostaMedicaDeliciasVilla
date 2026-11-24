package com.postamedica.rest.infraestructure.web;

import com.postamedica.rest.application.services.EspecialistaService;
import com.postamedica.rest.application.services.UsuarioService;
import com.postamedica.rest.domain.dtos.LoginRequest;
import com.postamedica.rest.domain.dtos.LoginResponse;
import com.postamedica.rest.domain.dtos.RegistrarPacienteRequest;
import com.postamedica.rest.domain.dtos.UsuarioResponse;
import com.postamedica.rest.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final EspecialistaService especialistaService;

    // =====================================================
    // REGISTRO DE PACIENTE
    // POST /api/usuarios/registrar
    // =====================================================
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarPaciente(@RequestBody RegistrarPacienteRequest request) {
        try {
            Usuario nuevo = usuarioService.registrarPaciente(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.fromDomain(nuevo));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // =====================================================
    // LOGIN UNIFICADO (paciente, admin y especialista)
    // POST /api/usuarios/login
    // El campo 'rol' del request le dice al backend
    // en qué tabla buscar primero
    // =====================================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Si el rol indicado es ESPECIALISTA, buscar en tabla especialistas
            if ("ESPECIALISTA".equalsIgnoreCase(request.getRol())) {
                Optional<LoginResponse> respEsp = especialistaService.login(request);
                if (respEsp.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body("Credenciales incorrectas");
                }
                return ResponseEntity.ok(respEsp.get());
            }

            // Si es PACIENTE o ADMIN, buscar en tabla usuarios
            Optional<LoginResponse> respUser = usuarioService.login(request);
            if (respUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Credenciales incorrectas");
            }
            return ResponseEntity.ok(respUser.get());

        } catch (RuntimeException e) {
            // Captura mensajes como "cuenta pendiente de aprobación"
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // =====================================================
    // APROBAR PACIENTE (Admin)
    // PUT /api/usuarios/{id}/aprobar
    // =====================================================
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<?> aprobar(@PathVariable Long id) {
        try {
            Usuario u = usuarioService.aprobarPaciente(id);
            return ResponseEntity.ok(UsuarioResponse.fromDomain(u));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // =====================================================
    // RECHAZAR PACIENTE (Admin)
    // PUT /api/usuarios/{id}/rechazar
    // =====================================================
    @PutMapping("/{id}/rechazar")
    public ResponseEntity<?> rechazar(@PathVariable Long id) {
        try {
            Usuario u = usuarioService.rechazarPaciente(id);
            return ResponseEntity.ok(UsuarioResponse.fromDomain(u));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // =====================================================
    // LISTAR TODOS LOS USUARIOS (Admin)
    // GET /api/usuarios
    // =====================================================
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarTodos() {
        List<UsuarioResponse> lista = usuarioService.findAll().stream()
                .map(UsuarioResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    // =====================================================
    // LISTAR POR ROL (Admin)
    // GET /api/usuarios/rol/{rol}
    // Ejemplo: /api/usuarios/rol/PACIENTE
    // =====================================================
    @GetMapping("/rol/{rol}")
    public ResponseEntity<List<UsuarioResponse>> listarPorRol(@PathVariable String rol) {
        List<UsuarioResponse> lista = usuarioService.findByRol(rol.toUpperCase()).stream()
                .map(UsuarioResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    // =====================================================
    // LISTAR POR ESTADO (Admin)
    // GET /api/usuarios/estado/{estado}
    // Ejemplo: /api/usuarios/estado/PENDIENTE
    // =====================================================
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<UsuarioResponse>> listarPorEstado(@PathVariable String estado) {
        List<UsuarioResponse> lista = usuarioService.findByEstado(estado.toUpperCase()).stream()
                .map(UsuarioResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    // =====================================================
    // OBTENER POR ID
    // GET /api/usuarios/{id}
    // =====================================================
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        return usuarioService.findById(id)
                .map(UsuarioResponse::fromDomain)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =====================================================
    // ELIMINAR (Admin)
    // DELETE /api/usuarios/{id}
    // =====================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            usuarioService.deleteById(id);
            return ResponseEntity.ok("Usuario eliminado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}