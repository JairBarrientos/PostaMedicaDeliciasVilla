package com.postamedica.rest.infraestructure.persistence.entities;

import com.postamedica.rest.domain.model.GaleriaPortada;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "galeria_portada")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GaleriaPortadaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ruta_imagen", nullable = false, length = 512)
    private String rutaImagen;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    public GaleriaPortada toDomain() {
        GaleriaPortada g = new GaleriaPortada();
        g.setId(this.id);
        g.setRutaImagen(this.rutaImagen);
        g.setOrden(this.orden);
        g.setCreadoEn(this.creadoEn);
        return g;
    }

    public static GaleriaPortadaEntity fromDomain(GaleriaPortada g) {
        if (g == null) return null;
        return GaleriaPortadaEntity.builder()
                .id(g.getId())
                .rutaImagen(g.getRutaImagen())
                .orden(g.getOrden())
                .creadoEn(g.getCreadoEn())
                .build();
    }
}