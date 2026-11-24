package com.postamedica.rest.domain.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GaleriaPortada {
    private Long id;
    private String rutaImagen;
    private Integer orden;
    private LocalDateTime creadoEn;
}