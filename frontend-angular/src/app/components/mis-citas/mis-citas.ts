import { Component, OnInit, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HeaderComponent } from '../shared/header/header.component';
import { CitaService } from '../../services/cita.service';
import { EspecialistaService } from '../../services/especialista.service';
import { CitaContadorService } from '../../services/CitaContador.Service';
import { Cita } from '../../model/cita';
import { Especialista } from '../../model/especialista';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-mis-citas',
  standalone: true,
  imports: [CommonModule, RouterModule, HeaderComponent],
  templateUrl: './mis-citas.html',
  styleUrl: './mis-citas.css'
})
export class MisCitasComponent implements OnInit, AfterViewChecked {

  citas: Cita[] = [];
  especialistas: Map<number, Especialista> = new Map();
  cargando = true;
  tabActivo: 'proximas' | 'todas' = 'proximas';

  mostrarModal = false;
  citaAcancelar: Cita | null = null;
  cancelando = false;
  mensajeExito = '';
  mensajeError  = '';

  private qrsRendered = new Set<string>();

  constructor(
    private citaService: CitaService,
    private especialistaService: EspecialistaService,
    private contadorService: CitaContadorService
  ) {}

  ngOnInit() {
    const paciente = JSON.parse(localStorage.getItem('paciente_logueado') || '{}');
    forkJoin({
      especialistas: this.especialistaService.listarTodos(),
      citas: this.citaService.listarPorPaciente(paciente.id)
    }).subscribe({
      next: ({ especialistas, citas }) => {
        especialistas.forEach(e => this.especialistas.set(e.id!, e));
        this.citas = citas.sort((a, b) =>
          new Date(`${b.fecha}T${b.hora}`).getTime() - new Date(`${a.fecha}T${a.hora}`).getTime()
        );
        this.cargando = false;
      },
      error: () => { this.cargando = false; }
    });
  }

  // Renderizar QR después de que Angular actualice la vista
  ngAfterViewChecked() {
    this.citasActivas.forEach(c => {
      if (!c.codigoCita || this.qrsRendered.has(c.codigoCita)) return;
      const canvas = document.getElementById(`qr-${c.id}`) as HTMLCanvasElement;
      if (canvas) {
        this.dibujarQRSimple(canvas, c.codigoCita);
        this.qrsRendered.add(c.codigoCita);
      }
    });
  }

  // QR simplificado usando canvas (patrón visual representativo)
  private dibujarQRSimple(canvas: HTMLCanvasElement, texto: string) {
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    const size = 48;
    const cell = 4; // cada celda de 4px → 12x12 módulos
    const modules = size / cell;

    // Fondo blanco
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, 0, size, size);

    // Generar patrón pseudoaleatorio basado en el texto
    let hash = 0;
    for (let i = 0; i < texto.length; i++) {
      hash = ((hash << 5) - hash) + texto.charCodeAt(i);
      hash |= 0;
    }

    ctx.fillStyle = '#1a56db';
    for (let row = 0; row < modules; row++) {
      for (let col = 0; col < modules; col++) {
        // Esquinas fijas (finder patterns del QR real)
        const enEsquina =
          (row < 3 && col < 3) ||
          (row < 3 && col >= modules - 3) ||
          (row >= modules - 3 && col < 3);

        let filled = false;
        if (enEsquina) {
          // Patrón de esquina: borde + centro
          const localR = row < 3 ? row : row - (modules - 3);
          const localC = col < 3 ? col : col - (modules - 3);
          filled = localR === 0 || localR === 2 || localC === 0 || localC === 2 || (localR === 1 && localC === 1);
        } else {
          // Datos: hash del texto
          const bit = (hash ^ (row * 17 + col * 31 + row * col)) & 1;
          filled = bit === 1;
        }

        if (filled) {
          ctx.fillRect(col * cell, row * cell, cell - 0.5, cell - 0.5);
        }
      }
    }
  }

  // ─── Cancelación ────────────────────────────────────────────────

  puedeCancelar(cita: Cita): boolean {
    if (cita.estado !== 'PROGRAMADA' && cita.estado !== 'POR_CONFIRMAR') return false;
    const ahora = new Date();
    const fechaHoraCita = new Date(`${cita.fecha}T${cita.hora}`);
    const diffHoras = (fechaHoraCita.getTime() - ahora.getTime()) / (1000 * 60 * 60);
    return diffHoras > 24;
  }

  cancelacionVencida(cita: Cita): boolean {
    if (cita.estado !== 'PROGRAMADA' && cita.estado !== 'POR_CONFIRMAR') return false;
    return !this.puedeCancelar(cita);
  }

  abrirModalCancelacion(cita: Cita) {
    this.citaAcancelar = cita;
    this.mostrarModal = true;
    this.mensajeExito = '';
    this.mensajeError  = '';
  }

  cerrarModal() {
    if (this.cancelando) return;
    this.mostrarModal = false;
    this.citaAcancelar = null;
  }

  confirmarCancelacion() {
    if (!this.citaAcancelar || this.cancelando) return;
    this.cancelando = true;

    this.citaService.cancelar(this.citaAcancelar.id!, 'Cancelado por el paciente').subscribe({
      next: (citaActualizada) => {
        const idx = this.citas.findIndex(c => c.id === citaActualizada.id);
        if (idx !== -1) this.citas[idx] = citaActualizada;
        const paciente = JSON.parse(localStorage.getItem('paciente_logueado') || '{}');
        this.contadorService.refrescar(paciente.id);
        this.cancelando = false;
        this.mostrarModal = false;
        this.citaAcancelar = null;
        this.mensajeExito = 'Tu cita fue cancelada correctamente.';
        setTimeout(() => this.mensajeExito = '', 4000);
      },
      error: () => {
        this.cancelando = false;
        this.mensajeError = 'Ocurrió un error al cancelar. Intenta de nuevo.';
        setTimeout(() => this.mensajeError = '', 4000);
      }
    });
  }

  // ─── Getters ────────────────────────────────────────────────────

  get citasProximas(): Cita[] {
    const hoy = new Date(); hoy.setHours(0, 0, 0, 0);
    return this.citas
      .filter(c => new Date(c.fecha + 'T00:00:00') >= hoy && (c.estado === 'PROGRAMADA' || c.estado === 'POR_CONFIRMAR'))
      .sort((a, b) => new Date(`${a.fecha}T${a.hora}`).getTime() - new Date(`${b.fecha}T${b.hora}`).getTime());
  }

  get todasLasCitas(): Cita[] { return this.citas; }

  get citasActivas(): Cita[] {
    return this.tabActivo === 'proximas' ? this.citasProximas : this.todasLasCitas;
  }

  // ─── Helpers ────────────────────────────────────────────────────

  getNombreEspecialista(id: number): string { return this.especialistas.get(id)?.nombreCompleto ?? 'Especialista'; }
  getEspecialidad(id: number): string       { return this.especialistas.get(id)?.especialidad ?? ''; }

  getMes(fecha: string): string {
    return ['ENE','FEB','MAR','ABR','MAY','JUN','JUL','AGO','SEP','OCT','NOV','DIC'][new Date(fecha + 'T00:00:00').getMonth()];
  }
  getDia(fecha: string):  number { return new Date(fecha + 'T00:00:00').getDate(); }
  getAnio(fecha: string): number { return new Date(fecha + 'T00:00:00').getFullYear(); }

  getEstadoClase(estado: string): string {
    const map: Record<string, string> = {
      'POR_CONFIRMAR': 'estado-por-confirmar',
      'PROGRAMADA':    'estado-programada',
      'CANCELADA':     'estado-cancelada',
      'ATENDIDA':      'estado-atendida',
      'NO_ASISTIDA':   'estado-no-asistida'
    };
    return map[estado] ?? '';
  }

  getEstadoLabel(estado?: string): string {
    const map: Record<string, string> = {
      'POR_CONFIRMAR': 'Por confirmar',
      'PROGRAMADA':    'Confirmada',
      'CANCELADA':     'Cancelada',
      'ATENDIDA':      'Atendida',
      'NO_ASISTIDA':   'No asistió'
    };
    return map[estado ?? ''] ?? estado ?? '';
  }

  getBarraClase(estado: string): string {
    const map: Record<string, string> = {
      'POR_CONFIRMAR': 'barra-por-confirmar',
      'PROGRAMADA':    'barra-programada',
      'ATENDIDA':      'barra-atendida',
      'CANCELADA':     'barra-cancelada',
      'NO_ASISTIDA':   'barra-no-asistida'
    };
    return map[estado] ?? '';
  }
}