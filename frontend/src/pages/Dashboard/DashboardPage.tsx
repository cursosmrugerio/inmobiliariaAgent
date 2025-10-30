import React from 'react';
import { Box, Grid, Paper, Typography } from '@mui/material';
import BusinessIcon from '@mui/icons-material/Business';
import HomeIcon from '@mui/icons-material/Home';
import PeopleIcon from '@mui/icons-material/People';
import DescriptionIcon from '@mui/icons-material/Description';
import { useTranslation } from 'react-i18next';

const DASHBOARD_CARDS = [
  { icon: <BusinessIcon fontSize="large" />, color: '#1976d2', value: 0, labelKey: 'nav.inmobiliarias' },
  { icon: <HomeIcon fontSize="large" />, color: '#388e3c', value: 0, labelKey: 'nav.propiedades' },
  { icon: <PeopleIcon fontSize="large" />, color: '#f57c00', value: 0, labelKey: 'nav.personas' },
  { icon: <DescriptionIcon fontSize="large" />, color: '#d32f2f', value: 0, labelKey: 'nav.contratos' },
];

export const DashboardPage: React.FC = () => {
  const { t } = useTranslation();

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" sx={{ mb: 3 }}>
        {t('nav.dashboard')}
      </Typography>
      <Grid container spacing={3}>
        {DASHBOARD_CARDS.map((card) => (
          <Grid item xs={12} sm={6} md={3} key={card.labelKey}>
            <Paper sx={{ p: 3, display: 'flex', alignItems: 'center', gap: 2 }}>
              <Box sx={{ color: card.color }}>{card.icon}</Box>
              <Box>
                <Typography variant="h4">{card.value}</Typography>
                <Typography variant="body2" color="text.secondary">
                  {t(card.labelKey)}
                </Typography>
              </Box>
            </Paper>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};
