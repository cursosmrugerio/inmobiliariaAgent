import api from './api';
import type {
  Inmobiliaria,
  InmobiliariaCreateRequest,
  InmobiliariaUpdateRequest,
} from '@/types';

export const inmobiliariaService = {
  getAll: async (): Promise<Inmobiliaria[]> => {
    const response = await api.get<Inmobiliaria[]>('/inmobiliarias');
    return response.data;
  },

  getById: async (id: number): Promise<Inmobiliaria> => {
    const response = await api.get<Inmobiliaria>(`/inmobiliarias/${id}`);
    return response.data;
  },

  create: async (data: InmobiliariaCreateRequest): Promise<Inmobiliaria> => {
    const response = await api.post<Inmobiliaria>('/inmobiliarias', data);
    return response.data;
  },

  update: async (
    id: number,
    data: InmobiliariaUpdateRequest,
  ): Promise<Inmobiliaria> => {
    const response = await api.put<Inmobiliaria>(`/inmobiliarias/${id}`, data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/inmobiliarias/${id}`);
  },
};

export default inmobiliariaService;
