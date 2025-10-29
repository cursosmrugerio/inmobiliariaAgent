export enum AgentType {
  INMOBILIARIA = 'inmobiliaria',
  PROPIEDAD = 'propiedad',
  PERSONA = 'persona',
}

export interface ChatRequest {
  message: string;
  sessionId?: string;
}

export interface ChatResponse {
  response: string;
  sessionId: string;
  success: boolean;
  error?: string;
}

export interface Message {
  id: string;
  content: string;
  sender: 'user' | 'agent';
  timestamp: Date;
  success?: boolean;
  error?: string;
}

export interface AgentConfig {
  type: AgentType;
  name: string;
  description: string;
  endpoint: string;
  icon: string;
}
