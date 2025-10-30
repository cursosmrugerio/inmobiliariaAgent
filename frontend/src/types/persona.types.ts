export enum PersonaTipo {
  ARRENDADOR = 'ARRENDADOR',
  ARRENDATARIO = 'ARRENDATARIO',
  FIADOR = 'FIADOR',
  OTRO = 'OTRO',
}

export interface Persona {
  id: number;
  tipoPersona: PersonaTipo;
  nombre?: string;
  apellidos?: string;
  razonSocial?: string;
  rfc: string;
  curp?: string;
  email: string;
  telefono: string;
  fechaAlta: string;
  activo: boolean;
}

export interface PersonaCreateRequest {
  tipoPersona: PersonaTipo;
  nombre?: string;
  apellidos?: string;
  razonSocial?: string;
  rfc: string;
  curp?: string;
  email: string;
  telefono: string;
  fechaAlta: string;
  activo: boolean;
}

export interface PersonaUpdateRequest extends PersonaCreateRequest {
  id: number;
}
