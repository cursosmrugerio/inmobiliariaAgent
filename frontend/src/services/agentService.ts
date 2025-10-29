import api from './api';
import { AgentType, ChatRequest, ChatResponse } from '@/types/chat.types';

const AGENT_ENDPOINTS: Record<AgentType, string> = {
  [AgentType.INMOBILIARIA]: '/agent/chat',
  [AgentType.PROPIEDAD]: '/agent/propiedades/chat',
  [AgentType.PERSONA]: '/agent/personas/chat',
};

export const agentService = {
  sendMessage: async (
    agentType: AgentType,
    request: ChatRequest,
  ): Promise<ChatResponse> => {
    const endpoint = AGENT_ENDPOINTS[agentType];
    const response = await api.post<ChatResponse>(endpoint, request);
    return response.data;
  },
};

export default agentService;
