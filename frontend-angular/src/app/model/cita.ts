export interface Cita {
  id?: number;
  fecha: string;
  hora: string;
  codigoCita?: string;
  estado?: string;
  pacienteId: number;
  especialistaId: number;
  mensajeCancelacion?: string;
  fechaCreacion?: string;
}