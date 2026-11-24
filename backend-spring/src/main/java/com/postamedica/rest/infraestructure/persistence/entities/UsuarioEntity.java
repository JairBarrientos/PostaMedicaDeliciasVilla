package com.postamedica.rest.infraestructure.persistence.entities;

import com.postamedica.rest.domain.model.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios", uniqueConstraints = {
        @UniqueConstraint(columnNames = "dni"),
        @UniqueConstraint(columnNames = "correo")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombres", nullable = false, length = 25)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 25)
    private String apellidos;

    @Column(name = "dni", nullable = false, unique = true, length = 8)
    private String dni;

    @Column(name = "correo", nullable = false, unique = true)
    private String correo;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "telefono", length = 9, unique = true)
    private String telefono;

    @Column(name = "rol", nullable = false, length = 20)
    private String rol;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "afiliado_sis")
    private Boolean afiliadoSis;

    @Column(name = "intentos_registro")
    private Integer intentosRegistro;

    @Column(name = "fecha_ultimo_rechazo")
    private LocalDateTime fechaUltimoRechazo;

    // -----------------------------------------------
    // Mapeo: Entity → Domain
    // -----------------------------------------------
    public Usuario toDomain() {
        Usuario u = new Usuario();
        u.setId(this.id);
        u.setNombres(this.nombres);
        u.setApellidos(this.apellidos);
        u.setDni(this.dni);
        u.setCorreo(this.correo);
        u.setPassword(this.password);
        u.setTelefono(this.telefono);
        u.setRol(this.rol);
        u.setEstado(this.estado);
        u.setAfiliadoSis(this.afiliadoSis);
        u.setIntentosRegistro(this.intentosRegistro != null ? this.intentosRegistro : 0);
        u.setFechaUltimoRechazo(this.fechaUltimoRechazo);
        return u;
    }

    // -----------------------------------------------
    // Mapeo: Domain → Entity
    // -----------------------------------------------
    public static UsuarioEntity fromDomain(Usuario u) {
        if (u == null) return null;
        return UsuarioEntity.builder()
                .id(u.getId())
                .nombres(u.getNombres())
                .apellidos(u.getApellidos())
                .dni(u.getDni())
                .correo(u.getCorreo())
                .password(u.getPassword())
                .telefono(u.getTelefono())
                .rol(u.getRol())
                .estado(u.getEstado())
                .afiliadoSis(u.getAfiliadoSis())
                .intentosRegistro(u.getIntentosRegistro())
                .fechaUltimoRechazo(u.getFechaUltimoRechazo())
                .build();
    }
}