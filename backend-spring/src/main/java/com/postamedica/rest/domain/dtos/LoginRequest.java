package com.postamedica.rest.domain.dtos;

import lombok.Data;

/**
 * Request de login unificado para todos los roles.
 * Se puede usar DNI o correo como identificador.
 * El campo 'rol' es opcional y sirve como hint para el frontend.
 */
@Data
public class LoginRequest {
    private String identificador; // DNI o correo
    private String password;
    private String rol;           // "PACIENTE", "ESPECIALISTA" o "ADMIN" (hint del frontend)
}