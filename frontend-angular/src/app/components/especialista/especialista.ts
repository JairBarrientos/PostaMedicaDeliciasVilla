import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CitaService } from '../../services/cita.service';
import { UsuarioService } from '../../services/usuario.service';
import { ChatService } from '../../services/chat.service';
import { Cita } from '../../model/cita';
import { EspecialistaCalendarioComponent } from './especialista-calendario.component';

@Component({
  selector: 'app-especialista',
  standalone: true,
  imports: [CommonModule, FormsModule, EspecialistaCalendarioComponent],
  templateUrl: './especialista.html',
  styleUrl: './especialista.css'
})
export class EspecialistaComponent implements OnInit, OnDestroy {

  especialista: any = null;
  citas: Cita[] = [];
  pacientes: Map<number, any> = new Map();

  // Rechazar / Cancelar
  citaArechazar: Cita | null = null;
  mensajeRechazo = '';
  citaACancelar: Cita | null = null;
  mensajeCancelacion = '';

  tab: 'calendario' | 'calendario-semana' | 'chat' = 'calendario';

  // Chat
  pacienteSeleccionado: any = null;
  mensajes: any[] = [];
  nuevoMensaje = '';
  private chatInterval: any;

  // Ticker: actualiza "ahora" cada 30 seg para refrescar botones en tiempo real
  private tickInterval: any;
  ahora: Date = new Date();

  constructor(
    private citaService: CitaService,
    private usuarioService: UsuarioService,
    private chatService: ChatService,
    private router: Router
  ) {}

  ngOnInit() {
    this.especialista = JSON.parse(localStorage.getItem('especialista_logueado') || '{}');
    this.cargarCitas();
    this.cargarPacientes();
    this.tickInterval = setInterval(() => { this.ahora = new Date(); }, 30000);
  }

  cargarCitas() {
    this.citaService.listarPorEspecialista(this.especialista.id).subscribe(data => {
      this.citas = data.sort((a, b) => {
        const fechaCmp = a.fecha.localeCompare(b.fecha);
        return fechaCmp !== 0 ? fechaCmp : a.hora.localeCompare(b.hora);
      });
    });
  }

  cargarPacientes() {
    this.usuarioService.listarPorRol('PACIENTE').subscribe(lista => {
      lista.forEach(p => this.pacientes.set(p.id, p));
    });
  }

  getNombrePaciente(id: number): string {
    return this.pacientes.get(id)?.nombreCompleto ?? 'Paciente #' + id;
  }

  getEstadoClase(estado: string): string {
    const map: Record<string, string> = {
      'POR_CONFIRMAR': 'estado-por-confirmar',
      'PROGRAMADA':    'estado-programada',
      'CANCELADA':     'estado-cancelada',
      'ATENDIDA':      'estado-atendida',
      'NO_ASISTIDA':   'estado-no-asistida',
      'SIN_RESPUESTA': 'estado-sin-respuesta'
    };
    return map[estado] ?? '';
  }

  getEstadoLabel(estado?: string): string {
    const map: Record<string, string> = {
      'POR_CONFIRMAR': 'Por confirmar',
      'PROGRAMADA':    'Confirmada',
      'CANCELADA':     'Cancelada',
      'ATENDIDA':      'Atendida',
      'NO_ASISTIDA':   'No asistió',
      'SIN_RESPUESTA': 'Sin registro'
    };
    return map[estado ?? ''] ?? estado ?? '';
  }

  // ─── VENTANA HORARIA ──────────────────────────────────────
  // true si ahora está entre hora_inicio y hora_inicio+29:59
  estaEnVentana(cita: Cita): boolean {
    if (!cita.fecha || !cita.hora) return false;
    const [anio, mes, dia] = cita.fecha.split('-').map(Number);
    const [hh, mm] = cita.hora.split(':').map(Number);
    const inicio = new Date(anio, mes - 1, dia, hh, mm, 0);
    const fin    = new Date(anio, mes - 1, dia, hh, mm + 29, 59);
    return this.ahora >= inicio && this.ahora <= fin;
  }

  // true si la ventana ya cerró (cita pasó) y no fue marcada por el especialista
  esVencidaSinMarca(cita: Cita): boolean {
    return cita.estado === 'SIN_RESPUESTA';
  }

  // columna "Editar" solo aparece cuando es SIN_RESPUESTA
  mostrarColumnaEditar(): boolean {
    return this.citas.some(c => c.estado === 'SIN_RESPUESTA');
  }

  // ─── ACCIONES ─────────────────────────────────────────────
  aceptarCita(id: number) {
    if (!confirm('¿Confirmar esta cita?')) return;
    this.citaService.aceptar(id).subscribe(() => this.cargarCitas());
  }

  abrirRechazar(cita: Cita) { this.citaArechazar = cita; this.mensajeRechazo = ''; }

  confirmarRechazo() {
    if (!this.mensajeRechazo.trim()) return;
    this.citaService.rechazar(this.citaArechazar!.id!, this.mensajeRechazo).subscribe(() => {
      this.citaArechazar = null;
      this.cargarCitas();
    });
  }

  abrirCancelar(cita: Cita) { this.citaACancelar = cita; this.mensajeCancelacion = ''; }

  confirmarCancelacion() {
    if (!this.mensajeCancelacion.trim()) return;
    this.citaService.cancelar(this.citaACancelar!.id!, this.mensajeCancelacion).subscribe(() => {
      this.citaACancelar = null;
      this.cargarCitas();
    });
  }

  marcarAtendida(id: number) {
    if (!confirm('¿Marcar esta cita como atendida?')) return;
    this.citaService.actualizarEstado(id, 'ATENDIDA').subscribe(() => this.cargarCitas());
  }

  marcarNoAsistida(id: number) {
    if (!confirm('¿Marcar que el paciente no asistió?')) return;
    this.citaService.actualizarEstado(id, 'NO_ASISTIDA').subscribe(() => this.cargarCitas());
  }

  // ─── CHAT ─────────────────────────────────────────────────
  get pacientesConCita(): any[] {
    const ids = [...new Set(this.citas.map(c => c.pacienteId))];
    return ids.map(id => this.pacientes.get(id)).filter(Boolean);
  }

  seleccionarPaciente(p: any) {
    this.pacienteSeleccionado = p;
    this.cargarMensajes();
    clearInterval(this.chatInterval);
    this.chatInterval = setInterval(() => this.cargarMensajes(), 4000);
  }

  cargarMensajes() {
    if (!this.pacienteSeleccionado) return;
    this.chatService.obtenerConversacion(this.pacienteSeleccionado.id, this.especialista.id)
      .subscribe((msgs: any[]) => this.mensajes = msgs);
  }

  enviarMensaje() {
    if (!this.nuevoMensaje.trim() || !this.pacienteSeleccionado) return;
    this.chatService.enviar({
      pacienteId: this.pacienteSeleccionado.id,
      especialistaId: this.especialista.id,
      remitenteRol: 'ESPECIALISTA',
      contenido: this.nuevoMensaje.trim()
    }).subscribe(() => { this.nuevoMensaje = ''; this.cargarMensajes(); });
  }

  ngOnDestroy() {
    clearInterval(this.chatInterval);
    clearInterval(this.tickInterval);
  }

  logout() { localStorage.clear(); this.router.navigate(['/auth']); }
}