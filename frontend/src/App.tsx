import React, { Suspense, lazy, useMemo } from 'react';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
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

const LoadingFallback: React.FC = () => (
  <Box
    sx={{
      minHeight: { xs: 'calc(100vh - 56px)', sm: 'calc(100vh - 64px)' },
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
    }}
  >
    <CircularProgress />
  </Box>
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
          <Header />
          <Suspense fallback={<LoadingFallback />}>
            <Routes>
              <Route path="/login" element={<LoginForm />} />
              <Route
                path="/chat"
                element={
                  <PrivateRoute>
                    <ChatContainer />
                  </PrivateRoute>
                }
              />
              <Route path="/" element={<Navigate to="/chat" replace />} />
            </Routes>
          </Suspense>
        </BrowserRouter>
      </ThemeProvider>
    </AuthProvider>
  );
};

export default App;
