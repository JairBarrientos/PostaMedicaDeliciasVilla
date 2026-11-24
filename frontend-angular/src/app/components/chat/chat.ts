import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../shared/header/header.component';
import { ChatService } from '../../services/chat.service';
import { EspecialistaService } from '../../services/especialista.service';
import { CitaService } from '../../services/cita.service';
import { Especialista } from '../../model/especialista';
import { Router } from '@angular/router';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent],
  templateUrl: './chat.html',
  styleUrl: './chat.css'
})
export class ChatComponent implements OnInit, OnDestroy {

  paciente: any = null;
  especialistas: Especialista[] = [];
  especialistaSeleccionado: Especialista | null = null;
  mensajes: any[] = [];
  nuevoMensaje = '';
  private chatInterval: any;

  especialistasConMensajes: Set<number> = new Set();
  chatHabilitadoMap: Map<number, boolean> = new Map();
  chatActivo = false;

  citasDelPaciente: any[] = [];

  constructor(
    private chatService: ChatService,
    private especialistaService: EspecialistaService,
    private citaService: CitaService,
    private router: Router
  ) {}

  ngOnInit() {
    this.paciente = JSON.parse(localStorage.getItem('paciente_logueado') || '{}');
    this.citaService.listarPorPaciente(this.paciente.id).subscribe(citas => {
      this.citasDelPaciente = citas;
      const ids = [...new Set(citas.map(c => c.especialistaId))];
      this.especialistaService.listarTodos().subscribe(lista => {
        this.especialistas = lista.filter(e => ids.includes(e.id!));
        this.cargarEspecialistasConNoLeidos();
        this.especialistas.forEach(e => {
          this.chatService.chatHabilitado(this.paciente.id, e.id!).subscribe(habilitado => {
            this.chatHabilitadoMap.set(e.id!, habilitado);
          });
        });
      });
    });
  }

  ngOnDestroy() { clearInterval(this.chatInterval); }

  cargarEspecialistasConNoLeidos() {
    this.chatService.especialistasConNoLeidos(this.paciente.id).subscribe(ids => {
      this.especialistasConMensajes = new Set(ids);
    });
  }

  tieneNoLeidos(especialistaId: number): boolean {
    return this.especialistasConMensajes.has(especialistaId);
  }

  tieneChatActivo(especialistaId: number): boolean {
    return this.chatHabilitadoMap.get(especialistaId) ?? false;
  }

  seleccionarEspecialista(e: Especialista) {
    this.especialistaSeleccionado = e;
    this.chatActivo = this.tieneChatActivo(e.id!);
    this.cargarMensajes();
    clearInterval(this.chatInterval);
    this.chatInterval = setInterval(() => this.cargarMensajes(), 4000);
    this.chatService.marcarComoLeidos(this.paciente.id, e.id!).subscribe(() => {
      this.especialistasConMensajes.delete(e.id!);
    });
  }

  cargarMensajes() {
    if (!this.especialistaSeleccionado) return;
    this.chatService.obtenerConversacion(this.paciente.id, this.especialistaSeleccionado.id!)
      .subscribe(msgs => {
        this.mensajes = msgs;
        this.chatService.marcarComoLeidos(this.paciente.id, this.especialistaSeleccionado!.id!).subscribe();
      });
  }

  enviarMensaje() {
    if (!this.nuevoMensaje.trim() || !this.especialistaSeleccionado || !this.chatActivo) return;
    this.chatService.enviar({
      pacienteId: this.paciente.id,
      especialistaId: this.especialistaSeleccionado.id!,
      remitenteRol: 'PACIENTE',
      contenido: this.nuevoMensaje.trim()
    }).subscribe(() => {
      this.nuevoMensaje = '';
      this.cargarMensajes();
    });
  }

  limpiarMarca(contenido: string): string {
    return contenido.replace(/\s*\[REC-(24H|1H)-[^\]]+\]/g, '').trim();
  }

  esMensaje24h(contenido: string): boolean {
    return contenido.includes('[REC-24H-');
  }

  private citaActivaConEspecialista(especialistaId: number): any {
    return this.citasDelPaciente.find(c =>
      c.especialistaId === especialistaId &&
      (c.estado === 'PROGRAMADA' || c.estado === 'POR_CONFIRMAR')
    );
  }

  puedeCancelarCitaEspecialista(especialistaId: number): boolean {
    const cita = this.citaActivaConEspecialista(especialistaId);
    if (!cita) return false;
    const fechaStr = Array.isArray(cita.fecha)
      ? `${cita.fecha[0]}-${String(cita.fecha[1]).padStart(2,'0')}-${String(cita.fecha[2]).padStart(2,'0')}`
      : String(cita.fecha).substring(0, 10);
    const horaStr = Array.isArray(cita.hora)
      ? `${String(cita.hora[0]).padStart(2,'0')}:${String(cita.hora[1]).padStart(2,'0')}:00`
      : String(cita.hora).substring(0, 8);
    const fechaHora = new Date(`${fechaStr}T${horaStr}`);
    const diffHoras = (fechaHora.getTime() - Date.now()) / (1000 * 60 * 60);
    return diffHoras > 24;
  }

  citaVencidaCancelacion(especialistaId: number): boolean {
    const cita = this.citaActivaConEspecialista(especialistaId);
    if (!cita) return false;
    return !this.puedeCancelarCitaEspecialista(especialistaId);
  }

  irACancelarCita() {
    this.router.navigate(['/mis-citas']);
  }

  enviarRapido(texto: string) {
  if (!this.especialistaSeleccionado || !this.chatActivo) return;
  this.chatService.enviar({
    pacienteId: this.paciente.id,
    especialistaId: this.especialistaSeleccionado.id!,
    remitenteRol: 'PACIENTE',
    contenido: texto
  }).subscribe(() => this.cargarMensajes());
}
}