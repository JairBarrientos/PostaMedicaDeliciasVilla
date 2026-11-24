package com.postamedica.rest.domain.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private Long id;
    private String nombreCompleto;
    private String correo;
    private String dni;
    private String rol;          // PACIENTE / ESPECIALISTA / ADMIN
    private String estado;
    private String especialidad; // solo si es ESPECIALISTA
}