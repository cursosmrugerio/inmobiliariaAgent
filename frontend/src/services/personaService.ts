import api from './api';
import type { Persona, PersonaCreateRequest, PersonaUpdateRequest } from '@/types';

export const personaService = {
  getAll: async (): Promise<Persona[]> => {
    const response = await api.get<Persona[]>('/personas');
    return response.data;
  },

  getById: async (id: number): Promise<Persona> => {
    const response = await api.get<Persona>(`/personas/${id}`);
    return response.data;
  },

  create: async (data: PersonaCreateRequest): Promise<Persona> => {
    const response = await api.post<Persona>('/personas', data);
    return response.data;
  },

  update: async (id: number, data: PersonaUpdateRequest): Promise<Persona> => {
    const response = await api.put<Persona>(`/personas/${id}`, data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/personas/${id}`);
  },
};

export default personaService;
