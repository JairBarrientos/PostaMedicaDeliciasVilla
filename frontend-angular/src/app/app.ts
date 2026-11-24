import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SesionAvisoComponent } from './components/shared/sesion-aviso/sesion-aviso.component';
import { FlotantesComponent } from './components/shared/flotantes/flotantes.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, SesionAvisoComponent, FlotantesComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  title = 'postamedica-frontend';
}