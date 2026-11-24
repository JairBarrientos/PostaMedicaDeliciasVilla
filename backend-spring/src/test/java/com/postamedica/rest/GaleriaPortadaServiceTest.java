package com.postamedica.rest;

import com.postamedica.rest.application.services.GaleriaPortadaService;
import com.postamedica.rest.domain.model.GaleriaPortada;
import com.postamedica.rest.domain.repository.GaleriaPortadaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GaleriaPortadaServiceTest {

    @Mock
    private GaleriaPortadaRepository galeriaPortadaRepository;

    @InjectMocks
    private GaleriaPortadaService galeriaPortadaService;

    @Test
    @DisplayName("Subir imagen válida exitosamente")
    void subirImagen_exitoso() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "foto.jpg", "image/jpeg",
                new byte[500 * 1024] // 500 KB
        );

        when(galeriaPortadaRepository.count()).thenReturn(2L);
        when(galeriaPortadaRepository.existsByOrden(1)).thenReturn(false);

        GaleriaPortada guardada = new GaleriaPortada();
        guardada.setId(1L);
        guardada.setOrden(1);
        when(galeriaPortadaRepository.save(any())).thenReturn(guardada);

        GaleriaPortada resultado = galeriaPortadaService.subirImagen(1, archivo);

        assertNotNull(resultado);
        assertEquals(1, resultado.getOrden());
        verify(galeriaPortadaRepository).save(any());
    }

    @Test
    @DisplayName("Subir imagen falla si supera 2MB")
    void subirImagen_mayorA2MB_lanzaExcepcion() {
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "grande.jpg", "image/jpeg",
                new byte[3 * 1024 * 1024] // 3 MB
        );

        when(galeriaPortadaRepository.count()).thenReturn(1L);
        when(galeriaPortadaRepository.existsByOrden(1)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> galeriaPortadaService.subirImagen(1, archivo));

        assertEquals("La imagen no puede superar 2MB", ex.getMessage());
        verify(galeriaPortadaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Subir imagen falla si el formato no es permitido")
    void subirImagen_formatoInvalido_lanzaExcepcion() {
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "doc.gif", "image/gif",
                new byte[100 * 1024]
        );

        when(galeriaPortadaRepository.count()).thenReturn(1L);
        when(galeriaPortadaRepository.existsByOrden(1)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> galeriaPortadaService.subirImagen(1, archivo));

        assertEquals("Solo se permiten imágenes JPG, PNG o WEBP", ex.getMessage());
    }

    @Test
    @DisplayName("Subir imagen falla si ya existen 5 imágenes")
    void subirImagen_limiteAlcanzado_lanzaExcepcion() {
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "foto.jpg", "image/jpeg",
                new byte[100 * 1024]
        );

        when(galeriaPortadaRepository.count()).thenReturn(5L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> galeriaPortadaService.subirImagen(1, archivo));

        assertTrue(ex.getMessage().contains("Ya tienes 5 imágenes"));
    }

    @Test
    @DisplayName("Subir imagen falla si el orden ya está ocupado")
    void subirImagen_ordenDuplicado_lanzaExcepcion() {
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "foto.jpg", "image/jpeg",
                new byte[100 * 1024]
        );

        when(galeriaPortadaRepository.count()).thenReturn(2L);
        when(galeriaPortadaRepository.existsByOrden(1)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> galeriaPortadaService.subirImagen(1, archivo));

        assertTrue(ex.getMessage().contains("Ya existe una imagen con el orden"));
    }
}