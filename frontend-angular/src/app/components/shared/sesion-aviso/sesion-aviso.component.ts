import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InactividadService } from '../../../services/inactividad.service';

@Component({
  selector: 'app-sesion-aviso',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sesion-aviso.component.html',
  styleUrl: './sesion-aviso.component.css'
})
export class SesionAvisoComponent {
  readonly inactividad = inject(InactividadService);

  continuar() {
    this.inactividad.continuar();
  }
}