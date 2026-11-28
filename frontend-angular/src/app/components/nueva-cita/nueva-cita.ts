import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HeaderComponent } from '../shared/header/header.component';
import { CitaService } from '../../services/cita.service';
import { EspecialistaService } from '../../services/especialista.service';
import { Especialista } from '../../model/especialista';
import { HttpClient } from '@angular/common/http';
import { CitaContadorService } from '../../services/CitaContador.Service';

interface DiaSemana {
  nombre: string; numero: number; mes: string;
  fechaStr: string; esDomingo: boolean;
  esFeriado: boolean; feriadoMotivo: string;
  estaBloqueado: boolean; esPasado: boolean;
}

interface RangoBloqueo {
  fecha: string; horaInicio: string; horaFin: string; motivo: string;
}

@Component({
  selector: 'app-nueva-cita',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent, RouterModule],
  templateUrl: './nueva-cita.html',
  styleUrl: './nueva-cita.css'
})
export class NuevaCitaComponent implements OnInit {

  especialista: Especialista | null = null;
  fecha = '';
  hora = '';
  cargando = false;
  mensaje = '';
  tipoMensaje: 'exito' | 'error' | '' = '';
  esDomingoHoy = false;

  semana: DiaSemana[] = [];
  feriadosMap = new Map<string, string>();
  bloqueadosSet = new Set<string>();
  rangos: RangoBloqueo[] = [];
  horasOcupadas: string[] = [];

  lunesActual = this.getLunesDeHoy();

  mesesNombre = ['Ene','Feb','Mar','Abr','May','Jun',
                 'Jul','Ago','Sep','Oct','Nov','Dic'];

  mesesNombreCompleto = ['Enero','Febrero','Marzo','Abril','Mayo','Junio',
                         'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];

  horasDisponibles = [
    '07:00','07:30','08:00','08:30','09:00','09:30',
    '10:00','10:30','11:00','11:30','12:00','12:30',
    '13:00','13:30','14:00','14:30','15:00','15:30','16:00'
  ];

  // ✅ CORREGIDO: límite = fin del mes siguiente (solo 1 mes adelante)
  get fechaMaxima(): Date {
    const hoy = new Date();
    const mesSiguiente = hoy.getMonth() + 1; // 0-based: si mayo=4, siguiente=5 (junio)
    const anio = mesSiguiente > 11 ? hoy.getFullYear() + 1 : hoy.getFullYear();
    const mesReal = mesSiguiente % 12;
    // Último día del mes siguiente: día 0 del mes que le sigue
    return new Date(anio, mesReal + 1, 0);
  }

  // ✅ Texto del mes límite para mostrar en el aviso
  get mesLimiteTexto(): string {
    const limite = this.fechaMaxima;
    return `${this.mesesNombreCompleto[limite.getMonth()]} ${limite.getFullYear()}`;
  }

  get labelSemana(): string {
    const fin = new Date(this.lunesActual);
    fin.setDate(fin.getDate() + 6);
    return `${this.lunesActual.getDate()} ${this.mesesNombre[this.lunesActual.getMonth()]} — ${fin.getDate()} ${this.mesesNombre[fin.getMonth()]} ${fin.getFullYear()}`;
  }

  get fechaFormateada(): string {
    if (!this.fecha) return '';
    const [y, m, d] = this.fecha.split('-');
    return `${d} de ${this.mesesNombre[+m - 1]} de ${y}`;
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private citaService: CitaService,
    private especialistaService: EspecialistaService,
    private http: HttpClient,
    private contadorService: CitaContadorService
  ) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('especialistaId'));
    this.especialistaService.obtenerPorId(id).subscribe({
      next: (e) => {
        this.especialista = e;
        this.cargarSemana();
        const hoy = new Date();
        hoy.setHours(0, 0, 0, 0);
        if (hoy.getDay() === 0) {
          this.esDomingoHoy = true;
          this.fecha = '';
        } else {
          this.esDomingoHoy = false;
          this.fecha = this.toDateStr(hoy);
          this.cargarHorasOcupadas();
        }
      },
      error: () => this.router.navigate(['/dashboard'])
    });
  }

  getLunesDeHoy(): Date {
    const hoy = new Date(); hoy.setHours(0,0,0,0);
    const dia = hoy.getDay();
    const diff = dia === 0 ? -6 : 1 - dia;
    hoy.setDate(hoy.getDate() + diff);
    return hoy;
  }

  toDateStr(d: Date): string {
    const m = String(d.getMonth()+1).padStart(2,'0');
    const dd = String(d.getDate()).padStart(2,'0');
    return `${d.getFullYear()}-${m}-${dd}`;
  }

  cargarSemana() {
    const inicio = this.toDateStr(this.lunesActual);
    const fin = new Date(this.lunesActual); fin.setDate(fin.getDate() + 6);
    const finStr = this.toDateStr(fin);
    this.http.get<any>(`https://postamedica-backend.onrender.com/api/calendario/disponibilidad`, {
      params: { especialistaId: String(this.especialista!.id), inicio, fin: finStr }
    }).subscribe({
      next: (data) => {
        this.feriadosMap = new Map((data.feriados ?? []).map((f: any) =>
          typeof f === 'string' ? [f, 'Feriado'] : [f.fecha, f.motivo]
        ));
        this.bloqueadosSet = new Set(data.bloqueados ?? []);
        this.rangos = data.rangos ?? [];
        this.construirSemana();
      },
      error: () => { this.rangos = []; this.construirSemana(); }
    });
  }

  construirSemana() {
    const hoy = new Date(); hoy.setHours(0,0,0,0);
    const diasNombre = ['Dom','Lun','Mar','Mié','Jue','Vie','Sáb'];
    this.semana = [];
    for (let i = 0; i < 7; i++) {
      const d = new Date(this.lunesActual);
      d.setDate(d.getDate() + i);
      const fechaStr = this.toDateStr(d);
      const superaLimite = d > this.fechaMaxima;
      this.semana.push({
        nombre: diasNombre[d.getDay()], numero: d.getDate(),
        mes: this.mesesNombre[d.getMonth()], fechaStr,
        esDomingo: d.getDay() === 0,
        esFeriado: this.feriadosMap.has(fechaStr),
        feriadoMotivo: this.feriadosMap.get(fechaStr) ?? '',
        estaBloqueado: this.bloqueadosSet.has(fechaStr) || superaLimite,
        esPasado: d < hoy
      });
    }
  }

  semanaAnterior() {
    if (this.lunesActual <= this.getLunesDeHoy()) return;
    this.lunesActual = new Date(this.lunesActual);
    this.lunesActual.setDate(this.lunesActual.getDate() - 7);
    this.fecha = ''; this.hora = '';
    this.cargarSemana();
  }

  semanaSiguiente() {
    const lunesSiguiente = new Date(this.lunesActual);
    lunesSiguiente.setDate(lunesSiguiente.getDate() + 7);
    // ✅ Bloquear si el lunes siguiente ya supera la fecha máxima
    if (lunesSiguiente > this.fechaMaxima) return;
    this.lunesActual = lunesSiguiente;
    this.fecha = ''; this.hora = '';
    this.cargarSemana();
  }

  seleccionarDia(dia: DiaSemana) {
    if (dia.esDomingo || dia.esFeriado || dia.estaBloqueado || dia.esPasado) return;
    this.esDomingoHoy = false;
    this.fecha = dia.fechaStr;
    this.hora = '';
    this.cargarHorasOcupadas();
  }

  cargarHorasOcupadas() {
    this.citaService.listarPorEspecialista(this.especialista!.id!).subscribe({
      next: (citas: any[]) => {
        this.horasOcupadas = citas
          .filter((c: any) =>
            c.fecha === this.fecha &&
            ['PROGRAMADA','POR_CONFIRMAR','NO_ASISTIDA','ATENDIDA'].includes(c.estado)
          )
          .map((c: any) => c.hora.substring(0,5));
      },
      error: () => { this.horasOcupadas = []; }
    });
  }

  // ✅ Hora pasada (solo si es hoy)
  esHoraPasada(hora: string): boolean {
    const hoy = new Date();
    const fechaHoy = this.toDateStr(hoy);
    if (this.fecha !== fechaHoy) return false;
    const horaActual = `${String(hoy.getHours()).padStart(2,'0')}:${String(hoy.getMinutes()).padStart(2,'0')}`;
    return hora <= horaActual;
  }

  esCeldaBloqueada(hora: string): boolean {
    if (this.esHoraPasada(hora)) return true;
    return this.rangos.some(r =>
      r.fecha === this.fecha &&
      hora >= r.horaInicio.substring(0,5) &&
      hora < r.horaFin.substring(0,5)
    );
  }

  motivoRango(hora: string): string {
    if (this.esHoraPasada(hora)) return 'Hora pasada';
    const r = this.rangos.find(r =>
      r.fecha === this.fecha &&
      hora >= r.horaInicio.substring(0,5) &&
      hora < r.horaFin.substring(0,5)
    );
    return r?.motivo ?? '';
  }

  esHoraDisponible(hora: string): boolean {
    return !this.horasOcupadas.includes(hora) && !this.esCeldaBloqueada(hora);
  }

  fotoUrl(foto?: string): string {
    return foto ? `https://postamedica-backend.onrender.com${foto}` : '';
  }

  confirmar() {
    if (!this.fecha || !this.hora) { this.mostrar('Selecciona fecha y hora', 'error'); return; }
    const paciente = JSON.parse(localStorage.getItem('paciente_logueado') || '{}');
    this.cargando = true;
    this.citaService.agendar({
      pacienteId: paciente.id,
      especialistaId: this.especialista!.id!,
      fecha: this.fecha,
      hora: this.hora + ':00'
    }).subscribe({
      next: () => {
        this.contadorService.refrescar(paciente.id);
        this.mostrar('¡Cita agendada correctamente!', 'exito');
        setTimeout(() => this.router.navigate(['/mis-citas']), 1500);
      },
      error: (err) => {
        this.cargando = false;
        const msg = typeof err.error === 'string'
          ? err.error : (err.error?.message || 'Error al agendar la cita');
        this.mostrar(msg, 'error');
      }
    });
  }

  private mostrar(msg: string, tipo: 'exito' | 'error') {
    this.mensaje = msg; this.tipoMensaje = tipo;
    setTimeout(() => { this.mensaje = ''; this.tipoMensaje = ''; }, 3500);
  }
}