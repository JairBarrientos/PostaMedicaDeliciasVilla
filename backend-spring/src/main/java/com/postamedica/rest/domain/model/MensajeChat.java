package com.postamedica.rest.domain.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MensajeChat {
    private Long id;
    private Long pacienteId;
    private Long especialistaId;
    private String remitenteRol;  // PACIENTE o ESPECIALISTA
    private String contenido;
    private LocalDateTime fechaEnvio;
    private Boolean leido;
}