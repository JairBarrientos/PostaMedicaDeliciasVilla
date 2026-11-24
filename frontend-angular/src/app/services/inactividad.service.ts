import { Injectable, NgZone, OnDestroy, inject } from '@angular/core';
import { Router } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class InactividadService implements OnDestroy {

  private readonly router  = inject(Router);
  private readonly ngZone  = inject(NgZone);

  private readonly TIEMPO_LIMITE_MS  = 5 * 60 * 1000; // ← cambia aquí los minutos
  private readonly AVISO_ANTES_MS    = 30 * 1000;      // aviso 30 seg antes
  private readonly EVENTOS_ACTIVIDAD = [
    'mousemove', 'mousedown', 'keydown', 'touchstart', 'scroll', 'click'
  ];

  private timerSesion: ReturnType<typeof setTimeout> | null = null;
  private timerAviso:  ReturnType<typeof setTimeout> | null = null;
  private timerCuenta: ReturnType<typeof setInterval> | null = null;
  private listeners:   (() => void)[] = [];
  private activo = false;

  // Estado público para que el componente overlay lo lea
  mostrarAviso   = false;
  segundosRestantes = 30;

  iniciar(): void {
    if (this.activo) return;
    this.activo = true;

    this.ngZone.runOutsideAngular(() => {
      const handler = () => this.resetTimer();
      this.EVENTOS_ACTIVIDAD.forEach(evento => {
        window.addEventListener(evento, handler, { passive: true });
        this.listeners.push(() => window.removeEventListener(evento, handler));
      });
    });

    this.resetTimer();
  }

  detener(): void {
    this.activo = false;
    this.ocultarAviso();
    this.limpiarTimers();
    this.listeners.forEach(fn => fn());
    this.listeners = [];
  }

  continuar(): void {
    this.ocultarAviso();
    this.resetTimer();
  }

  private resetTimer(): void {
    this.limpiarTimers();
    this.ocultarAviso();

    // Timer para mostrar el aviso
    this.timerAviso = setTimeout(() => {
      this.ngZone.run(() => this.mostrarAvisoCierre());
    }, this.TIEMPO_LIMITE_MS - this.AVISO_ANTES_MS);

    // Timer para cerrar sesión
    this.timerSesion = setTimeout(() => {
      this.ngZone.run(() => this.cerrarSesionPorInactividad());
    }, this.TIEMPO_LIMITE_MS);
  }

  private mostrarAvisoCierre(): void {
    this.mostrarAviso = true;
    this.segundosRestantes = Math.floor(this.AVISO_ANTES_MS / 1000);

    this.timerCuenta = setInterval(() => {
      this.ngZone.run(() => {
        this.segundosRestantes--;
        if (this.segundosRestantes <= 0) this.limpiarCuenta();
      });
    }, 1000);
  }

  private ocultarAviso(): void {
    this.mostrarAviso = false;
    this.segundosRestantes = Math.floor(this.AVISO_ANTES_MS / 1000);
    this.limpiarCuenta();
  }

  private limpiarCuenta(): void {
    if (this.timerCuenta !== null) { clearInterval(this.timerCuenta); this.timerCuenta = null; }
  }

  private limpiarTimers(): void {
    if (this.timerSesion !== null) { clearTimeout(this.timerSesion); this.timerSesion = null; }
    if (this.timerAviso  !== null) { clearTimeout(this.timerAviso);  this.timerAviso  = null; }
    this.limpiarCuenta();
  }

  private cerrarSesionPorInactividad(): void {
    this.detener();
    localStorage.removeItem('paciente_logueado');
    localStorage.removeItem('especialista_logueado');
    localStorage.removeItem('admin_logueado');
    this.router.navigate(['/auth'], { queryParams: { sesionExpirada: 'true' } });
  }

  ngOnDestroy(): void { this.detener(); }
}