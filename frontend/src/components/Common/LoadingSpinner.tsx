import React from 'react';
import { Box, CircularProgress } from '@mui/material';

interface LoadingSpinnerProps {
  size?: number;
  fullScreen?: boolean;
}

export const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({
  size = 40,
  fullScreen = false,
}) => (
  <Box
    display="flex"
    justifyContent="center"
    alignItems="center"
    minHeight={fullScreen ? '100vh' : '200px'}
  >
    <CircularProgress size={size} />
  </Box>
);
