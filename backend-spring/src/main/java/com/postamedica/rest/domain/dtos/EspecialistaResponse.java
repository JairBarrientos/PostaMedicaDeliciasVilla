package com.postamedica.rest.domain.dtos;

import com.postamedica.rest.domain.model.Especialista;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EspecialistaResponse {

    private Long id;
    private String nombreCompleto;
    private String nombres;
    private String apellidos;
    private String especialidad;
    private String correo;
    private String dni;
    private String telefono;
    private String foto;
    private String estado;

    public static EspecialistaResponse fromDomain(Especialista e) {
        if (e == null) return null;
        return EspecialistaResponse.builder()
                .id(e.getId())
                .nombreCompleto(e.getNombreCompleto())
                .nombres(e.getNombres())
                .apellidos(e.getApellidos())
                .especialidad(e.getEspecialidad())
                .correo(e.getCorreo())
                .dni(e.getDni())
                .telefono(e.getTelefono())
                .foto(e.getFoto())
                .estado(e.getEstado())
                .build();
    }
}