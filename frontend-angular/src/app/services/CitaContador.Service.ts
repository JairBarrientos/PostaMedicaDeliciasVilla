import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { CitaService } from './cita.service';

@Injectable({ providedIn: 'root' })
export class CitaContadorService {

  private _semana = new BehaviorSubject<number>(0);
  private _mes    = new BehaviorSubject<number>(0);

  semana$ = this._semana.asObservable();
  mes$    = this._mes.asObservable();

  readonly maxSemana = 2;
  readonly maxMes    = 8;

  constructor(private citaService: CitaService) {}

  refrescar(pacienteId: number) {
    this.citaService.listarPorPaciente(pacienteId).subscribe(citas => {

      const activas = citas.filter(c =>
        c.estado === 'PROGRAMADA' || c.estado === 'POR_CONFIRMAR'
      );

      const hoy = new Date();
      const anio  = hoy.getFullYear();
      const mes   = hoy.getMonth(); // 0-11

      // Semana: lunes a domingo
      const lunes = new Date(hoy);
      lunes.setDate(hoy.getDate() - ((hoy.getDay() + 6) % 7));
      lunes.setHours(0, 0, 0, 0);
      const domingo = new Date(lunes);
      domingo.setDate(lunes.getDate() + 6);
      domingo.setHours(23, 59, 59, 999);

      const countSemana = activas.filter(c => {
        const f = new Date(c.fecha + 'T00:00:00');
        return f >= lunes && f <= domingo;
      }).length;

      // Mes: comparar año y mes directamente desde el string YYYY-MM-DD
      const mesStr  = String(mes + 1).padStart(2, '0');   // "04"
      const prefijo = `${anio}-${mesStr}`;                 // "2026-04"

      const countMes = activas.filter(c =>
        c.fecha.startsWith(prefijo)
      ).length;

      console.log('Activas:', activas.length, '| Semana:', countSemana, '| Mes:', countMes, '| Prefijo:', prefijo);

      this._semana.next(countSemana);
      this._mes.next(countMes);
    });
  }
}