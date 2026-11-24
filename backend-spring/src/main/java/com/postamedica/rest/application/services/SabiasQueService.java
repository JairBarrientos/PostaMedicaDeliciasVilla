package com.postamedica.rest.application.services;

import com.postamedica.rest.domain.model.SabiasQue;
import com.postamedica.rest.domain.repository.SabiasQueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SabiasQueService {

    private final SabiasQueRepository sabiasQueRepository;

    private static final int LIMITE_MAXIMO = 10;

    public SabiasQue crear(String contenido) {
        if (contenido == null || contenido.isBlank()) {
            throw new RuntimeException("El contenido no puede estar vacío");
        }
        if (contenido.length() > 80) {
            throw new RuntimeException("El contenido no puede superar 80 caracteres");
        }
        if (sabiasQueRepository.count() >= LIMITE_MAXIMO) {
            throw new RuntimeException("Ya tienes 10 datos en Sabías que. Elimina uno para agregar otro");
        }

        SabiasQue nuevo = new SabiasQue();
        nuevo.setContenido(contenido.trim());
        nuevo.setCreadoEn(LocalDateTime.now());
        return sabiasQueRepository.save(nuevo);
    }

    @Transactional(readOnly = true)
    public List<SabiasQue> listar() {
        return sabiasQueRepository.findAll();
    }

    public void eliminar(Long id) {
        if (!sabiasQueRepository.existsById(id)) {
            throw new RuntimeException("Dato no encontrado con id: " + id);
        }
        sabiasQueRepository.deleteById(id);
    }
}