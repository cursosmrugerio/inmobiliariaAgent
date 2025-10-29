import React, { useMemo } from 'react';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { CssBaseline, ThemeProvider, createTheme } from '@mui/material';
import { enUS, esES } from '@mui/material/locale';
import { useTranslation } from 'react-i18next';

import { ChatContainer } from '@components/Chat/ChatContainer';
import { AuthProvider } from '@contexts/AuthContext';
import { LoginForm } from '@components/Auth/LoginForm';
import { PrivateRoute } from '@components/Auth/PrivateRoute';
import { Header } from '@components/Layout/Header';

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
        </BrowserRouter>
      </ThemeProvider>
    </AuthProvider>
  );
};

export default App;
