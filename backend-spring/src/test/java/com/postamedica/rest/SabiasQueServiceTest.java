package com.postamedica.rest;

import com.postamedica.rest.application.services.SabiasQueService;
import com.postamedica.rest.domain.model.SabiasQue;
import com.postamedica.rest.domain.repository.SabiasQueRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SabiasQueServiceTest {

    @Mock
    private SabiasQueRepository sabiasQueRepository;

    @InjectMocks
    private SabiasQueService sabiasQueService;

    @Test
    @DisplayName("Crear dato exitosamente con contenido válido")
    void crear_exitoso() {
        when(sabiasQueRepository.count()).thenReturn(3L);
        SabiasQue guardado = new SabiasQue();
        guardado.setId(1L);
        guardado.setContenido("Caminar 30 minutos al día mejora tu salud.");
        when(sabiasQueRepository.save(any())).thenReturn(guardado);

        SabiasQue resultado = sabiasQueService.crear("Caminar 30 minutos al día mejora tu salud.");

        assertNotNull(resultado);
        assertEquals("Caminar 30 minutos al día mejora tu salud.", resultado.getContenido());
        verify(sabiasQueRepository).save(any());
    }

    @Test
    @DisplayName("Crear falla si el contenido está vacío")
    void crear_contenidoVacio_lanzaExcepcion() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> sabiasQueService.crear(""));

        assertEquals("El contenido no puede estar vacío", ex.getMessage());
        verify(sabiasQueRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear falla si el contenido supera 80 caracteres")
    void crear_contenidoMayor80_lanzaExcepcion() {
        String textoLargo = "Este texto tiene más de ochenta caracteres y debe ser rechazado por el sistema de validación.";

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> sabiasQueService.crear(textoLargo));

        assertEquals("El contenido no puede superar 80 caracteres", ex.getMessage());
        verify(sabiasQueRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear falla si ya existen 10 datos registrados")
    void crear_limiteAlcanzado_lanzaExcepcion() {
        when(sabiasQueRepository.count()).thenReturn(10L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> sabiasQueService.crear("Contenido válido menor a 80 chars."));

        assertTrue(ex.getMessage().contains("Ya tienes 10 datos"));
        verify(sabiasQueRepository, never()).save(any());
    }

    @Test
    @DisplayName("Eliminar dato existente exitosamente")
    void eliminar_exitoso() {
        when(sabiasQueRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> sabiasQueService.eliminar(1L));

        verify(sabiasQueRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Eliminar falla si el dato no existe")
    void eliminar_noExiste_lanzaExcepcion() {
        when(sabiasQueRepository.existsById(99L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> sabiasQueService.eliminar(99L));

        assertTrue(ex.getMessage().contains("no encontrado"));
    }
}