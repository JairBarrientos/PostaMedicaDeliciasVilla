import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { CitaContadorService } from '../../../services/CitaContador.Service';
import { InactividadService } from '../../../services/inactividad.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit, OnDestroy {

  private readonly router      = inject(Router);
  private readonly contador    = inject(CitaContadorService);
  private readonly inactividad = inject(InactividadService);

  usuario: any = null;
  rol = '';

  citasSemana = 0;
  citasMes    = 0;
  readonly maxSemana = 2;
  readonly maxMes    = 8;

  mensajesNoLeidos = 0;
  mostrarTelefono = false;
  private mensajesInterval: any;
  private subs: Subscription[] = [];

  ngOnInit() {
    const p = localStorage.getItem('paciente_logueado');
    const e = localStorage.getItem('especialista_logueado');
    const a = localStorage.getItem('admin_logueado');

    if (p)      { this.usuario = JSON.parse(p); this.rol = 'PACIENTE'; }
    else if (e) { this.usuario = JSON.parse(e); this.rol = 'ESPECIALISTA'; }
    else if (a) { this.usuario = JSON.parse(a); this.rol = 'ADMIN'; }

    if (this.rol === 'PACIENTE') {
      this.subs.push(this.contador.semana$.subscribe(n => this.citasSemana = n));
      this.subs.push(this.contador.mes$.subscribe(n => this.citasMes = n));
      this.contador.refrescar(this.usuario.id);

      this.cargarNoLeidos();
      this.mensajesInterval = setInterval(() => this.cargarNoLeidos(), 10000);
    }
  }

  cargarNoLeidos() {
    if (!this.usuario?.id) return;
    fetch(`https://postamedica-backend.onrender.com/api/chat/no-leidos/${this.usuario.id}`)
      .then(r => r.json())
      .then(n => this.mensajesNoLeidos = n)
      .catch(() => this.mensajesNoLeidos = 0);
  }

  get claseSemana(): string {
    if (this.citasSemana >= this.maxSemana) return 'lleno';
    if (this.citasSemana === 1)             return 'medio';
    return 'libre';
  }

  get claseMes(): string {
    if (this.citasMes >= this.maxMes) return 'lleno';
    if (this.citasMes >= 5)           return 'medio';
    return 'libre';
  }

  get nombreMesActual(): string {
    const meses = ['Enero','Febrero','Marzo','Abril','Mayo','Junio',
                   'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];
    return meses[new Date().getMonth()];
  }

  get lineaArriba(): string {
    const partes = this.usuario?.nombreCompleto?.trim().split(' ') ?? [];
    return partes.slice(0, Math.ceil(partes.length / 2)).join(' ');
  }

  get lineaAbajo(): string {
    const partes = this.usuario?.nombreCompleto?.trim().split(' ') ?? [];
    return partes.slice(Math.ceil(partes.length / 2)).join(' ');
  }

  ngOnDestroy() {
    this.subs.forEach(s => s.unsubscribe());
    clearInterval(this.mensajesInterval);
  }

  logout() {
    this.inactividad.detener();
    localStorage.removeItem('paciente_logueado');
    localStorage.removeItem('especialista_logueado');
    localStorage.removeItem('admin_logueado');
    this.router.navigate(['/auth']);
  }

  irAMisCitas()      { this.router.navigate(['/mis-citas']); }
  irADashboard()     { this.router.navigate(['/dashboard']); }
  irAChat()          { this.router.navigate(['/chat']); }
  irAEspecialistas() {
    this.router.navigate(['/dashboard']).then(() => {
      setTimeout(() => {
        const el = document.querySelector('.especialistas-section');
        if (el) {
          const top = el.getBoundingClientRect().top + window.scrollY - 180;
          window.scrollTo({ top, behavior: 'smooth' });
        }
      }, 100);
    });
  }
}