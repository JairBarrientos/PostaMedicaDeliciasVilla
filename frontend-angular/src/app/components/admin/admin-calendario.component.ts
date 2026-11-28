import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { EspecialistaService } from '../../services/especialista.service';
import { ReporteService } from '../../services/reporte.service';
import { Especialista } from '../../model/especialista';
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
  selector: 'app-admin-calendario',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-calendario.component.html',
  styleUrl: './admin-calendario.component.css'
})
export class AdminCalendarioComponent implements OnInit {

  especialistas: Especialista[] = [];
  especialistaSeleccionadoId: number | null = null;
  get espActual(): Especialista | undefined {
    return this.especialistas.find(e => e.id === this.especialistaSeleccionadoId);
  }

  semanaInicio: Date = new Date();
  dias: { fecha: Date; str: string; esHoy: boolean; nombre: string; num: number }[] = [];

  citas: Cita[] = [];
  bloqueos: Bloqueo[] = [];
  feriados: Feriado[] = [];
  cargando = false;

  panelAbierto = false;
  modoPanel: 'lista' | 'nuevo' = 'lista';

  nuevoBloqueo = {
    tipo: 'DIA_COMPLETO' as 'DIA_COMPLETO' | 'RANGO_HORAS',
    fecha: '',
    horaInicio: '07:00',
    horaFin: '08:00',
    motivo: ''
  };

  nuevoFeriado = { fecha: '', motivo: '' };
  tabPanel: 'especialista' | 'feriado' = 'especialista';
  guardandoBloqueo = false;
  mensajePanel: string | null = null;
  tipoMensaje: 'ok' | 'error' = 'ok';

  horas = [
    '07:00','07:30','08:00','08:30','09:00','09:30',
    '10:00','10:30','11:00','11:30','12:00','12:30',
    '13:00','13:30','14:00','14:30','15:00','15:30','16:00'
  ];
  horasLabel: string[] = [];
  nombreDias = ['Lun','Mar','Mié','Jue','Vie','Sáb','Dom'];

  private api     = 'https://postamedica-backend.onrender.com/api/calendario';
  private apiCitas = 'https://postamedica-backend.onrender.com/api/citas';

  constructor(
    private especialistaService: EspecialistaService,
    private http: HttpClient,
    private reporteService: ReporteService
  ) {
    this.horasLabel = this.horas.map(h => {
      const [hh, mm] = h.split(':').map(Number);
      const ampm = hh < 12 ? 'AM' : 'PM';
      const h12 = hh > 12 ? hh - 12 : hh;
      return `${h12}:${mm === 0 ? '00' : mm} ${ampm}`;
    });
  }

  ngOnInit() {
    this.especialistaService.listarTodos().subscribe(l => this.especialistas = l);
    this.irHoy();
  }

  irHoy() {
    const hoy = new Date();
    const lunes = new Date(hoy);
    lunes.setDate(hoy.getDate() - ((hoy.getDay() + 6) % 7));
    lunes.setHours(0,0,0,0);
    this.semanaInicio = lunes;
    this.buildDias(); this.cargarDatos();
  }

  semanaAnterior() {
    this.semanaInicio = new Date(this.semanaInicio);
    this.semanaInicio.setDate(this.semanaInicio.getDate() - 7);
    this.buildDias(); this.cargarDatos();
  }

  semanaSiguiente() {
    this.semanaInicio = new Date(this.semanaInicio);
    this.semanaInicio.setDate(this.semanaInicio.getDate() + 7);
    this.buildDias(); this.cargarDatos();
  }

  buildDias() {
    const hoy = new Date(); hoy.setHours(0,0,0,0);
    this.dias = [];
    for (let i = 0; i < 7; i++) {
      const d = new Date(this.semanaInicio);
      d.setDate(this.semanaInicio.getDate() + i);
      this.dias.push({ fecha: d, str: this.fmt(d), esHoy: d.getTime() === hoy.getTime(), nombre: this.nombreDias[i], num: d.getDate() });
    }
  }

  onEspChange() { this.cargarDatos(); }

  cargarDatos() {
    if (!this.especialistaSeleccionadoId) { this.citas = []; this.bloqueos = []; this.feriados = []; return; }
    this.cargando = true;
    const id = this.especialistaSeleccionadoId;
    const inicio = this.fmt(this.semanaInicio);
    const fin = new Date(this.semanaInicio); fin.setDate(fin.getDate() + 6);
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

  // ─── DESCARGAS EXCEL ───

  descargarCitasEspecialista() {
    if (!this.espActual) return;
    const nombre = this.espActual.nombreCompleto ??
      `${this.espActual.nombres} ${this.espActual.apellidos}`;
    this.reporteService.exportarCitasEspecialista(this.citas, nombre);
  }

  descargarDisponibilidad() {
    if (!this.espActual) return;
    const nombre = this.espActual.nombreCompleto ??
      `${this.espActual.nombres} ${this.espActual.apellidos}`;
    this.reporteService.exportarDisponibilidad(this.bloqueos, this.feriados, nombre);
  }

  // ── Lógica de celda ──
  getCita(fechaStr: string, hora: string): Cita | null {
    return this.citas.find(c =>
      c.fecha === fechaStr &&
      (c.hora ?? '').substring(0,5) === hora &&
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
      hora >= b.horaInicio.substring(0,5) &&
      hora < b.horaFin.substring(0,5)
    );
  }

  motivoCelda(fechaStr: string, hora: string): string {
    const b = this.bloqueos.find(b =>
      b.tipo === 'RANGO_HORAS' && b.fecha === fechaStr &&
      hora >= (b.horaInicio ?? '').substring(0,5) &&
      hora < (b.horaFin ?? '').substring(0,5)
    );
    return b?.motivo ?? '';
  }

  motivoDia(fechaStr: string): string {
    const f = this.feriados.find(f => f.fecha === fechaStr);
    if (f) return f.motivo;
    const b = this.bloqueos.find(b => b.fecha === fechaStr && b.tipo === 'DIA_COMPLETO');
    return b?.motivo ?? 'No trabaja';
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

  abrirPanel() { this.panelAbierto = true; this.modoPanel = 'lista'; }
  cerrarPanel() { this.panelAbierto = false; this.mensajePanel = null; }

  guardarBloqueo() {
    if (!this.especialistaSeleccionadoId || !this.nuevoBloqueo.fecha || !this.nuevoBloqueo.motivo.trim()) {
      this.flash('Completa todos los campos', 'error'); return;
    }
    this.guardandoBloqueo = true;
    const body: any = {
      especialistaId: String(this.especialistaSeleccionadoId),
      fecha:   this.nuevoBloqueo.fecha,
      motivo:  this.nuevoBloqueo.motivo.trim(),
      tipo:    this.nuevoBloqueo.tipo
    };
    if (this.nuevoBloqueo.tipo === 'RANGO_HORAS') {
      body.horaInicio = this.nuevoBloqueo.horaInicio + ':00';
      body.horaFin    = this.nuevoBloqueo.horaFin    + ':00';
    }
    this.http.post(`${this.api}/especialista/bloquear`, body, { responseType: 'text' }).subscribe({
      next: () => {
        this.guardandoBloqueo = false;
        this.flash('Bloqueo guardado', 'ok');
        this.nuevoBloqueo = { tipo: 'DIA_COMPLETO', fecha: '', horaInicio: '07:00', horaFin: '08:00', motivo: '' };
        this.modoPanel = 'lista';
        this.cargarDatos();
      },
      error: () => { this.guardandoBloqueo = false; this.flash('Error al guardar', 'error'); }
    });
  }

  guardarFeriado() {
    if (!this.nuevoFeriado.fecha || !this.nuevoFeriado.motivo.trim()) {
      this.flash('Completa fecha y motivo', 'error'); return;
    }
    this.guardandoBloqueo = true;
    this.http.post(`${this.api}/feriado`, this.nuevoFeriado, { responseType: 'text' }).subscribe({
      next: () => {
        this.guardandoBloqueo = false;
        this.flash('Feriado guardado', 'ok');
        this.nuevoFeriado = { fecha: '', motivo: '' };
        this.modoPanel = 'lista';
        this.cargarDatos();
      },
      error: () => { this.guardandoBloqueo = false; this.flash('Error al guardar', 'error'); }
    });
  }

  eliminarBloqueo(id: number) {
    if (!confirm('¿Eliminar este bloqueo?')) return;
    this.http.delete(`${this.api}/especialista/bloquear/${id}`, { responseType: 'text' }).subscribe({
      next: () => { this.flash('Bloqueo eliminado', 'ok'); this.cargarDatos(); },
      error: () => this.flash('Error al eliminar', 'error')
    });
  }

  eliminarFeriado(fecha: string) {
    if (!confirm('¿Eliminar este feriado?')) return;
    this.http.delete(`${this.api}/feriado`, { body: { fecha }, responseType: 'text' }).subscribe({
      next: () => { this.flash('Feriado eliminado', 'ok'); this.cargarDatos(); },
      error: () => this.flash('Error al eliminar', 'error')
    });
  }

  bloqueosSemana(): Bloqueo[] {
    const strs = this.dias.map(d => d.str);
    return this.bloqueos.filter(b => strs.includes(b.fecha));
  }

  feriadosSemana(): Feriado[] {
    const strs = this.dias.map(d => d.str);
    return this.feriados.filter(f => strs.includes(f.fecha));
  }

  get totalProgramadas(): number {
    return this.citas.filter(c => c.estado === 'PROGRAMADA' && this.dias.some(d => d.str === c.fecha)).length;
  }

  get tituloSemana(): string {
    const fin = new Date(this.semanaInicio); fin.setDate(fin.getDate() + 6);
    const M = ['Enero','Febrero','Marzo','Abril','Mayo','Junio','Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];
    if (this.semanaInicio.getMonth() === fin.getMonth())
      return `${this.semanaInicio.getDate()} – ${fin.getDate()} de ${M[fin.getMonth()]} ${fin.getFullYear()}`;
    return `${this.semanaInicio.getDate()} ${M[this.semanaInicio.getMonth()]} – ${fin.getDate()} ${M[fin.getMonth()]} ${fin.getFullYear()}`;
  }

  private flash(msg: string, tipo: 'ok' | 'error') {
    this.mensajePanel = msg; this.tipoMensaje = tipo;
    setTimeout(() => this.mensajePanel = null, 3000);
  }

  private fmt(d: Date): string {
    return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
  }
}