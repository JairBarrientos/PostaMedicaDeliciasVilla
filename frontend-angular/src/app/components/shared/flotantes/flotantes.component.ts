import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { DaltonismoService, ModoDaltonismo } from '../../../services/daltonismo.service';

@Component({
  selector: 'app-flotantes',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './flotantes.component.html',
  styleUrl: './flotantes.component.css'
})
export class FlotantesComponent implements OnInit, OnDestroy {

  private readonly daltonismo = inject(DaltonismoService);

  mostrarTelefono = false;
  mostrarPanelDaltonismo = false;
  modoActual: ModoDaltonismo = 'normal';
  private subDaltonismo?: Subscription;

  ngOnInit() {
    this.subDaltonismo = this.daltonismo.modo$.subscribe(m => this.modoActual = m);
  }

  elegirModo(modo: ModoDaltonismo) {
    this.daltonismo.cambiarModo(modo);
    this.mostrarPanelDaltonismo = false;
  }

  ngOnDestroy() {
    this.subDaltonismo?.unsubscribe();
  }
}