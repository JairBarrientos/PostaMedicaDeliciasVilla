package com.postamedica.rest.infraestructure.web;

import com.postamedica.rest.application.services.ChatService;
import com.postamedica.rest.domain.dtos.EnviarMensajeRequest;
import com.postamedica.rest.domain.dtos.MensajeChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<?> enviar(@RequestBody EnviarMensajeRequest request) {
        try {
            MensajeChatResponse response = MensajeChatResponse.fromDomain(chatService.enviar(request));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<MensajeChatResponse>> obtenerConversacion(
            @RequestParam Long pacienteId,
            @RequestParam Long especialistaId) {
        List<MensajeChatResponse> lista = chatService
                .obtenerConversacion(pacienteId, especialistaId).stream()
                .map(MensajeChatResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/no-leidos/{pacienteId}")
    public ResponseEntity<Integer> contarNoLeidos(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(chatService.contarNoLeidos(pacienteId));
    }

    @PutMapping("/marcar-leidos")
    public ResponseEntity<Void> marcarComoLeidos(
            @RequestParam Long pacienteId,
            @RequestParam Long especialistaId) {
        chatService.marcarComoLeidos(pacienteId, especialistaId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/especialistas-con-noLeidos/{pacienteId}")
    public ResponseEntity<List<Long>> especialistasConNoLeidos(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(chatService.especialistasConNoLeidos(pacienteId));
    }

    // ✅ NUEVO: verificar si el chat está habilitado entre paciente y especialista
    @GetMapping("/habilitado")
    public ResponseEntity<Boolean> chatHabilitado(
            @RequestParam Long pacienteId,
            @RequestParam Long especialistaId) {
        return ResponseEntity.ok(chatService.chatHabilitado(pacienteId, especialistaId));
    }
}