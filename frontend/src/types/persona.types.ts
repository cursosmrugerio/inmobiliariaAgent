export enum PersonaTipo {
  FISICA = 'FISICA',
  MORAL = 'MORAL',
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
