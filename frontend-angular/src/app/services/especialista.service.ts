import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Especialista } from '../model/especialista';

@Injectable({ providedIn: 'root' })
export class EspecialistaService {

  private apiUrl = 'https://postamedica-backend.onrender.com/api/especialistas';

  constructor(private http: HttpClient) {}

  // Listar todos (para el panel admin)
  listarTodos(): Observable<Especialista[]> {
    return this.http.get<Especialista[]>(this.apiUrl);
  }

  // Listar solo ACTIVOS (para el dashboard de pacientes)
  listarActivos(): Observable<Especialista[]> {
    return this.http.get<Especialista[]>(`${this.apiUrl}/activos`);
  }

  // Obtener por ID
  obtenerPorId(id: number): Observable<Especialista> {
    return this.http.get<Especialista>(`${this.apiUrl}/${id}`);
  }

  // Crear especialista (solo admin)
  crear(especialista: Especialista): Observable<Especialista> {
    return this.http.post<Especialista>(this.apiUrl, especialista);
  }

  // Subir / cambiar foto
  subirFoto(id: number, archivo: File): Observable<{ fotoUrl: string }> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    return this.http.post<{ fotoUrl: string }>(`${this.apiUrl}/${id}/foto`, formData);
  }

  // Actualizar celular — responseType: 'text' porque el backend devuelve String plano
  actualizarCelular(id: number, telefono: string): Observable<string> {
    return this.http.patch(`${this.apiUrl}/${id}/celular`, { telefono }, { responseType: 'text' });
  }

  // Cambiar estado ACTIVO / INACTIVO
  cambiarEstado(id: number, estado: 'ACTIVO' | 'INACTIVO'): Observable<Especialista> {
    return this.http.patch<Especialista>(`${this.apiUrl}/${id}/estado`, { estado });
  }

  // Eliminar especialista (solo admin)
  eliminar(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`, { responseType: 'text' });
  }
}