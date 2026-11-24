package com.postamedica.rest.infraestructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "disponibilidad_especialista")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisponibilidadEspecialistaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "especialista_id", nullable = false)
    private Long especialistaId;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "motivo", length = 200)
    private String motivo;

    // null = bloqueo de día completo
    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    // "DIA_COMPLETO" | "RANGO_HORAS" | "FERIADO"
    @Column(name = "tipo", length = 30)
    @Builder.Default
    private String tipo = "DIA_COMPLETO";
}