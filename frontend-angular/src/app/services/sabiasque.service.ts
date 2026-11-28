import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject } from 'rxjs';

export interface SabiasQueItem {
  id: number;
  contenido: string;
}

@Injectable({ providedIn: 'root' })
export class SabiasQueService {

  private api = 'https://postamedica-backend.onrender.com/api/sabias-que';
  private _datos$ = new BehaviorSubject<string[]>([]);
  datos$ = this._datos$.asObservable();

  get datos(): string[] {
    return this._datos$.value;
  }

  constructor(private http: HttpClient) {
    this.cargarDesdeApi();
  }

  cargarDesdeApi() {
    this.http.get<SabiasQueItem[]>(this.api).subscribe({
      next: (data) => this._datos$.next(data.map(d => d.contenido)),
      error: () => this._datos$.next([])
    });
  }

  listarCompleto() {
    return this.http.get<SabiasQueItem[]>(this.api);
  }

  crear(contenido: string) {
    return this.http.post<SabiasQueItem>(this.api, { contenido });
  }

  eliminar(id: number) {
    return this.http.delete(`${this.api}/${id}`);
  }

  guardar(datos: string[]): void {
    this._datos$.next([...datos]);
  }
}