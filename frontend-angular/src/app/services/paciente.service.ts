import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Paciente } from '../model/paciente';

@Injectable({ providedIn: 'root' })
export class PacienteService {

  private apiUrl = 'https://postamedica-backend.onrender.com/api/usuarios';

  constructor(private http: HttpClient) {}

  // Registro de paciente (queda en PENDIENTE)
  registrar(paciente: Paciente): Observable<any> {
    return this.http.post(`${this.apiUrl}/registrar`, paciente);
  }
}