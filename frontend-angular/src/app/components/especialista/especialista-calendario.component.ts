import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Cita } from '../../model/cita';

interface Bloqueo {
  id: number;
  fecha: string;
  motivo: string;
  tipo: 'DIA_COMPLETO' | 'RANGO_HORAS';
  horaInicio?: string;
  horaFin?: string;
}

interface Feriado {
  id: number;
  fecha: string;
  motivo: string;
}

@Component({
  selector: 'app-especialista-calendario',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './especialista-calendario.component.html',
  styleUrl: './especialista-calendario.component.css'
})
export class EspecialistaCalendarioComponent implements OnInit {

  especialistaId: number = 0;
  especialistaNombre: string = '';

  semanaInicio: Date = new Date();
  dias: { fecha: Date; str: string; esHoy: boolean; nombre: string; num: number }[] = [];

  citas: Cita[] = [];
  bloqueos: Bloqueo[] = [];
  feriados: Feriado[] = [];
  pacientes: Map<number, any> = new Map();
  cargando = false;

  // Panel lateral de ausencias
  panelAbierto = false;
  modoPanel: 'lista' | 'nuevo' = 'lista';

  nuevaAusencia = {
    tipo: 'DIA_COMPLETO' as 'DIA_COMPLETO' | 'RANGO_HORAS',
    fecha: '',
    horaInicio: '07:00',
    horaFin: '08:00',
    motivo: ''
  };

  guardando = false;
  mensajePanel: string | null = null;
  tipoMensaje: 'ok' | 'error' = 'ok';

  horas = [
    '07:00','07:30','08:00','08:30','09:00','09:30',
    '10:00','10:30','11:00','11:30','12:00','12:30',
    '13:00','13:30','14:00','14:30','15:00','15:30','16:00'
  ];
  horasLabel: string[] = [];
  nombreDias = ['Lun','Mar','Mié','Jue','Vie','Sáb','Dom'];

  private api = 'http://localhost:8080/api/calendario';
  private apiCitas = 'http://localhost:8080/api/citas';
  private apiUsuarios = 'http://localhost:8080/api/usuarios';

  constructor(private http: HttpClient) {
    this.horasLabel = this.horas.map(h => {
      const [hh, mm] = h.split(':').map(Number);
      const ampm = hh < 12 ? 'AM' : 'PM';
      const h12 = hh > 12 ? hh - 12 : hh;
      return `${h12}:${mm === 0 ? '00' : mm} ${ampm}`;
    });
  }

  ngOnInit() {
    const raw = localStorage.getItem('especialista_logueado');
    if (raw) {
      const esp = JSON.parse(raw);
      this.especialistaId = esp.id;
      this.especialistaNombre = esp.nombreCompleto;
    }
    this.irHoy();
    this.cargarPacientes();
  }

  irHoy() {
    const hoy = new Date();
    const lunes = new Date(hoy);
    lunes.setDate(hoy.getDate() - ((hoy.getDay() + 6) % 7));
    lunes.setHours(0, 0, 0, 0);
    this.semanaInicio = lunes;
    this.buildDias();
    this.cargarDatos();
  }

  semanaAnterior() {
    this.semanaInicio = new Date(this.semanaInicio);
    this.semanaInicio.setDate(this.semanaInicio.getDate() - 7);
    this.buildDias();
    this.cargarDatos();
  }

  semanaSiguiente() {
    this.semanaInicio = new Date(this.semanaInicio);
    this.semanaInicio.setDate(this.semanaInicio.getDate() + 7);
    this.buildDias();
    this.cargarDatos();
  }

  buildDias() {
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    this.dias = [];
    for (let i = 0; i < 7; i++) {
      const d = new Date(this.semanaInicio);
      d.setDate(this.semanaInicio.getDate() + i);
      this.dias.push({
        fecha: d,
        str: this.fmt(d),
        esHoy: d.getTime() === hoy.getTime(),
        nombre: this.nombreDias[i],
        num: d.getDate()
      });
    }
  }

  cargarDatos() {
    if (!this.especialistaId) return;
    this.cargando = true;
    const id = this.especialistaId;
    const inicio = this.fmt(this.semanaInicio);
    const fin = new Date(this.semanaInicio);
    fin.setDate(fin.getDate() + 6);
    const finStr = this.fmt(fin);

    this.http.get<Cita[]>(`${this.apiCitas}/especialista/${id}`).subscribe({
      next: d => { this.citas = d; this.cargando = false; },
      error: () => { this.citas = []; this.cargando = false; }
    });

    this.http.get<Bloqueo[]>(`${this.api}/especialista/${id}/bloqueados`).subscribe({
      next: d => this.bloqueos = d,
      error: () => this.bloqueos = []
    });

    this.http.get<Feriado[]>(`${this.api}/feriados`, { params: { inicio, fin: finStr } }).subscribe({
      next: d => this.feriados = d,
      error: () => this.feriados = []
    });
  }

  cargarPacientes() {
    this.http.get<any[]>(`${this.apiUsuarios}?rol=PACIENTE`).subscribe({
      next: lista => lista.forEach(p => this.pacientes.set(p.id, p)),
      error: () => {}
    });
  }

  getNombrePaciente(id: number): string {
    return this.pacientes.get(id)?.nombreCompleto ?? `Pac. #${id}`;
  }

  // ── Lógica de celda ──
  getCita(fechaStr: string, hora: string): Cita | null {
    return this.citas.find(c =>
      c.fecha === fechaStr &&
      (c.hora ?? '').substring(0, 5) === hora &&
      c.estado !== 'CANCELADA'
    ) ?? null;
  }

  esDiaCompleto(fechaStr: string): boolean {
    return this.bloqueos.some(b => b.fecha === fechaStr && b.tipo === 'DIA_COMPLETO') ||
           this.feriados.some(f => f.fecha === fechaStr);
  }

  esFeriado(fechaStr: string): boolean {
    return this.feriados.some(f => f.fecha === fechaStr);
  }

  esDomingo(d: Date): boolean { return d.getDay() === 0; }

  esCeldaBloqueada(fechaStr: string, hora: string): boolean {
    return this.bloqueos.some(b =>
      b.tipo === 'RANGO_HORAS' &&
      b.fecha === fechaStr &&
      b.horaInicio != null && b.horaFin != null &&
      hora >= b.horaInicio.substring(0, 5) &&
      hora < b.horaFin.substring(0, 5)
    );
  }

  motivoCelda(fechaStr: string, hora: string): string {
    const b = this.bloqueos.find(b =>
      b.tipo === 'RANGO_HORAS' && b.fecha === fechaStr &&
      hora >= (b.horaInicio ?? '').substring(0, 5) &&
      hora < (b.horaFin ?? '').substring(0, 5)
    );
    return b?.motivo ?? '';
  }

  motivoDia(fechaStr: string): string {
    const f = this.feriados.find(f => f.fecha === fechaStr);
    if (f) return f.motivo;
    const b = this.bloqueos.find(b => b.fecha === fechaStr && b.tipo === 'DIA_COMPLETO');
    return b?.motivo ?? 'Ausente';
  }

  esAusenciaPropia(fechaStr: string): boolean {
    return this.bloqueos.some(b => b.fecha === fechaStr && b.tipo === 'DIA_COMPLETO');
  }

  claseCita(estado?: string): string {
    if (estado === 'POR_CONFIRMAR') return 'chip-amarillo';
    if (estado === 'ATENDIDA')      return 'chip-verde';
    if (estado === 'NO_ASISTIDA')   return 'chip-naranja';
    if (estado === 'CANCELADA')     return 'chip-rojo';
    return 'chip-azul';
  }

  iconoCita(estado?: string): string {
    if (estado === 'POR_CONFIRMAR') return 'fa-clock';
    if (estado === 'ATENDIDA')      return 'fa-circle-check';
    if (estado === 'NO_ASISTIDA')   return 'fa-circle-xmark';
    if (estado === 'CANCELADA')     return 'fa-ban';
    return 'fa-calendar-check';
  }

  labelEstado(estado?: string): string {
    if (estado === 'POR_CONFIRMAR') return 'Por confirmar';
    if (estado === 'PROGRAMADA')    return 'Confirmada';
    if (estado === 'ATENDIDA')      return 'Atendida';
    if (estado === 'NO_ASISTIDA')   return 'No asistió';
    if (estado === 'CANCELADA')     return 'Cancelada';
    if (estado === 'SIN_RESPUESTA') return 'Sin registro'; 
    return estado ?? '';
  }

  // ── Panel de ausencias ──
  abrirPanel() { this.panelAbierto = true; this.modoPanel = 'lista'; }
  cerrarPanel() { this.panelAbierto = false; this.mensajePanel = null; }

  guardarAusencia() {
    if (!this.nuevaAusencia.fecha || !this.nuevaAusencia.motivo.trim()) {
      this.flash('Completa todos los campos', 'error');
      return;
    }
    this.guardando = true;
    const body: any = {
      especialistaId: String(this.especialistaId),
      fecha:  this.nuevaAusencia.fecha,
      motivo: this.nuevaAusencia.motivo.trim(),
      tipo:   this.nuevaAusencia.tipo
    };
    if (this.nuevaAusencia.tipo === 'RANGO_HORAS') {
      body.horaInicio = this.nuevaAusencia.horaInicio + ':00';
      body.horaFin    = this.nuevaAusencia.horaFin    + ':00';
    }
    this.http.post(`${this.api}/especialista/bloquear`, body, { responseType: 'text' }).subscribe({
      next: () => {
        this.guardando = false;
        this.flash('Ausencia registrada', 'ok');
        this.nuevaAusencia = { tipo: 'DIA_COMPLETO', fecha: '', horaInicio: '07:00', horaFin: '08:00', motivo: '' };
        this.modoPanel = 'lista';
        this.cargarDatos();
      },
      error: () => { this.guardando = false; this.flash('Error al guardar', 'error'); }
    });
  }

  eliminarAusencia(id: number) {
    if (!confirm('¿Eliminar esta ausencia?')) return;
    this.http.delete(`${this.api}/especialista/bloquear/${id}`, { responseType: 'text' }).subscribe({
      next: () => { this.flash('Ausencia eliminada', 'ok'); this.cargarDatos(); },
      error: () => this.flash('Error al eliminar', 'error')
    });
  }

  ausenciasSemana(): Bloqueo[] {
    const strs = this.dias.map(d => d.str);
    return this.bloqueos.filter(b => strs.includes(b.fecha));
  }

  feriadosSemana(): Feriado[] {
    const strs = this.dias.map(d => d.str);
    return this.feriados.filter(f => strs.includes(f.fecha));
  }

  get totalProgramadas(): number {
    return this.citas.filter(c =>
      c.estado === 'PROGRAMADA' && this.dias.some(d => d.str === c.fecha)
    ).length;
  }

  get tituloSemana(): string {
    const fin = new Date(this.semanaInicio);
    fin.setDate(fin.getDate() + 6);
    const M = ['Enero','Febrero','Marzo','Abril','Mayo','Junio','Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];
    if (this.semanaInicio.getMonth() === fin.getMonth())
      return `${this.semanaInicio.getDate()} – ${fin.getDate()} de ${M[fin.getMonth()]} ${fin.getFullYear()}`;
    return `${this.semanaInicio.getDate()} ${M[this.semanaInicio.getMonth()]} – ${fin.getDate()} ${M[fin.getMonth()]} ${fin.getFullYear()}`;
  }

  private flash(msg: string, tipo: 'ok' | 'error') {
    this.mensajePanel = msg;
    this.tipoMensaje = tipo;
    setTimeout(() => this.mensajePanel = null, 3000);
  }

  private fmt(d: Date): string {
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }
}