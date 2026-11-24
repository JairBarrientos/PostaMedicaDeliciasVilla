import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class UsuarioService {

  private apiUrl = 'http://localhost:8080/api/usuarios';

  constructor(private http: HttpClient) {}

  // Login unificado — paciente, admin y especialista
  login(data: { identificador: string; password: string; rol: string }): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, data);
  }

  // Listar todos los usuarios (admin)
  listarTodos(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  // Listar por rol: PACIENTE, ADMIN
  listarPorRol(rol: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/rol/${rol}`);
  }

  // Listar por estado: PENDIENTE, APROBADO, RECHAZADO
  listarPorEstado(estado: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/estado/${estado}`);
  }

  // Aprobar paciente (admin)
  aprobar(id: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/aprobar`, {});
  }

  // Rechazar paciente (admin)
  rechazar(id: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/rechazar`, {});
  }

  // Eliminar usuario (admin)
  eliminar(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}