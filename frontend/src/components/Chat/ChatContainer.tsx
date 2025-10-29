import React, { useEffect, useRef } from 'react';
import { AgentSelector } from './AgentSelector';
import { MessageBubble } from './MessageBubble';
import { MessageInput } from './MessageInput';
import { TypingIndicator } from './TypingIndicator';
import { useChat } from '@hooks/useChat';
import { AgentType } from '@/types/chat.types';
import { Box, Button, Chip, Container, Paper, Typography } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useAuth } from '@contexts/AuthContext';
import { useNavigate } from 'react-router-dom';

export const ChatContainer: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const {
    messages,
    sessionId,
    currentAgent,
    isLoading,
    sendMessage,
    clearConversation,
    changeAgent,
  } = useChat(AgentType.INMOBILIARIA);
  const { logout, user } = useAuth();

  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

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
          <Box
            sx={{
              display: 'flex',
              flexDirection: 'column',
              gap: 0.5,
            }}
          >
            <Typography variant="h5">{t('chat.title')}</Typography>
            {user && (
              <Typography variant="body2" color="text.secondary">
                {user.name} · {user.email}
              </Typography>
            )}
          </Box>
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 1,
              alignSelf: { xs: 'stretch', sm: 'center' },
              width: { xs: '100%', sm: 'auto' },
              justifyContent: { xs: 'space-between', sm: 'flex-end' },
            }}
          >
            {sessionId && (
              <Chip
                label={`Session: ${sessionId.substring(0, 8)}...`}
                size="small"
              />
            )}
            <Button
              variant="outlined"
              color="secondary"
              size="small"
              onClick={handleLogout}
            >
              {t('auth.logout')}
            </Button>
          </Box>
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
