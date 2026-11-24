package com.postamedica.rest.infraestructure.web;

import com.postamedica.rest.application.services.EspecialistaService;
import com.postamedica.rest.domain.dtos.CrearEspecialistaRequest;
import com.postamedica.rest.domain.dtos.EspecialistaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/especialistas")
@RequiredArgsConstructor
public class EspecialistaController {

    private final EspecialistaService especialistaService;

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody CrearEspecialistaRequest request) {
        try {
            EspecialistaResponse response = EspecialistaResponse
                    .fromDomain(especialistaService.crear(request));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<EspecialistaResponse>> listarTodos() {
        List<EspecialistaResponse> lista = especialistaService.findAll().stream()
                .map(EspecialistaResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/activos")
    public ResponseEntity<List<EspecialistaResponse>> listarActivos() {
        List<EspecialistaResponse> lista = especialistaService.findActivos().stream()
                .map(EspecialistaResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        return especialistaService.findById(id)
                .map(EspecialistaResponse::fromDomain)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/foto")
    public ResponseEntity<?> subirFoto(
            @PathVariable Long id,
            @RequestParam("archivo") MultipartFile archivo) {
        try {
            String fotoUrl = especialistaService.actualizarFoto(id, archivo);
            return ResponseEntity.ok(Map.of("fotoUrl", fotoUrl));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir imagen");
        }
    }

    @PatchMapping("/{id}/celular")
    public ResponseEntity<?> actualizarCelular(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            especialistaService.actualizarTelefono(id, body.get("telefono"));
            return ResponseEntity.ok("Celular actualizado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            EspecialistaResponse response = EspecialistaResponse
                    .fromDomain(especialistaService.cambiarEstado(id, body.get("estado")));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            especialistaService.deleteById(id);
            return ResponseEntity.ok("Especialista eliminado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}