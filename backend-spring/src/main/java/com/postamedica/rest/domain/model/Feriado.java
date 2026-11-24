package com.postamedica.rest.domain.model;

import lombok.Data;
import java.time.LocalDate;

@Data
public class Feriado {
    private Long id;
    private LocalDate fecha;
    private String motivo;
}