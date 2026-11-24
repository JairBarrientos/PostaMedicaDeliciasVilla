package com.postamedica.rest.domain.model;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
public class CitaMedica {
    private Long id;
    private LocalDate fecha;
    private LocalTime hora;
    private String codigoCita;
    private String estado;
    private Long pacienteId;
    private String pacienteNombre;
    private String pacienteDni;
    private Long especialistaId;
    private String especialistaNombre;
    private String especialistaEspecialidad;
    private String mensajeCancelacion;
    private LocalDateTime fechaCreacion;
}