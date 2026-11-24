import { Routes } from '@angular/router';
import { AuthComponent } from './components/auth/auth';
import { DashboardComponent } from './components/dashboard/dashboard';
import { AdminComponent } from './components/admin/admin';
import { EspecialistaComponent } from './components/especialista/especialista';
import { authGuard } from './services/auth-guard';
import { adminGuard } from './services/admin-guard';
import { especialistaGuard } from './services/especialista-guard';

export const routes: Routes = [
  // Pública
  { path: 'auth', component: AuthComponent },

  // Paciente
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  {
    path: 'nueva-cita/:especialistaId',
    loadComponent: () =>
      import('./components/nueva-cita/nueva-cita').then(m => m.NuevaCitaComponent),
    canActivate: [authGuard]
  },
  {
    path: 'mis-citas',
    loadComponent: () =>
      import('./components/mis-citas/mis-citas').then(m => m.MisCitasComponent),
    canActivate: [authGuard]
  },
  {
    path: 'chat',
    loadComponent: () =>
      import('./components/chat/chat').then(m => m.ChatComponent),
    canActivate: [authGuard]
  },

  // Admin
  { path: 'admin', component: AdminComponent, canActivate: [adminGuard] },

  // Especialista
  { path: 'especialista', component: EspecialistaComponent, canActivate: [especialistaGuard] },

  // Redirecciones
  { path: '', redirectTo: 'auth', pathMatch: 'full' },
  { path: '**', redirectTo: 'auth' }
];