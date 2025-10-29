import React, { useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Container,
  Paper,
  TextField,
  Typography,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '@contexts/AuthContext';

export const LoginForm: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { login } = useAuth();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      await login(email, password);
      navigate('/chat', { replace: true });
    } catch (err: unknown) {
      const maybeAxiosError = err as {
        response?: { data?: { message?: string } };
        message?: string;
      };
      const responseMessage = maybeAxiosError.response?.data?.message;
      if (typeof responseMessage === 'string' && responseMessage.length > 0) {
        setError(responseMessage);
      } else if (
        typeof maybeAxiosError.message === 'string' &&
        maybeAxiosError.message.length > 0
      ) {
        setError(maybeAxiosError.message);
      } else {
        setError(t('auth.loginError'));
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Container maxWidth="xs">
      <Box
        sx={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <Paper elevation={3} sx={{ p: 4, width: '100%' }}>
          <Typography variant="h4" textAlign="center" gutterBottom>
            {t('auth.login')}
          </Typography>

          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit}>
            <TextField
              fullWidth
              type="email"
              label={t('auth.email')}
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              margin="normal"
              required
            />
            <TextField
              fullWidth
              type="password"
              label={t('auth.password')}
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              margin="normal"
              required
            />
            <Button
              fullWidth
              type="submit"
              variant="contained"
              size="large"
              disabled={isLoading}
              sx={{ mt: 3 }}
            >
              {isLoading ? t('auth.loggingIn') : t('auth.login')}
            </Button>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
};
