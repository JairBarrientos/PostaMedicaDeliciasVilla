package com.postamedica.rest;

import com.postamedica.rest.application.services.UsuarioService;
import com.postamedica.rest.domain.dtos.LoginRequest;
import com.postamedica.rest.domain.dtos.LoginResponse;
import com.postamedica.rest.domain.dtos.RegistrarPacienteRequest;
import com.postamedica.rest.domain.model.Usuario;
import com.postamedica.rest.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private RegistrarPacienteRequest requestValido;

    @BeforeEach
    void setUp() {
        requestValido = new RegistrarPacienteRequest();
        requestValido.setNombres("Ana");
        requestValido.setApellidos("Torres Quispe");
        requestValido.setDni("74512389");
        requestValido.setCorreo("ana@gmail.com");
        requestValido.setPassword("Pass1234");
        requestValido.setTelefono("987654321");
        requestValido.setAfiliadoSis(true);
    }

    //REGISTRO

    @Test
    @DisplayName("Registro exitoso de paciente nuevo")
    void registrarPaciente_exitoso() {
        when(usuarioRepository.findByDni("74512389")).thenReturn(Optional.empty());
        when(usuarioRepository.existsByCorreo("ana@gmail.com")).thenReturn(false);
        when(usuarioRepository.existsByTelefono("987654321")).thenReturn(false);
        when(passwordEncoder.encode("Pass1234")).thenReturn("hashedPass");

        Usuario guardado = new Usuario();
        guardado.setId(1L);
        guardado.setEstado("PENDIENTE");
        guardado.setRol("PACIENTE");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(guardado);

        Usuario resultado = usuarioService.registrarPaciente(requestValido);

        assertEquals("PENDIENTE", resultado.getEstado());
        assertEquals("PACIENTE", resultado.getRol());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Registro falla si DNI ya existe con estado APROBADO")
    void registrarPaciente_dniDuplicadoAprobado_lanzaExcepcion() {
        Usuario existente = new Usuario();
        existente.setEstado("APROBADO");
        when(usuarioRepository.findByDni("74512389")).thenReturn(Optional.of(existente));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> usuarioService.registrarPaciente(requestValido));

        assertEquals("Ya existe un usuario registrado con ese DNI", ex.getMessage());
    }

    @Test
    @DisplayName("Registro falla si correo ya está en uso")
    void registrarPaciente_correoDuplicado_lanzaExcepcion() {
        when(usuarioRepository.findByDni("74512389")).thenReturn(Optional.empty());
        when(usuarioRepository.existsByCorreo("ana@gmail.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> usuarioService.registrarPaciente(requestValido));

        assertEquals("Ya existe un usuario con ese correo", ex.getMessage());
    }

    //LOGIN

    @Test
    @DisplayName("Login exitoso con DNI y contraseña correcta")
    void login_exitoso() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setDni("74512389");
        usuario.setNombres("Ana");
        usuario.setApellidos("Torres");
        usuario.setCorreo("ana@gmail.com");
        usuario.setRol("PACIENTE");
        usuario.setEstado("APROBADO");
        usuario.setPassword("hashedPass");

        LoginRequest request = new LoginRequest();
        request.setIdentificador("74512389");
        request.setPassword("Pass1234");
        request.setRol("PACIENTE");

        when(usuarioRepository.findByDni("74512389")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Pass1234", "hashedPass")).thenReturn(true);

        Optional<LoginResponse> resultado = usuarioService.login(request);

        assertTrue(resultado.isPresent());
        assertEquals("PACIENTE", resultado.get().getRol());
    }

    @Test
    @DisplayName("Login falla si la contraseña es incorrecta")
    void login_passwordIncorrecto_retornaVacio() {
        Usuario usuario = new Usuario();
        usuario.setPassword("hashedPass");
        usuario.setRol("PACIENTE");
        usuario.setEstado("APROBADO");

        LoginRequest request = new LoginRequest();
        request.setIdentificador("74512389");
        request.setPassword("wrongpass");

        when(usuarioRepository.findByDni("74512389")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("wrongpass", "hashedPass")).thenReturn(false);

        Optional<LoginResponse> resultado = usuarioService.login(request);

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Login falla si la cuenta está PENDIENTE")
    void login_cuentaPendiente_lanzaExcepcion() {
        Usuario usuario = new Usuario();
        usuario.setPassword("hashedPass");
        usuario.setRol("PACIENTE");
        usuario.setEstado("PENDIENTE");

        LoginRequest request = new LoginRequest();
        request.setIdentificador("74512389");
        request.setPassword("Pass1234");

        when(usuarioRepository.findByDni("74512389")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Pass1234", "hashedPass")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> usuarioService.login(request));

        assertTrue(ex.getMessage().contains("pendiente de aprobación"));
    }

    //APROBAR / RECHAZAR

    @Test
    @DisplayName("Aprobar paciente cambia estado a APROBADO")
    void aprobarPaciente_exitoso() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setRol("PACIENTE");
        usuario.setEstado("PENDIENTE");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any())).thenReturn(usuario);

        Usuario resultado = usuarioService.aprobarPaciente(1L);

        assertEquals("APROBADO", resultado.getEstado());
    }

    @Test
    @DisplayName("Rechazar paciente incrementa intentosRegistro")
    void rechazarPaciente_incrementaIntentos() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setRol("PACIENTE");
        usuario.setEstado("PENDIENTE");
        usuario.setIntentosRegistro(1);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any())).thenReturn(usuario);

        Usuario resultado = usuarioService.rechazarPaciente(1L);

        assertEquals("RECHAZADO", resultado.getEstado());
        assertEquals(2, resultado.getIntentosRegistro());
    }
}