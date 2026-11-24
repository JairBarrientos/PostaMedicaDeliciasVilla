export interface Paciente {
  id?: number;
  nombres: string;
  apellidos: string;
  dni: string;
  correo: string;
  password?: string;
  telefono: string;
  rol?: string;
  estado?: string;
  afiliadoSis?: boolean;
}