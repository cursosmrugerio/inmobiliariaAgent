import api from './api';
import type {
  Propiedad,
  PropiedadCreateRequest,
  PropiedadUpdateRequest,
} from '@/types';

export const propiedadService = {
  getAll: async (inmobiliariaId?: number): Promise<Propiedad[]> => {
    const params = inmobiliariaId ? { inmobiliariaId } : {};
    const response = await api.get<Propiedad[]>('/propiedades', { params });
    return response.data;
  },

  getById: async (id: number): Promise<Propiedad> => {
    const response = await api.get<Propiedad>(`/propiedades/${id}`);
    return response.data;
  },

  create: async (data: PropiedadCreateRequest): Promise<Propiedad> => {
    const response = await api.post<Propiedad>('/propiedades', data);
    return response.data;
  },

  update: async (id: number, data: PropiedadUpdateRequest): Promise<Propiedad> => {
    const response = await api.put<Propiedad>(`/propiedades/${id}`, data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/propiedades/${id}`);
  },
};

export default propiedadService;
