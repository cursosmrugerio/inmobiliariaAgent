import React from 'react';
import { Box, Typography } from '@mui/material';
import { keyframes } from '@emotion/react';
import { useTranslation } from 'react-i18next';

const typingAnimation = keyframes`
  0% { opacity: 0.2; transform: translateY(0); }
  50% { opacity: 1; transform: translateY(-2px); }
  100% { opacity: 0.2; transform: translateY(0); }
`;

export const TypingIndicator: React.FC = () => {
  const { t } = useTranslation();

  return (
    <Box sx={{ display: 'flex', alignItems: 'center', mb: 2, gap: 1 }}>
      <Box
        sx={{
          display: 'flex',
          gap: 0.5,
          bgcolor: 'grey.100',
          p: 2,
          borderRadius: 2,
        }}
      >
        {[0, 1, 2].map((index) => (
          <Box
            key={index}
            sx={{
              width: 8,
              height: 8,
              borderRadius: '50%',
              bgcolor: 'grey.500',
              animation: `${typingAnimation} 1.4s infinite`,
              animationDelay: `${index * 0.2}s`,
            }}
          />
        ))}
      </Box>
      <Typography variant="body2" color="text.secondary">
        {t('chat.typing')}
      </Typography>
    </Box>
  );
};
