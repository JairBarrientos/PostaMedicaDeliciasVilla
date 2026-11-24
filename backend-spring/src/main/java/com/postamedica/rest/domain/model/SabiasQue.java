package com.postamedica.rest.domain.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SabiasQue {
    private Long id;
    private String contenido;
    private LocalDateTime creadoEn;
}