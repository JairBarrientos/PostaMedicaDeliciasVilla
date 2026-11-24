package com.postamedica.rest.domain.dtos;

import lombok.Data;

@Data
public class EnviarMensajeRequest {
    private Long pacienteId;
    private Long especialistaId;
    private String remitenteRol; // "PACIENTE" o "ESPECIALISTA"
    private String contenido;
}