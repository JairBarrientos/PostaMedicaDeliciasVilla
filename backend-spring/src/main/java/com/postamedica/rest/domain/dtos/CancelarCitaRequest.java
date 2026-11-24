package com.postamedica.rest.domain.dtos;

import lombok.Data;
 
@Data
public class CancelarCitaRequest {
    // Mensaje del especialista al paciente explicando la cancelación
    private String mensaje;
}
 
