import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { InactividadService } from './inactividad.service';

export const especialistaGuard = () => {
  const router = inject(Router);
  const inactividad = inject(InactividadService);
  const raw = localStorage.getItem('especialista_logueado');

  if (!raw) {
    router.navigate(['/auth']);
    return false;
  }

  const especialista = JSON.parse(raw);

  if (!especialista.rol || especialista.rol.trim().toUpperCase() !== 'ESPECIALISTA') {
    router.navigate(['/auth']);
    return false;
  }

  inactividad.iniciar(); // asegura que el timer esté activo
  return true;
};