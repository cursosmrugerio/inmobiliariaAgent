export interface Inmobiliaria {
  id: number;
  nombre: string;
  rfc: string;
  nombreContacto: string;
  correo: string;
  telefono: string;
}

export interface InmobiliariaCreateRequest {
  nombre: string;
  rfc: string;
  nombreContacto: string;
  correo: string;
  telefono: string;
}

export interface InmobiliariaUpdateRequest extends InmobiliariaCreateRequest {
  id: number;
}
