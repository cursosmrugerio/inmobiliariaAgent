import React from 'react';
import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
} from 'react-router-dom';
import {
  CssBaseline,
  ThemeProvider,
  createTheme,
} from '@mui/material';
import { ChatContainer } from '@components/Chat/ChatContainer';
import { AuthProvider } from '@contexts/AuthContext';
import { LoginForm } from '@components/Auth/LoginForm';
import { PrivateRoute } from '@components/Auth/PrivateRoute';

const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
});

const App: React.FC = () => {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <BrowserRouter>
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
      </AuthProvider>
    </ThemeProvider>
  );
};

export default App;
