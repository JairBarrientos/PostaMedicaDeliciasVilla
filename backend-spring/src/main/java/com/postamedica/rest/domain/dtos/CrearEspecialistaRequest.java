package com.postamedica.rest.domain.dtos;

import lombok.Data;

@Data
public class CrearEspecialistaRequest {
    private String nombres;
    private String apellidos;
    private String especialidad;
    private String correo;
    private String password;
    private String dni;
    private String telefono;
}