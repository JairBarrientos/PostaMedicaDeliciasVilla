import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ModoDaltonismo = 'normal' | 'protanopia' | 'deuteranopia' | 'tritanopia';

@Injectable({ providedIn: 'root' })
export class DaltonismoService {
  private readonly STORAGE_KEY = 'modo_daltonismo';
  private modoSubject = new BehaviorSubject<ModoDaltonismo>(this.obtenerModoGuardado());
  modo$ = this.modoSubject.asObservable();

  constructor() {
    this.aplicarModo(this.modoSubject.value);
  }

  private obtenerModoGuardado(): ModoDaltonismo {
    const guardado = localStorage.getItem(this.STORAGE_KEY) as ModoDaltonismo | null;
    return guardado ?? 'normal';
  }

  get modoActual(): ModoDaltonismo {
    return this.modoSubject.value;
  }

  cambiarModo(modo: ModoDaltonismo) {
    this.modoSubject.next(modo);
    localStorage.setItem(this.STORAGE_KEY, modo);
    this.aplicarModo(modo);
  }

  private aplicarModo(modo: ModoDaltonismo) {
    const contenedor = document.getElementById('contenido-filtrable');
    if (!contenedor) return;
    contenedor.classList.remove('modo-protanopia', 'modo-deuteranopia', 'modo-tritanopia');
    if (modo !== 'normal') {
      contenedor.classList.add('modo-' + modo);
    }
  }
}