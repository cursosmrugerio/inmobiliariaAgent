import { useCallback, useState } from 'react';
import { agentService } from '@services/agentService';
import { AgentType, ChatRequest, Message } from '@/types/chat.types';

const createMessageId = (): string => {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }
  return Math.random().toString(36).slice(2, 11);
};

export const useChat = (initialAgent: AgentType) => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [currentAgent, setCurrentAgent] = useState<AgentType>(initialAgent);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const sendMessage = useCallback(
    async (content: string) => {
      if (!content.trim()) {
        return;
      }

      const trimmedContent = content.trim();
      const userMessage: Message = {
        id: createMessageId(),
        content: trimmedContent,
        sender: 'user',
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, userMessage]);
      setIsLoading(true);
      setError(null);

      try {
        const request: ChatRequest = {
          message: trimmedContent,
          sessionId: sessionId || undefined,
        };

        const response = await agentService.sendMessage(currentAgent, request);

        if (!sessionId && response.sessionId) {
          setSessionId(response.sessionId);
        }

        const agentMessage: Message = {
          id: createMessageId(),
          content: response.response,
          sender: 'agent',
          timestamp: new Date(),
          success: response.success,
          error: response.error,
        };
        setMessages((prev) => [...prev, agentMessage]);

        if (!response.success) {
          setError(response.error || 'Unknown error occurred');
        }
      } catch (err: unknown) {
        const errorMessage =
          (err as { response?: { data?: { message?: string } }; message?: string }).response?.data?.message ||
          (err as { message?: string }).message ||
          'Network error';

        setError(errorMessage);

        const errorMsg: Message = {
          id: createMessageId(),
          content: errorMessage,
          sender: 'agent',
          timestamp: new Date(),
          success: false,
          error: errorMessage,
        };
        setMessages((prev) => [...prev, errorMsg]);
      } finally {
        setIsLoading(false);
      }
    },
    [currentAgent, sessionId],
  );

  const clearConversation = useCallback(() => {
    setMessages([]);
    setSessionId(null);
    setError(null);
  }, []);

  const changeAgent = useCallback(
    (newAgent: AgentType) => {
      setCurrentAgent(newAgent);
      clearConversation();
    },
    [clearConversation],
  );

  return {
    messages,
    sessionId,
    currentAgent,
    isLoading,
    error,
    sendMessage,
    clearConversation,
    changeAgent,
  };
};
