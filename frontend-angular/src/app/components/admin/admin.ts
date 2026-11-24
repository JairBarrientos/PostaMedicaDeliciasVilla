import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { UsuarioService } from '../../services/usuario.service';
import { EspecialistaService } from '../../services/especialista.service';
import { ReporteService } from '../../services/reporte.service';
import { CitaService } from '../../services/cita.service';
import { Especialista } from '../../model/especialista';
import { AdminCalendarioComponent } from './admin-calendario.component';
import { SabiasQueService, SabiasQueItem } from '../../services/sabiasque.service';
import { GaleriaPortadaService, GaleriaPortada } from '../../services/galeria-portada.service';
import { forkJoin, of } from 'rxjs';
import { switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, AdminCalendarioComponent],
  templateUrl: './admin.html',
  styleUrl: './admin.css'
})
export class AdminComponent implements OnInit {

  tab: 'pendientes' | 'usuarios' | 'especialistas' | 'calendario' | 'sabiasque' | 'galeria' = 'pendientes';

  pacientesPendientes: any[] = [];
  todosUsuarios: any[] = [];
  especialistas: Especialista[] = [];

  // Filtros panel usuarios
  filtroBusqueda = '';
  filtroEstado   = '';

  creandoEspecialista = false;
  nuevoEsp: Especialista = { nombres: '', apellidos: '', especialidad: '', correo: '', password: '', dni: '', telefono: '' };
  archivoNuevo: File | null = null;
  previewNuevo: string | null = null;

  modalAbierto = false;
  espEditando: Especialista | null = null;
  telefonoEditando = '';
  archivoEditando: File | null = null;
  previewEditando: string | null = null;

  guardando = false;
  mensajeExito: string | null = null;

  sabiasQueItems: SabiasQueItem[] = [];
  nuevoDato: string = '';
  agregandoDato: boolean = false;
  guardandoDatos: boolean = false;

  galeriaItems: GaleriaPortada[] = [];
  ordenNuevoSlide: number = 1;
  archivoSlide: File | null = null;
  previewSlide: string | null = null;
  subiendoSlide: boolean = false;

  dragIndex: number = -1;

  constructor(
    private usuarioService: UsuarioService,
    private especialistaService: EspecialistaService,
    private citaService: CitaService,
    private router: Router,
    private sabiasQueService: SabiasQueService,
    private reporteService: ReporteService,
    private galeriaService: GaleriaPortadaService
  ) {}

  ngOnInit() { this.cargarTab('pendientes'); }

  cargarTab(t: 'pendientes' | 'usuarios' | 'especialistas' | 'calendario' | 'sabiasque' | 'galeria') {
    this.tab = t;
    this.cerrarModal();
    if (t === 'pendientes') {
      this.usuarioService.listarPorEstado('PENDIENTE').subscribe(d => this.pacientesPendientes = d);
    } else if (t === 'usuarios') {
      this.filtroBusqueda = '';
      this.filtroEstado   = '';
      this.usuarioService.listarTodos().subscribe(d => this.todosUsuarios = d);
    } else if (t === 'especialistas') {
      this.especialistaService.listarTodos().subscribe(d => this.especialistas = d);
    } else if (t === 'sabiasque') {
      this.sabiasQueService.listarCompleto().subscribe(data => this.sabiasQueItems = data);
      this.agregandoDato = false;
      this.nuevoDato = '';
    } else if (t === 'galeria') {
      this.galeriaService.listar().subscribe(data => {
        this.galeriaItems = data.sort((a, b) => a.orden - b.orden);
      });
      this.archivoSlide = null;
      this.previewSlide = null;
      this.ordenNuevoSlide = this.siguienteOrdenDisponible();
    }
  }

  // ── Filtros usuarios ──────────────────────────────────────────────────────

  usuariosFiltrados(): any[] {
    return this.todosUsuarios.filter(u => {
      const texto = this.filtroBusqueda.toLowerCase();
      const coincideTexto = !texto ||
        u.nombreCompleto?.toLowerCase().includes(texto) ||
        u.dni?.includes(texto) ||
        u.correo?.toLowerCase().includes(texto);
      const coincideEstado = !this.filtroEstado || u.estado === this.filtroEstado;
      return coincideTexto && coincideEstado;
    });
  }

  usuariosFiltradosPorRol(rol: string): any[] {
    return this.usuariosFiltrados().filter(u => u.rol === rol);
  }

  contarEstado(estado: string): number {
    return this.todosUsuarios.filter(u => u.estado === estado).length;
  }

  estadoLabel(estado?: string): string {
    const map: Record<string, string> = {
      'APROBADO':  'Aprobado',
      'PENDIENTE': 'Pendiente',
      'RECHAZADO': 'Rechazado',
    };
    return map[estado ?? ''] ?? estado ?? '—';
  }

  // ── Descargas Excel ───────────────────────────────────────────────────────

  descargarUsuarios() {
    this.reporteService.exportarUsuarios(this.todosUsuarios);
  }

  descargarHistorial(paciente: any) {
    this.citaService.listarPorPaciente(paciente.id).subscribe(citas => {
      this.reporteService.exportarHistorialPaciente(paciente, citas);
    });
  }

  descargarReporteGeneral() {
    this.usuarioService.listarTodos().subscribe(usuarios => {
      fetch('http://localhost:8080/api/citas')
        .then(r => r.json())
        .then(citas => this.reporteService.exportarReporteGeneral(usuarios, citas));
    });
  }

  // ── Galería ───────────────────────────────────────────────────────────────

  siguienteOrdenDisponible(): number {
    const ordenes = this.galeriaItems.map(g => g.orden);
    for (let i = 1; i <= 5; i++) {
      if (!ordenes.includes(i)) return i;
    }
    return 1;
  }

  onArchivoSlide(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    this.archivoSlide = input.files[0];
    const reader = new FileReader();
    reader.onload = e => this.previewSlide = e.target?.result as string;
    reader.readAsDataURL(this.archivoSlide);
  }

  subirSlide() {
    if (!this.archivoSlide) { alert('Selecciona una imagen'); return; }
    if (this.galeriaItems.length >= 5) { alert('Ya tienes 5 imágenes. Elimina una para agregar otra.'); return; }
    this.subiendoSlide = true;
    this.galeriaService.subir(this.ordenNuevoSlide, this.archivoSlide).subscribe({
      next: () => {
        this.subiendoSlide = false;
        this.archivoSlide = null;
        this.previewSlide = null;
        this.mostrarExito('Imagen subida correctamente');
        this.galeriaService.listar().subscribe(data => {
          this.galeriaItems = data.sort((a, b) => a.orden - b.orden);
          this.ordenNuevoSlide = this.siguienteOrdenDisponible();
        });
      },
      error: (err) => {
        this.subiendoSlide = false;
        alert(err.error || 'Error al subir imagen');
      }
    });
  }

  eliminarSlide(id: number) {
    if (!confirm('¿Eliminar esta imagen?')) return;
    this.galeriaService.eliminar(id).subscribe({
      next: () => {
        this.mostrarExito('Imagen eliminada correctamente');
        this.galeriaService.listar().subscribe(data => {
          this.galeriaItems = data.sort((a, b) => a.orden - b.orden);
          this.ordenNuevoSlide = this.siguienteOrdenDisponible();
        });
      }
    });
  }

  slideUrl(ruta: string): string { return this.galeriaService.fotoUrl(ruta); }

  onDragStart(index: number) { this.dragIndex = index; }

  onDrop(dropIndex: number) {
    if (this.dragIndex === dropIndex) return;
    const item = this.galeriaItems.splice(this.dragIndex, 1)[0];
    this.galeriaItems.splice(dropIndex, 0, item);
    this.galeriaItems = this.galeriaItems.map((g, i) => ({ ...g, orden: i + 1 }));
    this.dragIndex = -1;
  }

  // ── Pacientes ─────────────────────────────────────────────────────────────

  aprobar(id: number) { this.usuarioService.aprobar(id).subscribe(() => this.cargarTab('pendientes')); }
  rechazar(id: number) { this.usuarioService.rechazar(id).subscribe(() => this.cargarTab('pendientes')); }

  // ── Especialistas ─────────────────────────────────────────────────────────

  onFotoNueva(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    this.archivoNuevo = input.files[0];
    const reader = new FileReader();
    reader.onload = e => this.previewNuevo = e.target?.result as string;
    reader.readAsDataURL(this.archivoNuevo);
  }

  guardarEspecialista() {
    const { nombres, apellidos, especialidad, correo, password } = this.nuevoEsp;
    if (!nombres || !apellidos || !especialidad || !correo || !password) return;
    if (!confirm(`¿Registrar al especialista "${nombres} ${apellidos}"?`)) return;
    this.guardando = true;
    this.especialistaService.crear(this.nuevoEsp).pipe(
      switchMap(esp => this.archivoNuevo
        ? this.especialistaService.subirFoto(esp.id!, this.archivoNuevo!)
        : of(null))
    ).subscribe({
      next: () => {
        this.guardando = false;
        this.creandoEspecialista = false;
        this.archivoNuevo = null; this.previewNuevo = null;
        this.nuevoEsp = { nombres: '', apellidos: '', especialidad: '', correo: '', password: '', dni: '', telefono: '' };
        this.mostrarExito('Especialista registrado correctamente');
        this.cargarTab('especialistas');
      },
      error: () => { this.guardando = false; }
    });
  }

  cancelarNuevo() {
    this.creandoEspecialista = false;
    this.archivoNuevo = null; this.previewNuevo = null;
    this.nuevoEsp = { nombres: '', apellidos: '', especialidad: '', correo: '', password: '', dni: '', telefono: '' };
  }

  abrirModal(e: Especialista) {
    this.espEditando = { ...e };
    this.telefonoEditando = e.telefono ?? '';
    this.archivoEditando = null;
    this.previewEditando = e.foto ? this.fotoUrl(e.foto) : null;
    this.modalAbierto = true;
  }

  cerrarModal() {
    this.modalAbierto = false;
    this.espEditando = null;
    this.archivoEditando = null;
    this.previewEditando = null;
  }

  onFotoEditando(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    this.archivoEditando = input.files[0];
    const reader = new FileReader();
    reader.onload = e => this.previewEditando = e.target?.result as string;
    reader.readAsDataURL(this.archivoEditando);
  }

  guardarEdicion() {
    if (!this.espEditando) return;
    const hayFoto = !!this.archivoEditando;
    const telefonoNuevo = this.telefonoEditando.trim();
    const telefonoOriginal = (this.espEditando.telefono ?? '').trim();
    const hayTelefono = telefonoNuevo !== telefonoOriginal;
    if (!hayFoto && !hayTelefono) { alert('No realizaste ningún cambio.'); return; }
    if (!confirm(`¿Guardar los cambios del Dr./Dra. "${this.espEditando.nombreCompleto}"?`)) return;
    const id = this.espEditando.id!;
    const tareas: any[] = [];
    if (hayFoto) tareas.push(this.especialistaService.subirFoto(id, this.archivoEditando!));
    if (hayTelefono) tareas.push(this.especialistaService.actualizarCelular(id, telefonoNuevo));
    this.guardando = true;
    forkJoin(tareas).subscribe({
      next: () => {
        this.guardando = false;
        this.cerrarModal();
        this.mostrarExito('Cambios guardados correctamente');
        this.cargarTab('especialistas');
      },
      error: (err) => {
        this.guardando = false;
        alert('Error al guardar:\nStatus: ' + err?.status + ' | ' + err?.message);
      }
    });
  }

  toggleEstado(e: Especialista) {
    const nuevoEstado = e.estado === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
    const accion = nuevoEstado === 'INACTIVO' ? 'desactivar' : 'activar';
    if (!confirm(`¿Deseas ${accion} al Dr./Dra. "${e.nombreCompleto}"?`)) return;
    this.especialistaService.cambiarEstado(e.id!, nuevoEstado).subscribe({
      next: () => {
        this.mostrarExito(`Especialista ${nuevoEstado === 'ACTIVO' ? 'activado' : 'desactivado'} correctamente`);
        this.cargarTab('especialistas');
      }
    });
  }

  // ── Sabías que ────────────────────────────────────────────────────────────

  eliminarDato(id: number) {
    if (!confirm('¿Eliminar este dato?')) return;
    this.sabiasQueService.eliminar(id).subscribe({
      next: () => {
        this.mostrarExito('Dato eliminado correctamente');
        this.sabiasQueService.listarCompleto().subscribe(data => this.sabiasQueItems = data);
        this.sabiasQueService.cargarDesdeApi();
      }
    });
  }

  guardarSabiasQue() {
    if (!this.nuevoDato.trim()) return;
    this.guardandoDatos = true;
    this.sabiasQueService.crear(this.nuevoDato.trim()).subscribe({
      next: () => {
        this.guardandoDatos = false;
        this.nuevoDato = '';
        this.agregandoDato = false;
        this.mostrarExito('Dato guardado correctamente');
        this.sabiasQueService.listarCompleto().subscribe(data => this.sabiasQueItems = data);
        this.sabiasQueService.cargarDesdeApi();
      },
      error: (err) => {
        this.guardandoDatos = false;
        alert(err.error || 'Error al guardar');
      }
    });
  }

  // ── Utilidades ────────────────────────────────────────────────────────────

  fotoUrl(foto?: string): string {
    return foto ? `http://localhost:8080${foto}` : '';
  }

  mostrarExito(msg: string) {
    this.mensajeExito = msg;
    setTimeout(() => this.mensajeExito = null, 3500);
  }

  logout() {
    localStorage.removeItem('admin_logueado');
    this.router.navigate(['/auth']);
  }
}