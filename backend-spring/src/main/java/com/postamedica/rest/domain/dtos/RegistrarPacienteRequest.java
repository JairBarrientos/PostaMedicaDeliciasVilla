package com.postamedica.rest.domain.dtos;

import lombok.Data;

@Data
public class RegistrarPacienteRequest {
    private String nombres;
    private String apellidos;
    private String dni;
    private String correo;
    private String password;
    private String telefono;
    private Boolean afiliadoSis;
}