import React from 'react';
import { Box, Paper, Typography } from '@mui/material';
import { useTranslation } from 'react-i18next';

export const ContratosPage: React.FC = () => {
  const { t } = useTranslation();

  return (
    <Box sx={{ p: 3 }}>
      <Paper sx={{ p: 4, textAlign: 'center' }}>
        <Typography variant="h4" sx={{ mb: 2 }}>
          {t('nav.contratos')}
        </Typography>
        <Typography variant="body1" color="text.secondary">
          {t('common.noData')}
        </Typography>
      </Paper>
    </Box>
  );
};
