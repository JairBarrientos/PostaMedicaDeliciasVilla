package com.postamedica.rest.domain.dtos;

import com.postamedica.rest.domain.model.Usuario;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsuarioResponse {
    private Long id;
    private String nombreCompleto;
    private String nombres;
    private String apellidos;
    private String dni;
    private String correo;
    private String telefono;
    private String rol;
    private String estado;
    private Boolean afiliadoSis;

    public static UsuarioResponse fromDomain(Usuario u) {
        if (u == null) return null;
        return UsuarioResponse.builder()
                .id(u.getId())
                .nombreCompleto(u.getNombreCompleto())
                .nombres(u.getNombres())
                .apellidos(u.getApellidos())
                .dni(u.getDni())
                .correo(u.getCorreo())
                .telefono(u.getTelefono())
                .rol(u.getRol())
                .estado(u.getEstado())
                .afiliadoSis(u.getAfiliadoSis())
                .build();
    }
}