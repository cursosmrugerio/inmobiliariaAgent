import React, { useEffect, useState } from 'react';
import { Box, Grid, Paper, Typography } from '@mui/material';
import BusinessIcon from '@mui/icons-material/Business';
import HomeIcon from '@mui/icons-material/Home';
import PeopleIcon from '@mui/icons-material/People';
import DescriptionIcon from '@mui/icons-material/Description';
import { useTranslation } from 'react-i18next';
import { inmobiliariaService, propiedadService, personaService } from '@/services';
import { LoadingSpinner } from '@/components/Common';

interface DashboardStats {
  inmobiliarias: number;
  propiedades: number;
  personas: number;
  contratos: number;
}

export const DashboardPage: React.FC = () => {
  const { t } = useTranslation();
  const [stats, setStats] = useState<DashboardStats>({
    inmobiliarias: 0,
    propiedades: 0,
    personas: 0,
    contratos: 0,
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadStats = async () => {
      try {
        setLoading(true);
        const [inmobiliarias, propiedades, personas] = await Promise.all([
          inmobiliariaService.getAll(),
          propiedadService.getAll(),
          personaService.getAll(),
        ]);

        setStats({
          inmobiliarias: inmobiliarias.length,
          propiedades: propiedades.length,
          personas: personas.length,
          contratos: 0, // TODO: Add contratos service when available
        });
      } catch (error) {
        console.error('Error loading dashboard stats:', error);
      } finally {
        setLoading(false);
      }
    };

    loadStats();
  }, []);

  const DASHBOARD_CARDS = [
    {
      icon: <BusinessIcon fontSize="large" />,
      color: '#1976d2',
      value: stats.inmobiliarias,
      labelKey: 'nav.inmobiliarias',
    },
    {
      icon: <HomeIcon fontSize="large" />,
      color: '#388e3c',
      value: stats.propiedades,
      labelKey: 'nav.propiedades',
    },
    {
      icon: <PeopleIcon fontSize="large" />,
      color: '#f57c00',
      value: stats.personas,
      labelKey: 'nav.personas',
    },
    {
      icon: <DescriptionIcon fontSize="large" />,
      color: '#d32f2f',
      value: stats.contratos,
      labelKey: 'nav.contratos',
    },
  ];

  if (loading) {
    return <LoadingSpinner fullScreen />;
  }

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
