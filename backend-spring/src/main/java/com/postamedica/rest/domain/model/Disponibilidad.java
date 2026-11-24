package com.postamedica.rest.domain.model;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class Disponibilidad {
    private Long id;
    private Long especialistaId;
    private LocalDate fecha;
    private String motivo;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String tipo;
}