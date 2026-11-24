package com.postamedica.rest.infraestructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "feriados")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeriadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha", nullable = false, unique = true)
    private LocalDate fecha;

    @Column(name = "motivo", length = 200)
    private String motivo;
}