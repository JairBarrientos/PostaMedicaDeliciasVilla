import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ChatService {

  private base = 'https://postamedica-backend.onrender.com/api/chat';

  constructor(private http: HttpClient) {}

  enviar(data: any): Observable<any> {
    return this.http.post(this.base, data);
  }

  obtenerConversacion(pacienteId: number, especialistaId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}?pacienteId=${pacienteId}&especialistaId=${especialistaId}`);
  }

  contarNoLeidos(pacienteId: number): Observable<number> {
    return this.http.get<number>(`${this.base}/no-leidos/${pacienteId}`);
  }

  marcarComoLeidos(pacienteId: number, especialistaId: number): Observable<void> {
    return this.http.put<void>(
      `${this.base}/marcar-leidos?pacienteId=${pacienteId}&especialistaId=${especialistaId}`, {}
    );
  }

  especialistasConNoLeidos(pacienteId: number): Observable<number[]> {
    return this.http.get<number[]>(`${this.base}/especialistas-con-noLeidos/${pacienteId}`);
  }

  // ✅ NUEVO: verificar si el chat está habilitado
  chatHabilitado(pacienteId: number, especialistaId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.base}/habilitado?pacienteId=${pacienteId}&especialistaId=${especialistaId}`);
  }

  // ===== ESPECIALISTA =====
  // Devuelve { "pacienteId": cantidadNoLeidos }
  noLeidosPorPaciente(especialistaId: number): Observable<Record<string, number>> {
    return this.http.get<Record<string, number>>(`${this.base}/no-leidos-especialista/${especialistaId}`);
  }

  marcarComoLeidosEspecialista(pacienteId: number, especialistaId: number): Observable<void> {
    return this.http.put<void>(
      `${this.base}/marcar-leidos-especialista?pacienteId=${pacienteId}&especialistaId=${especialistaId}`, {}
    );
  }
}