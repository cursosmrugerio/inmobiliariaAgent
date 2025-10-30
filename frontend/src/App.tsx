import React, { Suspense, lazy, useMemo } from 'react';
import { BrowserRouter, Navigate, Outlet, Route, Routes } from 'react-router-dom';
import { Box, CircularProgress, CssBaseline, ThemeProvider, createTheme } from '@mui/material';
import { enUS, esES } from '@mui/material/locale';
import { useTranslation } from 'react-i18next';

import { AuthProvider } from '@contexts/AuthContext';
import { PrivateRoute } from '@components/Auth/PrivateRoute';
import { Header } from '@components/Layout/Header';

const ChatContainer = lazy(async () => {
  const module = await import('@components/Chat/ChatContainer');
  return { default: module.ChatContainer };
});

const LoginForm = lazy(async () => {
  const module = await import('@components/Auth/LoginForm');
  return { default: module.LoginForm };
});

const DashboardPage = lazy(async () => {
  const module = await import('@/pages/Dashboard');
  return { default: module.DashboardPage };
});

const InmobiliariasPage = lazy(async () => {
  const module = await import('@/pages/Inmobiliarias');
  return { default: module.InmobiliariasPage };
});

const PropiedadesPage = lazy(async () => {
  const module = await import('@/pages/Propiedades');
  return { default: module.PropiedadesPage };
});

const PersonasPage = lazy(async () => {
  const module = await import('@/pages/Personas');
  return { default: module.PersonasPage };
});

const ContratosPage = lazy(async () => {
  const module = await import('@/pages/Contratos');
  return { default: module.ContratosPage };
});

const LoadingFallback: React.FC = () => (
  <Box
    sx={{
      minHeight: '50vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
    }}
  >
    <CircularProgress />
  </Box>
);

const PrivateLayout: React.FC = () => (
  <PrivateRoute>
    <>
      <Header />
      <Box component="main">
        <Outlet />
      </Box>
    </>
  </PrivateRoute>
);

const App: React.FC = () => {
  const { i18n } = useTranslation();

  const theme = useMemo(
    () =>
      createTheme(
        {
          palette: {
            primary: {
              main: '#1976d2',
            },
            secondary: {
              main: '#dc004e',
            },
          },
        },
        i18n.language.startsWith('es') ? esES : enUS,
      ),
    [i18n.language],
  );

  return (
    <AuthProvider>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <BrowserRouter>
          <Suspense fallback={<LoadingFallback />}>
            <Routes>
              <Route path="/login" element={<LoginForm />} />
              <Route element={<PrivateLayout />}>
                <Route index element={<Navigate to="/dashboard" replace />} />
                <Route path="/dashboard" element={<DashboardPage />} />
                <Route path="/inmobiliarias" element={<InmobiliariasPage />} />
                <Route path="/propiedades" element={<PropiedadesPage />} />
                <Route path="/personas" element={<PersonasPage />} />
                <Route path="/contratos" element={<ContratosPage />} />
                <Route path="/chat" element={<ChatContainer />} />
              </Route>
              <Route path="*" element={<Navigate to="/dashboard" replace />} />
            </Routes>
          </Suspense>
        </BrowserRouter>
      </ThemeProvider>
    </AuthProvider>
  );
};

export default App;
