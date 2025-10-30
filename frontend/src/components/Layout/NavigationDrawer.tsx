import React from 'react';
import {
  Divider,
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
} from '@mui/material';
import DashboardIcon from '@mui/icons-material/Dashboard';
import BusinessIcon from '@mui/icons-material/Business';
import HomeIcon from '@mui/icons-material/Home';
import PeopleIcon from '@mui/icons-material/People';
import DescriptionIcon from '@mui/icons-material/Description';
import ChatIcon from '@mui/icons-material/Chat';
import { useLocation, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

interface NavigationDrawerProps {
  open: boolean;
  onClose: () => void;
}

export const NavigationDrawer: React.FC<NavigationDrawerProps> = ({ open, onClose }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { t } = useTranslation();

  const menuItems = [
    { path: '/', label: t('nav.dashboard'), icon: <DashboardIcon /> },
    { path: '/inmobiliarias', label: t('nav.inmobiliarias'), icon: <BusinessIcon /> },
    { path: '/propiedades', label: t('nav.propiedades'), icon: <HomeIcon /> },
    { path: '/personas', label: t('nav.personas'), icon: <PeopleIcon /> },
    { path: '/contratos', label: t('nav.contratos'), icon: <DescriptionIcon /> },
  ];

  const handleNavigate = (path: string) => {
    navigate(path);
    onClose();
  };

  return (
    <Drawer anchor="left" open={open} onClose={onClose}>
      <List sx={{ width: 260 }}>
        {menuItems.map((item) => (
          <ListItem key={item.path} disablePadding>
            <ListItemButton
              selected={location.pathname === item.path}
              onClick={() => handleNavigate(item.path)}
            >
              <ListItemIcon>{item.icon}</ListItemIcon>
              <ListItemText primary={item.label} />
            </ListItemButton>
          </ListItem>
        ))}
        <Divider sx={{ my: 1 }} />
        <ListItem disablePadding>
          <ListItemButton
            selected={location.pathname === '/chat'}
            onClick={() => handleNavigate('/chat')}
          >
            <ListItemIcon>
              <ChatIcon />
            </ListItemIcon>
            <ListItemText primary={t('nav.chat')} />
          </ListItemButton>
        </ListItem>
      </List>
    </Drawer>
  );
};
