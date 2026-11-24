package com.postamedica.rest.domain.model;

import lombok.Data;

@Data
public class Especialista {
    private Long id;
    private String nombres;
    private String apellidos;
    private String nombreCompleto;
    private String especialidad;
    private String correo;
    private String password;
    private String dni;
    private String telefono;
    private String foto;
    private String estado;

    // Concatena nombres + apellidos automáticamente
    public String getNombreCompleto() {
        if (nombres != null && apellidos != null) {
            return nombres.trim() + " " + apellidos.trim();
        }
        return nombreCompleto;
    }
}