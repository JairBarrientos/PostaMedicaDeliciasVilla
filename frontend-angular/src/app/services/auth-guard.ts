import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { InactividadService } from './inactividad.service';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  const inactividad = inject(InactividadService);
  const paciente = localStorage.getItem('paciente_logueado');

  if (paciente) {
    inactividad.iniciar(); // asegura que el timer esté activo
    return true;
  }

  router.navigate(['/auth']);
  return false;
};