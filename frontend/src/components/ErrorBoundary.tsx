import { Component, ErrorInfo, ReactNode } from 'react';
import { Box, Button, Container, Typography } from '@mui/material';
import { WithTranslation, withTranslation } from 'react-i18next';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error?: Error;
}

class ErrorBoundaryBase extends Component<Props & WithTranslation, State> {
  constructor(props: Props & WithTranslation) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('Error caught by boundary:', error, errorInfo);
  }

  private handleReload = () => {
    this.setState({ hasError: false, error: undefined });
    window.location.reload();
  };

  override render(): ReactNode {
    const { hasError, error } = this.state;
    const { children, t } = this.props;

    if (hasError) {
      return (
        <Container>
          <Box
            sx={{
              minHeight: '100vh',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              textAlign: 'center',
              gap: 2,
              py: 4,
            }}
          >
            <Typography variant="h4" gutterBottom>
              {t('errors.unexpectedTitle')}
            </Typography>
            <Typography color="text.secondary">
              {t('errors.unexpectedDescription')}
            </Typography>
            {error?.message && (
              <Typography variant="body2" color="text.disabled">
                {error.message}
              </Typography>
            )}
            <Button variant="contained" onClick={this.handleReload}>
              {t('errors.reload')}
            </Button>
          </Box>
        </Container>
      );
    }

    return children;
  }
}

export const ErrorBoundary = withTranslation()(ErrorBoundaryBase);
