package com.postamedica.rest.domain.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Usuario {
    private Long id;
    private String nombres;
    private String apellidos;
    private String nombreCompleto;
    private String dni;
    private String correo;
    private String password;
    private String telefono;
    private String rol;
    private String estado;
    private Boolean afiliadoSis;
    private Integer intentosRegistro;
    private LocalDateTime fechaUltimoRechazo;

    public String getNombreCompleto() {
        if (nombres != null && apellidos != null) {
            return nombres.trim() + " " + apellidos.trim();
        }
        return nombreCompleto;
    }

    public boolean isAprobado() {
        return "APROBADO".equalsIgnoreCase(this.estado);
    }
}