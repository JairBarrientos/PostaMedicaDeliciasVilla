import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { HeaderComponent } from '../shared/header/header.component';
import { EspecialistaService } from '../../services/especialista.service';
import { Especialista } from '../../model/especialista';
import { SabiasQueService } from '../../services/sabiasque.service';
import { GaleriaPortadaService, GaleriaPortada } from '../../services/galeria-portada.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, HeaderComponent],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class DashboardComponent implements OnInit, OnDestroy {

  especialistas: Especialista[] = [];
  cargando = true;
  nombreUsuario = '';

  slides: GaleriaPortada[] = [];
  slideActual = 0;
  private slideInterval: any;

  sabiasQue: string[] = [];
  sabiaQueIndex = 0;
  sabiaQueActual = '';
  animandoTexto = false;
  private sabiaQueInterval: any;
  private sabiasQueSub!: Subscription;

  constructor(
    private especialistaService: EspecialistaService,
    private router: Router,
    private sabiasQueService: SabiasQueService,
    private galeriaService: GaleriaPortadaService,
    private http: HttpClient
  ) {}

  ngOnInit() {
    const raw = localStorage.getItem('paciente_logueado');
    if (raw) {
      const paciente = JSON.parse(raw);
      this.nombreUsuario = paciente.nombreCompleto?.split(' ')[0] ?? 'Paciente';
    }

    this.especialistaService.listarActivos().subscribe({
      next: (data) => { this.especialistas = data; this.cargando = false; },
      error: () => { this.cargando = false; }
    });

    // Cargar galería desde BD
    this.galeriaService.listar().subscribe({
      next: (data) => {
        this.slides = data.sort((a, b) => a.orden - b.orden);
        if (this.slides.length > 0) {
          this.slideInterval = setInterval(() => {
            this.slideActual = (this.slideActual + 1) % this.slides.length;
          }, 5000);
        }
      },
      error: () => { this.slides = []; }
    });

    // Cargar sabías que desde BD
    this.http.get<any[]>('http://localhost:8080/api/sabias-que').subscribe({
      next: (data) => {
        this.sabiasQue = data.map(d => d.contenido);
        if (this.sabiasQue.length > 0) {
          const diaDelAnio = Math.floor(
            (Date.now() - new Date(new Date().getFullYear(), 0, 0).getTime()) / 86400000
          );
          this.sabiaQueIndex = diaDelAnio % this.sabiasQue.length;
          this.sabiaQueActual = this.sabiasQue[this.sabiaQueIndex];
          this.sabiaQueInterval = setInterval(() => {
            this.cambiarSabiaQue((this.sabiaQueIndex + 1) % this.sabiasQue.length);
          }, 10000);
        }
      },
      error: () => { this.sabiasQue = []; }
    });
  }

  irASlide(i: number) {
    this.slideActual = i;
  }

  irASabiaQue(i: number) {
    this.cambiarSabiaQue(i);
  }

  agendarCita(especialistaId: number) {
    this.router.navigate(['/nueva-cita', especialistaId]);
  }

  ngOnDestroy() {
    if (this.slideInterval) clearInterval(this.slideInterval);
    if (this.sabiaQueInterval) clearInterval(this.sabiaQueInterval);
    if (this.sabiasQueSub) this.sabiasQueSub.unsubscribe();
  }

  cambiarSabiaQue(index: number) {
    this.animandoTexto = true;
    setTimeout(() => {
      this.sabiaQueIndex = index;
      this.sabiaQueActual = this.sabiasQue[index] ?? '';
      this.animandoTexto = false;
    }, 400);
  }

  slideUrl(slide: GaleriaPortada): string {
    return this.galeriaService.fotoUrl(slide.rutaImagen);
  }

  fotoUrl(foto?: string): string {
    return foto ? `http://localhost:8080${foto}` : '';
  }

  irAgendar(especialistaId: number) {
    this.router.navigate(['/nueva-cita', especialistaId]);
  }
}