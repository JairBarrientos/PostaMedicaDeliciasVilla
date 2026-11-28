
package com.postamedica.rest.application.events;

public record NotificacionCitaEvent(
        Long pacienteId,
        Long especialistaId,
        String texto
) {
}
