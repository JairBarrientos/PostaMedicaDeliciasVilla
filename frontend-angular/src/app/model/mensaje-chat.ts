export interface MensajeChat {
  id?: number;
  pacienteId: number;
  especialistaId: number;
  remitenteRol: 'PACIENTE' | 'ESPECIALISTA';
  contenido: string;
  fechaEnvio?: string;
  leido?: boolean;
}