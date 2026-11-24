package com.postamedica.rest.infraestructure.web;

import com.postamedica.rest.application.services.GaleriaPortadaService;
import com.postamedica.rest.domain.model.GaleriaPortada;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/galeria-portada")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class GaleriaPortadaController {

    private final GaleriaPortadaService galeriaPortadaService;

    // GET /api/galeria-portada
    @GetMapping
    public ResponseEntity<List<GaleriaPortada>> listar() {
        return ResponseEntity.ok(galeriaPortadaService.listar());
    }

    // POST /api/galeria-portada
    @PostMapping
    public ResponseEntity<?> subir(
            @RequestParam("orden") Integer orden,
            @RequestParam("archivo") MultipartFile archivo) {
        try {
            GaleriaPortada nueva = galeriaPortadaService.subirImagen(orden, archivo);
            return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir imagen");
        }
    }

    // DELETE /api/galeria-portada/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            galeriaPortadaService.eliminar(id);
            return ResponseEntity.ok("Imagen eliminada correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar imagen");
        }
    }
}