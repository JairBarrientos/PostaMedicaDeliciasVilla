package com.postamedica.rest.infraestructure.web;

import com.postamedica.rest.application.services.CitaService;
import com.postamedica.rest.domain.dtos.CancelarCitaRequest;
import com.postamedica.rest.domain.dtos.CitaMedicaResponse;
import com.postamedica.rest.domain.dtos.CrearCitaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    // Paciente agenda → queda en POR_CONFIRMAR
    @PostMapping
    public ResponseEntity<?> agendar(@RequestBody CrearCitaRequest request) {
        try {
            CitaMedicaResponse response = CitaMedicaResponse.fromDomain(citaService.agendar(request));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Especialista acepta → POR_CONFIRMAR → PROGRAMADA
    @PutMapping("/{id}/aceptar")
    public ResponseEntity<?> aceptar(@PathVariable Long id) {
        try {
            CitaMedicaResponse response = CitaMedicaResponse.fromDomain(citaService.aceptar(id));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Especialista rechaza → CANCELADA (con mensaje)
    @PutMapping("/{id}/rechazar")
    public ResponseEntity<?> rechazar(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            CancelarCitaRequest req = new CancelarCitaRequest();
            req.setMensaje(body.getOrDefault("mensaje", "Cita rechazada por el especialista"));
            CitaMedicaResponse response = CitaMedicaResponse.fromDomain(citaService.cancelar(id, req));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Especialista cancela → CANCELADA (con mensaje)
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable Long id, @RequestBody CancelarCitaRequest request) {
        try {
            CitaMedicaResponse response = CitaMedicaResponse.fromDomain(citaService.cancelar(id, request));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Especialista marca ATENDIDA o NO_ASISTIDA (solo para PROGRAMADA)
    @PutMapping("/{id}/estado/{nuevoEstado}")
    public ResponseEntity<?> actualizarEstado(@PathVariable Long id, @PathVariable String nuevoEstado) {
        try {
            CitaMedicaResponse response = CitaMedicaResponse.fromDomain(citaService.actualizarEstado(id, nuevoEstado.toUpperCase()));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<CitaMedicaResponse>> listarTodas() {
        return ResponseEntity.ok(citaService.listarTodas().stream().map(CitaMedicaResponse::fromDomain).collect(Collectors.toList()));
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<CitaMedicaResponse>> listarPorPaciente(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(citaService.listarPorPaciente(pacienteId).stream().map(CitaMedicaResponse::fromDomain).collect(Collectors.toList()));
    }

    @GetMapping("/especialista/{especialistaId}")
    public ResponseEntity<List<CitaMedicaResponse>> listarPorEspecialista(@PathVariable Long especialistaId) {
        return ResponseEntity.ok(citaService.listarPorEspecialista(especialistaId).stream().map(CitaMedicaResponse::fromDomain).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        return citaService.findById(id).map(CitaMedicaResponse::fromDomain).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}