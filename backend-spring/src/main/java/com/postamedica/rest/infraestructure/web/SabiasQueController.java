package com.postamedica.rest.infraestructure.web;

import com.postamedica.rest.application.services.SabiasQueService;
import com.postamedica.rest.domain.model.SabiasQue;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sabias-que")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SabiasQueController {

    private final SabiasQueService sabiasQueService;

    // GET /api/sabias-que
    @GetMapping
    public ResponseEntity<List<SabiasQue>> listar() {
        return ResponseEntity.ok(sabiasQueService.listar());
    }

    // POST /api/sabias-que
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, String> body) {
        try {
            String contenido = body.get("contenido");
            SabiasQue nuevo = sabiasQueService.crear(contenido);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // DELETE /api/sabias-que/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            sabiasQueService.eliminar(id);
            return ResponseEntity.ok("Eliminado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}