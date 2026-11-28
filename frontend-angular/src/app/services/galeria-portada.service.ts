import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface GaleriaPortada {
  id: number;
  rutaImagen: string;
  orden: number;
}

@Injectable({ providedIn: 'root' })
export class GaleriaPortadaService {

  private api = 'https://postamedica-backend.onrender.com/api/galeria-portada';

  constructor(private http: HttpClient) {}

  listar(): Observable<GaleriaPortada[]> {
    return this.http.get<GaleriaPortada[]>(this.api);
  }

  subir(orden: number, archivo: File): Observable<GaleriaPortada> {
    const formData = new FormData();
    formData.append('orden', orden.toString());
    formData.append('archivo', archivo);
    return this.http.post<GaleriaPortada>(this.api, formData);
  }

  eliminar(id: number): Observable<any> {
    return this.http.delete(`${this.api}/${id}`, { responseType: 'text' });
  }

  fotoUrl(ruta: string): string {
    return `https://postamedica-backend.onrender.com${ruta}`;
  }
}