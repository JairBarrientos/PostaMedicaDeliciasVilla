package com.postamedica.rest.infraestructure.web;

import com.postamedica.rest.domain.model.Disponibilidad;
import com.postamedica.rest.domain.model.Feriado;
import com.postamedica.rest.domain.repository.DisponibilidadRepository;
import com.postamedica.rest.domain.repository.FeriadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/calendario")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CalendarioController {

    private final DisponibilidadRepository disponibilidadRepo;
    private final FeriadoRepository feriadoRepo;

    @PostMapping("/especialista/bloquear")
    public ResponseEntity<?> bloquearDia(@RequestBody Map<String, String> body) {
        Long espId     = Long.parseLong(body.get("especialistaId"));
        LocalDate fecha = LocalDate.parse(body.get("fecha"));
        String motivo  = body.getOrDefault("motivo", "No disponible");
        String tipo    = body.getOrDefault("tipo", "DIA_COMPLETO");

        LocalTime horaInicio = null;
        LocalTime horaFin    = null;

        if ("RANGO_HORAS".equals(tipo)) {
            horaInicio = LocalTime.parse(body.get("horaInicio"));
            horaFin    = LocalTime.parse(body.get("horaFin"));
        }

        if ("DIA_COMPLETO".equals(tipo) && disponibilidadRepo.existsByEspecialistaIdAndFechaAndTipo(espId, fecha, "DIA_COMPLETO")) {
            return ResponseEntity.ok("Ya bloqueado");
        }

        Disponibilidad d = new Disponibilidad();
        d.setEspecialistaId(espId);
        d.setFecha(fecha);
        d.setMotivo(motivo);
        d.setTipo(tipo);
        d.setHoraInicio(horaInicio);
        d.setHoraFin(horaFin);
        disponibilidadRepo.save(d);

        return ResponseEntity.ok("Bloqueo registrado");
    }

    @DeleteMapping("/especialista/bloquear/{id}")
    public ResponseEntity<?> eliminarBloqueo(@PathVariable Long id) {
        disponibilidadRepo.deleteById(id);
        return ResponseEntity.ok("Bloqueo eliminado");
    }

    @GetMapping("/especialista/{especialistaId}/bloqueados")
    public ResponseEntity<List<Map<String, Object>>> getBloqueados(@PathVariable Long especialistaId) {
        List<Map<String, Object>> result = disponibilidadRepo.findByEspecialistaId(especialistaId)
                .stream().map(d -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id",         d.getId());
                    m.put("fecha",      d.getFecha().toString());
                    m.put("motivo",     d.getMotivo());
                    m.put("tipo",       d.getTipo());
                    m.put("horaInicio", d.getHoraInicio() != null ? d.getHoraInicio().toString() : null);
                    m.put("horaFin",    d.getHoraFin()    != null ? d.getHoraFin().toString()    : null);
                    return m;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/feriado")
    public ResponseEntity<?> agregarFeriado(@RequestBody Map<String, String> body) {
        LocalDate fecha = LocalDate.parse(body.get("fecha"));
        String motivo   = body.getOrDefault("motivo", "Feriado");
        if (!feriadoRepo.existsByFecha(fecha)) {
            Feriado f = new Feriado();
            f.setFecha(fecha);
            f.setMotivo(motivo);
            feriadoRepo.save(f);
        }
        return ResponseEntity.ok("Feriado registrado");
    }

    @DeleteMapping("/feriado")
    public ResponseEntity<?> eliminarFeriado(@RequestBody Map<String, String> body) {
        LocalDate fecha = LocalDate.parse(body.get("fecha"));
        feriadoRepo.deleteByFecha(fecha);
        return ResponseEntity.ok("Feriado eliminado");
    }

    @GetMapping("/feriados")
    public ResponseEntity<List<Map<String, Object>>> getFeriados(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        List<Map<String, Object>> result = feriadoRepo.findByFechaBetween(inicio, fin)
                .stream().map(f -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id",     f.getId());
                    m.put("fecha",  f.getFecha().toString());
                    m.put("motivo", f.getMotivo());
                    return m;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<Map<String, Object>> getDisponibilidad(
            @RequestParam Long especialistaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        List<Disponibilidad> bloqueos = disponibilidadRepo.findByEspecialistaId(especialistaId)
                .stream()
                .filter(d -> !d.getFecha().isBefore(inicio) && !d.getFecha().isAfter(fin))
                .toList();

        List<String> bloqueados = bloqueos.stream()
                .filter(d -> "DIA_COMPLETO".equals(d.getTipo()))
                .map(d -> d.getFecha().toString()).toList();

        List<Map<String, Object>> rangos = bloqueos.stream()
                .filter(d -> "RANGO_HORAS".equals(d.getTipo()))
                .map(d -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("fecha",      d.getFecha().toString());
                    m.put("horaInicio", d.getHoraInicio().toString());
                    m.put("horaFin",    d.getHoraFin().toString());
                    m.put("motivo",     d.getMotivo());
                    return m;
                }).collect(Collectors.toList());

        List<String> feriados = feriadoRepo.findByFechaBetween(inicio, fin)
                .stream().map(f -> f.getFecha().toString()).toList();

        return ResponseEntity.ok(Map.of(
                "bloqueados", bloqueados,
                "rangos",     rangos,
                "feriados",   feriados
        ));
    }
}