import React from 'react';
import { Alert, Box, Paper, Typography } from '@mui/material';
import { Message } from '@/types/chat.types';
import { formatTime } from '@utils/formatters';

interface MessageBubbleProps {
  message: Message;
}

export const MessageBubble: React.FC<MessageBubbleProps> = ({ message }) => {
  const isUser = message.sender === 'user';
  const isError = message.success === false;

  return (
    <Box
      sx={{
        display: 'flex',
        justifyContent: isUser ? 'flex-end' : 'flex-start',
        mb: 2,
      }}
    >
      <Box sx={{ maxWidth: { xs: '85%', sm: '70%' } }}>
        {isError ? (
          <Alert severity="error">{message.content}</Alert>
        ) : (
          <Paper
            elevation={1}
            sx={{
              p: 2,
              bgcolor: isUser ? 'primary.main' : 'grey.100',
              color: isUser ? 'primary.contrastText' : 'text.primary',
              borderRadius: 2,
            }}
          >
            <Typography variant="body1" sx={{ whiteSpace: 'pre-wrap' }}>
              {message.content}
            </Typography>
            <Typography
              variant="caption"
              sx={{
                display: 'block',
                mt: 0.5,
                opacity: 0.7,
                textAlign: 'right',
              }}
            >
              {formatTime(message.timestamp)}
            </Typography>
          </Paper>
        )}
      </Box>
    </Box>
  );
};
