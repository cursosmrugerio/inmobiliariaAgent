export enum PropiedadTipo {
  CASA = 'CASA',
  DEPARTAMENTO = 'DEPARTAMENTO',
  LOCAL_COMERCIAL = 'LOCAL_COMERCIAL',
  OFICINA = 'OFICINA',
  BODEGA = 'BODEGA',
  TERRENO = 'TERRENO',
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
