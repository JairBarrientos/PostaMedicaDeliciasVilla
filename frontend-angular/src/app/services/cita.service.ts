import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Cita } from '../model/cita';

@Injectable({ providedIn: 'root' })
export class CitaService {

  private api = 'https://postamedica-backend.onrender.com/api/citas';

  constructor(private http: HttpClient) {}

  agendar(data: { pacienteId: number; especialistaId: number; fecha: string; hora: string }): Observable<Cita> {
    return this.http.post<Cita>(this.api, data);
  }

  // Especialista acepta la cita → POR_CONFIRMAR → PROGRAMADA
  aceptar(id: number): Observable<Cita> {
    return this.http.put<Cita>(`${this.api}/${id}/aceptar`, {});
  }

  // Especialista rechaza → CANCELADA
  rechazar(id: number, mensaje: string): Observable<Cita> {
    return this.http.put<Cita>(`${this.api}/${id}/rechazar`, { mensaje });
  }

  cancelar(id: number, mensaje: string): Observable<Cita> {
    return this.http.put<Cita>(`${this.api}/${id}/cancelar`, { mensaje });
  }

  actualizarEstado(id: number, estado: string): Observable<Cita> {
    return this.http.put<Cita>(`${this.api}/${id}/estado/${estado}`, {});
  }

  listarPorPaciente(pacienteId: number): Observable<Cita[]> {
    return this.http.get<Cita[]>(`${this.api}/paciente/${pacienteId}`);
  }

  listarPorEspecialista(especialistaId: number): Observable<Cita[]> {
    return this.http.get<Cita[]>(`${this.api}/especialista/${especialistaId}`);
  }
}