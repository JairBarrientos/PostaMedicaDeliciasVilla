package com.postamedica.rest.application.services;

import com.postamedica.rest.domain.model.GaleriaPortada;
import com.postamedica.rest.domain.repository.GaleriaPortadaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GaleriaPortadaService {

    private final GaleriaPortadaRepository galeriaPortadaRepository;

    private static final int LIMITE_MAXIMO = 5;
    private static final long MAX_BYTES    = 2 * 1024 * 1024; // 2 MB
    private static final List<String> TIPOS_PERMITIDOS = List.of(
            "image/jpeg", "image/png", "image/webp"
    );

    public GaleriaPortada subirImagen(Integer orden, MultipartFile archivo) throws IOException {

        // Validar límite de 5 imágenes
        if (galeriaPortadaRepository.count() >= LIMITE_MAXIMO) {
            throw new RuntimeException("Ya tienes 5 imágenes en la galería. Elimina una para agregar otra");
        }

        // Validar que el orden no esté ocupado
        if (galeriaPortadaRepository.existsByOrden(orden)) {
            throw new RuntimeException("Ya existe una imagen con el orden " + orden + ". Elige otro");
        }

        // Validar orden entre 1 y 5
        if (orden < 1 || orden > 5) {
            throw new RuntimeException("El orden debe ser entre 1 y 5");
        }

        // Validar tipo de archivo
        String contentType = archivo.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType)) {
            throw new RuntimeException("Solo se permiten imágenes JPG, PNG o WEBP");
        }

        // Validar tamaño máximo 2MB
        if (archivo.getSize() > MAX_BYTES) {
            throw new RuntimeException("La imagen no puede superar 2MB");
        }

        // Guardar archivo en uploads/
        Path uploadsDir = Paths.get("uploads");
        if (!Files.exists(uploadsDir)) {
            Files.createDirectories(uploadsDir);
        }

        String nombreArchivo = UUID.randomUUID() + "_" + archivo.getOriginalFilename();
        Path destino = uploadsDir.resolve(nombreArchivo);
        Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        GaleriaPortada nueva = new GaleriaPortada();
        nueva.setRutaImagen("/uploads/" + nombreArchivo);
        nueva.setOrden(orden);
        nueva.setCreadoEn(LocalDateTime.now());

        return galeriaPortadaRepository.save(nueva);
    }

    @Transactional(readOnly = true)
    public List<GaleriaPortada> listar() {
        return galeriaPortadaRepository.findAllOrdenado();
    }

    public void eliminar(Long id) throws IOException {
        GaleriaPortada imagen = galeriaPortadaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada con id: " + id));

        // Eliminar archivo físico del servidor
        String rutaRelativa = imagen.getRutaImagen().replaceFirst("^/", "");
        Path archivo = Paths.get(rutaRelativa);
        if (Files.exists(archivo)) {
            Files.delete(archivo);
        }

        galeriaPortadaRepository.deleteById(id);
    }
}