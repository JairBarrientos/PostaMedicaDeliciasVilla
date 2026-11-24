package com.postamedica.rest.infraestructure.persistence.entities;

import com.postamedica.rest.domain.model.CitaMedica;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "citas_medicas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitaMedicaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora", nullable = false)
    private LocalTime hora;

    @Column(name = "codigo_cita", nullable = false, unique = true, length = 30)
    private String codigoCita;

    // POR_CONFIRMAR / PROGRAMADA / CANCELADA / ATENDIDA / NO_ASISTIDA
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    @Column(name = "especialista_id", nullable = false)
    private Long especialistaId;

    @Column(name = "mensaje_cancelacion", length = 500)
    private String mensajeCancelacion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    // -----------------------------------------------
    // Mapeo: Entity → Domain
    // -----------------------------------------------
    public CitaMedica toDomain() {
        CitaMedica c = new CitaMedica();
        c.setId(this.id);
        c.setFecha(this.fecha);
        c.setHora(this.hora);
        c.setCodigoCita(this.codigoCita);
        c.setEstado(this.estado);
        c.setPacienteId(this.pacienteId);
        c.setEspecialistaId(this.especialistaId);
        c.setMensajeCancelacion(this.mensajeCancelacion);
        c.setFechaCreacion(this.fechaCreacion);
        return c;
    }

    // -----------------------------------------------
    // Mapeo: Domain → Entity
    // -----------------------------------------------
    public static CitaMedicaEntity fromDomain(CitaMedica c) {
        if (c == null) return null;
        return CitaMedicaEntity.builder()
                .id(c.getId())
                .fecha(c.getFecha())
                .hora(c.getHora())
                .codigoCita(c.getCodigoCita())
                .estado(c.getEstado())
                .pacienteId(c.getPacienteId())
                .especialistaId(c.getEspecialistaId())
                .mensajeCancelacion(c.getMensajeCancelacion())
                .fechaCreacion(c.getFechaCreacion())
                .build();
    }
}