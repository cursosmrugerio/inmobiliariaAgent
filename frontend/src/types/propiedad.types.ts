export enum PropiedadTipo {
  CASA = 'CASA',
  DEPARTAMENTO = 'DEPARTAMENTO',
  OFICINA = 'OFICINA',
  LOCAL = 'LOCAL',
  ESTACIONAMIENTO = 'ESTACIONAMIENTO',
  EDIFICIO = 'EDIFICIO',
  TERRENO = 'TERRENO',
  OTRO = 'OTRO',
}

export interface Propiedad {
  id: number;
  nombre: string;
  tipo: PropiedadTipo;
  direccion: string;
  observaciones: string;
  inmobiliariaId: number;
  inmobiliariaNombre: string;
}

export interface PropiedadCreateRequest {
  nombre: string;
  tipo: PropiedadTipo;
  direccion: string;
  observaciones: string;
  inmobiliariaId: number;
}

export interface PropiedadUpdateRequest extends PropiedadCreateRequest {
  id: number;
}
