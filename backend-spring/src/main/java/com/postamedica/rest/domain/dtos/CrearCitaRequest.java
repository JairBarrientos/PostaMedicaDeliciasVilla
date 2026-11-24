package com.postamedica.rest.domain.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CrearCitaRequest {
    private Long pacienteId;
    private Long especialistaId;
    private LocalDate fecha;
    private LocalTime hora;
}