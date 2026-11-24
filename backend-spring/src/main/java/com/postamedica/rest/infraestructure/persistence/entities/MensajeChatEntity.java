package com.postamedica.rest.infraestructure.persistence.entities;

import com.postamedica.rest.domain.model.MensajeChat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes_chat")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeChatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    @Column(name = "especialista_id", nullable = false)
    private Long especialistaId;

    // Quién envió el mensaje: PACIENTE o ESPECIALISTA
    @Column(name = "remitente_rol", nullable = false, length = 20)
    private String remitenteRol;

    @Column(name = "contenido", nullable = false, length = 1000)
    private String contenido;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

    @Column(name = "leido")
    private Boolean leido;

    // -----------------------------------------------
    // Mapeo: Entity → Domain
    // -----------------------------------------------
    public MensajeChat toDomain() {
        MensajeChat m = new MensajeChat();
        m.setId(this.id);
        m.setPacienteId(this.pacienteId);
        m.setEspecialistaId(this.especialistaId);
        m.setRemitenteRol(this.remitenteRol);
        m.setContenido(this.contenido);
        m.setFechaEnvio(this.fechaEnvio);
        m.setLeido(this.leido);
        return m;
    }

    // -----------------------------------------------
    // Mapeo: Domain → Entity
    // -----------------------------------------------
    public static MensajeChatEntity fromDomain(MensajeChat m) {
        if (m == null) return null;
        return MensajeChatEntity.builder()
                .id(m.getId())
                .pacienteId(m.getPacienteId())
                .especialistaId(m.getEspecialistaId())
                .remitenteRol(m.getRemitenteRol())
                .contenido(m.getContenido())
                .fechaEnvio(m.getFechaEnvio())
                .leido(m.getLeido())
                .build();
    }
}