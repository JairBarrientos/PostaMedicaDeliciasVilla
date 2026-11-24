import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';

@Injectable({ providedIn: 'root' })
export class ReporteService {

  // ─── LABEL LEGIBLE PARA ESTADO ───────────────────────────
  private labelEstado(estado: string): string {
    const map: Record<string, string> = {
      'POR_CONFIRMAR': 'Por confirmar',
      'PROGRAMADA':    'Confirmada',
      'ATENDIDA':      'Atendida',
      'NO_ASISTIDA':   'No asistió',
      'CANCELADA':     'Cancelada',
      'SIN_RESPUESTA': 'Sin registro del especialista',
    };
    return map[estado] ?? estado;
  }

  // ─── USUARIOS ───
  exportarUsuarios(usuarios: any[]) {
    const data = usuarios.map(u => ({
      'ID':            u.id,
      'Nombres':       u.nombres ?? u.nombreCompleto?.split(' ').slice(0, Math.ceil(u.nombreCompleto.split(' ').length / 2)).join(' '),
      'Apellidos':     u.apellidos ?? u.nombreCompleto?.split(' ').slice(Math.ceil(u.nombreCompleto.split(' ').length / 2)).join(' '),
      'DNI':           u.dni,
      'Correo':        u.correo,
      'Teléfono':      u.telefono ?? '—',
      'Rol':           u.rol,
      'Estado':        u.estado,
      'Afiliado SIS':  u.afiliadoSis ? 'Sí' : 'No',
    }));
    this.descargar(data, 'Reporte_Usuarios', 'Usuarios');
  }

  // ─── HISTORIAL MÉDICO DE UN PACIENTE ───
  exportarHistorialPaciente(paciente: any, citas: any[]) {
    const infoPaciente = [
      { 'Campo': 'Nombre completo', 'Valor': paciente.nombreCompleto ?? '—' },
      { 'Campo': 'DNI',             'Valor': paciente.dni ?? '—' },
      { 'Campo': 'Correo',          'Valor': paciente.correo ?? '—' },
      { 'Campo': 'Teléfono',        'Valor': paciente.telefono ?? '—' },
      { 'Campo': 'Afiliado SIS',    'Valor': paciente.afiliadoSis ? 'Sí' : 'No' },
      { 'Campo': 'Estado',          'Valor': paciente.estado ?? '—' },
    ];

    const citasMapeadas = citas.map(c => ({
      'Código':              c.codigoCita ?? c.id,
      'Fecha':               c.fecha,
      'Hora':                c.hora?.substring(0, 5) ?? c.hora,
      'Especialista':        c.especialistaNombre ?? c.especialistaId,
      'Especialidad':        c.especialistaEspecialidad ?? '—',
      'Estado':              this.labelEstado(c.estado),
      'Motivo Cancelación':  c.mensajeCancelacion ?? '—',
      'Fecha Registro':      c.fechaCreacion ? c.fechaCreacion.substring(0, 10) : '—',
    }));

    const resumen = [
      { 'Concepto': 'Total citas',                   'Cantidad': citas.length },
      { 'Concepto': 'Atendidas',                     'Cantidad': citas.filter(c => c.estado === 'ATENDIDA').length },
      { 'Concepto': 'Confirmadas (pendientes)',       'Cantidad': citas.filter(c => c.estado === 'PROGRAMADA').length },
      { 'Concepto': 'Canceladas',                    'Cantidad': citas.filter(c => c.estado === 'CANCELADA').length },
      { 'Concepto': 'No asistió',                    'Cantidad': citas.filter(c => c.estado === 'NO_ASISTIDA').length },
      { 'Concepto': 'Sin registro del especialista', 'Cantidad': citas.filter(c => c.estado === 'SIN_RESPUESTA').length },
    ];

    const wb       = XLSX.utils.book_new();
    const wsDatos  = XLSX.utils.json_to_sheet(infoPaciente);
    const wsRes    = XLSX.utils.json_to_sheet(resumen);
    const wsCitas  = XLSX.utils.json_to_sheet(citasMapeadas.length ? citasMapeadas : [{ 'Sin registros': '—' }]);

    this.aplicarEstilos(wsDatos);
    this.aplicarEstilos(wsRes);
    this.aplicarEstilos(wsCitas);

    XLSX.utils.book_append_sheet(wb, wsDatos, 'Datos del Paciente');
    XLSX.utils.book_append_sheet(wb, wsRes,   'Resumen');
    XLSX.utils.book_append_sheet(wb, wsCitas, 'Historial de Citas');

    const nombre = (paciente.nombreCompleto ?? 'paciente').replace(/\s+/g, '_');
    XLSX.writeFile(wb, `Historial_Medico_${nombre}.xlsx`);
  }

  // ─── CITAS ───
  exportarCitas(citas: any[]) {
    const data = citas.map(c => ({
      'ID Cita':        c.id,
      'Fecha':          c.fecha,
      'Hora':           c.hora,
      'Paciente':       c.pacienteNombre ?? c.pacienteId,
      'DNI Paciente':   c.pacienteDni ?? '—',
      'Especialista':   c.especialistaNombre ?? c.especialistaId,
      'Estado':         this.labelEstado(c.estado),
      'Motivo Cancelación': c.mensajeCancelacion ?? '—',
      'Fecha Creación': c.fechaCreacion ?? '—',
    }));
    this.descargar(data, 'Reporte_Citas', 'Citas');
  }

  // ─── CITAS POR ESPECIALISTA ───
  exportarCitasEspecialista(citas: any[], nombreEspecialista: string) {
    const data = citas.map(c => ({
      'ID Cita':       c.id,
      'Fecha':         c.fecha,
      'Hora':          c.hora,
      'Paciente':      c.pacienteNombre ?? c.pacienteId,
      'DNI Paciente':  c.pacienteDni ?? '—',
      'Estado':        this.labelEstado(c.estado),
      'Motivo':        c.mensajeCancelacion ?? '—',
    }));
    const nombre = nombreEspecialista.replace(/\s+/g, '_');
    this.descargar(data, `Calendario_${nombre}`, `Citas de ${nombreEspecialista}`);
  }

  // ─── DISPONIBILIDAD ───
  exportarDisponibilidad(bloqueados: any[], feriados: any[], nombreEspecialista: string) {
    const dataBloqueados = bloqueados.map(b => ({
      'Fecha':       b.fecha,
      'Tipo':        b.tipo,
      'Motivo':      b.motivo ?? '—',
      'Hora Inicio': b.horaInicio ?? '—',
      'Hora Fin':    b.horaFin ?? '—',
    }));
    const dataFeriados = feriados.map(f => ({
      'Fecha':  f.fecha,
      'Motivo': f.motivo ?? '—',
    }));
    const wb = XLSX.utils.book_new();
    const wsBloqueados = XLSX.utils.json_to_sheet(dataBloqueados.length ? dataBloqueados : [{ 'Sin registros': '—' }]);
    const wsFeriados   = XLSX.utils.json_to_sheet(dataFeriados.length   ? dataFeriados   : [{ 'Sin registros': '—' }]);
    this.aplicarEstilos(wsBloqueados);
    this.aplicarEstilos(wsFeriados);
    const nombre = nombreEspecialista.replace(/\s+/g, '_');
    XLSX.utils.book_append_sheet(wb, wsBloqueados, 'Bloqueos');
    XLSX.utils.book_append_sheet(wb, wsFeriados,   'Feriados');
    XLSX.writeFile(wb, `Disponibilidad_${nombre}.xlsx`);
  }

  // ─── REPORTE GENERAL ───
  exportarReporteGeneral(usuarios: any[], citas: any[]) {
    const dataUsuarios = usuarios.map(u => ({
      'ID':           u.id,
      'Nombres':      u.nombres ?? '—',
      'Apellidos':    u.apellidos ?? '—',
      'DNI':          u.dni,
      'Correo':       u.correo,
      'Teléfono':     u.telefono ?? '—',
      'Rol':          u.rol,
      'Estado':       u.estado,
      'Afiliado SIS': u.afiliadoSis ? 'Sí' : 'No',
    }));
    const dataCitas = citas.map(c => ({
      'ID Cita':            c.id,
      'Fecha':              c.fecha,
      'Hora':               c.hora,
      'Paciente':           c.pacienteNombre ?? c.pacienteId,
      'Especialista':       c.especialistaNombre ?? c.especialistaId,
      'Estado':             this.labelEstado(c.estado),
      'Motivo Cancelación': c.mensajeCancelacion ?? '—',
      'Fecha Creación':     c.fechaCreacion ?? '—',
    }));
    const resumen = [
      { 'Concepto': 'Total usuarios',                    'Cantidad': usuarios.length },
      { 'Concepto': 'Pacientes aprobados',               'Cantidad': usuarios.filter(u => u.estado === 'APROBADO').length },
      { 'Concepto': 'Pacientes pendientes',              'Cantidad': usuarios.filter(u => u.estado === 'PENDIENTE').length },
      { 'Concepto': 'Pacientes rechazados',              'Cantidad': usuarios.filter(u => u.estado === 'RECHAZADO').length },
      { 'Concepto': 'Total citas',                       'Cantidad': citas.length },
      { 'Concepto': 'Citas confirmadas (pendientes)',    'Cantidad': citas.filter(c => c.estado === 'PROGRAMADA').length },
      { 'Concepto': 'Citas canceladas',                  'Cantidad': citas.filter(c => c.estado === 'CANCELADA').length },
      { 'Concepto': 'Citas atendidas',                   'Cantidad': citas.filter(c => c.estado === 'ATENDIDA').length },
      { 'Concepto': 'Citas no asistidas',                'Cantidad': citas.filter(c => c.estado === 'NO_ASISTIDA').length },
      { 'Concepto': 'Sin registro del especialista',     'Cantidad': citas.filter(c => c.estado === 'SIN_RESPUESTA').length },
    ];
    const wb = XLSX.utils.book_new();
    const wsResumen  = XLSX.utils.json_to_sheet(resumen);
    const wsUsuarios = XLSX.utils.json_to_sheet(dataUsuarios.length ? dataUsuarios : [{ 'Sin registros': '—' }]);
    const wsCitas    = XLSX.utils.json_to_sheet(dataCitas.length    ? dataCitas    : [{ 'Sin registros': '—' }]);
    this.aplicarEstilos(wsResumen);
    this.aplicarEstilos(wsUsuarios);
    this.aplicarEstilos(wsCitas);
    XLSX.utils.book_append_sheet(wb, wsResumen,  'Resumen');
    XLSX.utils.book_append_sheet(wb, wsUsuarios, 'Usuarios');
    XLSX.utils.book_append_sheet(wb, wsCitas,    'Citas');
    const fecha = new Date().toISOString().slice(0, 10);
    XLSX.writeFile(wb, `Reporte_General_${fecha}.xlsx`);
  }

  // ─── UTILIDADES ───
  private descargar(data: any[], nombreArchivo: string, nombreHoja: string) {
    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.json_to_sheet(data.length ? data : [{ 'Sin registros': '—' }]);
    this.aplicarEstilos(ws);
    XLSX.utils.book_append_sheet(wb, ws, nombreHoja);
    XLSX.writeFile(wb, `${nombreArchivo}.xlsx`);
  }

  private aplicarEstilos(ws: XLSX.WorkSheet) {
    const range = XLSX.utils.decode_range(ws['!ref'] || 'A1');
    const colWidths: { wch: number }[] = [];
    for (let C = range.s.c; C <= range.e.c; C++) {
      let maxLen = 10;
      for (let R = range.s.r; R <= range.e.r; R++) {
        const cell = ws[XLSX.utils.encode_cell({ r: R, c: C })];
        if (cell && cell.v) {
          const len = String(cell.v).length;
          if (len > maxLen) maxLen = len;
        }
      }
      colWidths.push({ wch: Math.min(maxLen + 2, 40) });
    }
    ws['!cols'] = colWidths;
  }
}