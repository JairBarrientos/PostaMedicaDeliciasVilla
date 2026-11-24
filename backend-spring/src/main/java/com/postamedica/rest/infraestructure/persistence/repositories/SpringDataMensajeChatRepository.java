package com.postamedica.rest.infraestructure.persistence.repositories;

import com.postamedica.rest.infraestructure.persistence.entities.MensajeChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataMensajeChatRepository extends JpaRepository<MensajeChatEntity, Long> {

    @Query("""
        SELECT m FROM MensajeChatEntity m
        WHERE m.pacienteId = :pacienteId
          AND m.especialistaId = :especialistaId
        ORDER BY m.fechaEnvio ASC
    """)
    List<MensajeChatEntity> findConversacion(
            @Param("pacienteId") Long pacienteId,
            @Param("especialistaId") Long especialistaId
    );

    // Mensajes no leídos: del especialista O del sistema
    @Query("""
        SELECT m FROM MensajeChatEntity m
        WHERE m.pacienteId = :pacienteId
          AND m.remitenteRol IN ('ESPECIALISTA', 'SISTEMA')
          AND m.leido = false
    """)
    List<MensajeChatEntity> findNoLeidosByPaciente(@Param("pacienteId") Long pacienteId);

    @Modifying
    @Query("""
        UPDATE MensajeChatEntity m
        SET m.leido = true
        WHERE m.pacienteId = :pacienteId
          AND m.especialistaId = :especialistaId
          AND m.remitenteRol IN ('ESPECIALISTA', 'SISTEMA')
          AND m.leido = false
    """)
    void marcarComoLeidos(
            @Param("pacienteId") Long pacienteId,
            @Param("especialistaId") Long especialistaId
    );

    @Query("""
        SELECT DISTINCT m.especialistaId FROM MensajeChatEntity m
        WHERE m.pacienteId = :pacienteId
          AND m.remitenteRol IN ('ESPECIALISTA', 'SISTEMA')
          AND m.leido = false
    """)
    List<Long> findEspecialistasConNoLeidos(@Param("pacienteId") Long pacienteId);
}