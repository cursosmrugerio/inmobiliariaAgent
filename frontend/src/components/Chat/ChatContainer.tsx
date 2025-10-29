import React, { useEffect, useRef } from 'react';
import { AgentSelector } from './AgentSelector';
import { MessageBubble } from './MessageBubble';
import { MessageInput } from './MessageInput';
import { TypingIndicator } from './TypingIndicator';
import { useChat } from '@hooks/useChat';
import { AgentType } from '@/types/chat.types';
import { Box, Chip, Container, Paper, Typography } from '@mui/material';
import { useTranslation } from 'react-i18next';

export const ChatContainer: React.FC = () => {
  const { t } = useTranslation();
  const {
    messages,
    sessionId,
    currentAgent,
    isLoading,
    sendMessage,
    clearConversation,
    changeAgent,
  } = useChat(AgentType.INMOBILIARIA);

  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  return (
    <Container
      maxWidth="md"
      sx={{ height: '100vh', display: 'flex', flexDirection: 'column', py: 2 }}
    >
      <Paper elevation={2} sx={{ p: 2, mb: 2 }}>
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: { xs: 'flex-start', sm: 'center' },
            flexDirection: { xs: 'column', sm: 'row' },
            gap: 1,
            mb: 1,
          }}
        >
          <Typography variant="h5">{t('chat.title')}</Typography>
          {sessionId && (
            <Chip label={`Session: ${sessionId.substring(0, 8)}...`} size="small" />
          )}
        </Box>
        <AgentSelector
          currentAgent={currentAgent}
          onChangeAgent={changeAgent}
          onClearConversation={clearConversation}
        />
      </Paper>

      <Paper
        elevation={1}
        sx={{
          flex: 1,
          overflowY: 'auto',
          p: 2,
          mb: 2,
          bgcolor: 'grey.50',
        }}
      >
        {messages.length === 0 ? (
          <Box sx={{ textAlign: 'center', py: 4 }}>
            <Typography color="text.secondary">
              {t('chat.emptyState')}
            </Typography>
          </Box>
        ) : (
          messages.map((message) => (
            <MessageBubble key={message.id} message={message} />
          ))
        )}
        {isLoading && <TypingIndicator />}
        <div ref={messagesEndRef} />
      </Paper>

      <MessageInput onSendMessage={sendMessage} disabled={isLoading} />
    </Container>
  );
};
