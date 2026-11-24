import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { InactividadService } from './inactividad.service';

export const adminGuard = () => {
  const router = inject(Router);
  const inactividad = inject(InactividadService);
  const raw = localStorage.getItem('admin_logueado');

  if (!raw) {
    router.navigate(['/auth']);
    return false;
  }

  const admin = JSON.parse(raw);

  if (!admin.rol || admin.rol.trim().toUpperCase() !== 'ADMIN') {
    router.navigate(['/auth']);
    return false;
  }

  inactividad.iniciar(); // asegura que el timer esté activo
  return true;
};