package com.postamedica.rest.infraestructure.persistence.entities;

import com.postamedica.rest.domain.model.SabiasQue;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sabias_que")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SabiasQueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contenido", nullable = false, length = 80)
    private String contenido;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    public SabiasQue toDomain() {
        SabiasQue s = new SabiasQue();
        s.setId(this.id);
        s.setContenido(this.contenido);
        s.setCreadoEn(this.creadoEn);
        return s;
    }

    public static SabiasQueEntity fromDomain(SabiasQue s) {
        if (s == null) return null;
        return SabiasQueEntity.builder()
                .id(s.getId())
                .contenido(s.getContenido())
                .creadoEn(s.getCreadoEn())
                .build();
    }
}