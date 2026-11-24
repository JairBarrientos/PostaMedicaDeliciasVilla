package com.postamedica.rest.infraestructure.persistence.entities;

import com.postamedica.rest.domain.model.Especialista;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "especialistas", uniqueConstraints = {
        @UniqueConstraint(columnNames = "dni"),
        @UniqueConstraint(columnNames = "correo")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EspecialistaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombres", nullable = false, length = 25)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 25)
    private String apellidos;

    @Column(name = "especialidad", nullable = false, length = 60)
    private String especialidad;

    @Column(name = "correo", nullable = false, unique = true)
    private String correo;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "dni", nullable = false, unique = true, length = 8)
    private String dni;

    @Column(name = "telefono", length = 9)
    private String telefono;

    @Column(name = "foto", length = 225)
    private String foto;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    // -----------------------------------------------
    // Mapeo: Entity → Domain
    // -----------------------------------------------
    public Especialista toDomain() {
        Especialista e = new Especialista();
        e.setId(this.id);
        e.setNombres(this.nombres);
        e.setApellidos(this.apellidos);
        e.setEspecialidad(this.especialidad);
        e.setCorreo(this.correo);
        e.setPassword(this.password);
        e.setDni(this.dni);
        e.setTelefono(this.telefono);
        e.setFoto(this.foto);
        e.setEstado(this.estado);
        return e;
    }

    // -----------------------------------------------
    // Mapeo: Domain → Entity
    // -----------------------------------------------
    public static EspecialistaEntity fromDomain(Especialista e) {
        if (e == null) return null;
        return EspecialistaEntity.builder()
                .id(e.getId())
                .nombres(e.getNombres())
                .apellidos(e.getApellidos())
                .especialidad(e.getEspecialidad())
                .correo(e.getCorreo())
                .password(e.getPassword())
                .dni(e.getDni())
                .telefono(e.getTelefono())
                .foto(e.getFoto())
                .estado(e.getEstado())
                .build();
    }
}