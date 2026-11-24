package com.postamedica.rest.domain.dtos;

import com.postamedica.rest.domain.model.CitaMedica;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@Builder
public class CitaMedicaResponse {

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

    public static CitaMedicaResponse fromDomain(CitaMedica c) {
        if (c == null) return null;
        return CitaMedicaResponse.builder()
                .id(c.getId())
                .fecha(c.getFecha())
                .hora(c.getHora())
                .codigoCita(c.getCodigoCita())
                .estado(c.getEstado())
                .pacienteId(c.getPacienteId())
                .pacienteNombre(c.getPacienteNombre())
                .pacienteDni(c.getPacienteDni())
                .especialistaId(c.getEspecialistaId())
                .especialistaNombre(c.getEspecialistaNombre())
                .especialistaEspecialidad(c.getEspecialistaEspecialidad())
                .mensajeCancelacion(c.getMensajeCancelacion())
                .fechaCreacion(c.getFechaCreacion())
                .build();
    }
}