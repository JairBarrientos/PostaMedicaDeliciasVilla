package com.postamedica.rest.domain.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

import com.postamedica.rest.domain.model.MensajeChat;

@Data
@Builder
public class MensajeChatResponse {

    private Long id;
    private Long pacienteId;
    private Long especialistaId;
    private String remitenteRol;
    private String contenido;
    private LocalDateTime fechaEnvio;
    private Boolean leido;

    public static MensajeChatResponse fromDomain(MensajeChat m) {
        if (m == null) return null;
        return MensajeChatResponse.builder()
                .id(m.getId())
                .pacienteId(m.getPacienteId())
                .especialistaId(m.getEspecialistaId())
                .remitenteRol(m.getRemitenteRol())
                .contenido(m.getContenido())
                .fechaEnvio(m.getFechaEnvio())
                .leido(m.getLeido())
                .build();
    }
}