# Frontend CRUD Implementation Guide

## Overview
This guide provides a phased approach to implement traditional CRUD (Create, Read, Update, Delete) interfaces for all entities in the Inmobiliaria Management System frontend. The new CRUD functionality will coexist with the existing agent chat interface, accessible through a navigation menu.

**Target Stack:** React 18 + TypeScript + Material-UI v5 + Vite
**Backend Integration:** Existing REST APIs (already implemented)
**Estimated Total Effort:** 3-4 weeks (depending on team size)

---

## Prerequisites

### Required Knowledge
- React 18 with TypeScript
- Material-UI (MUI) v5 components
- React Router v6
- Axios for HTTP requests
- React Hook Form (will be added)
- i18next for internationalization

### Environment Setup
```bash
cd frontend
npm install
npm run dev  # Development server on localhost:5173
```

### Backend APIs Available
All REST endpoints are already implemented:
- `/inmobiliarias` - Agency management
- `/propiedades` - Property management
- `/personas` - Contact management
- `/contratos` - Contract management (if available)

---

## Phase 1: Foundation Setup (Week 1, Days 1-2)

### 1.1 Install Required Dependencies

```bash
cd frontend
npm install react-hook-form @hookform/resolvers yup
npm install @mui/x-data-grid  # Optional: for advanced data grid features
```

**Files to update:**
- `package.json` (automatically updated)

**Deliverable:** Updated `package.json` with new dependencies

---

### 1.2 Create Project Structure

Create the following directories:

```bash
mkdir -p src/pages/Dashboard
mkdir -p src/pages/Inmobiliarias
mkdir -p src/pages/Propiedades
mkdir -p src/pages/Personas
mkdir -p src/pages/Contratos
mkdir -p src/components/Common
mkdir -p src/types
```

**Deliverable:** New directory structure in place

---

### 1.3 Define TypeScript Types

Create type definitions for all entities matching backend DTOs.

**File:** `src/types/inmobiliaria.types.ts`
```typescript
export interface Inmobiliaria {
  id: number;
  nombre: string;
  rfc: string;
  nombreContacto: string;
  correo: string;
  telefono: string;
}

export interface InmobiliariaCreateRequest {
  nombre: string;
  rfc: string;
  nombreContacto: string;
  correo: string;
  telefono: string;
}

export interface InmobiliariaUpdateRequest extends InmobiliariaCreateRequest {
  id: number;
}
```

**File:** `src/types/propiedad.types.ts`
```typescript
export enum PropiedadTipo {
  CASA = 'CASA',
  DEPARTAMENTO = 'DEPARTAMENTO',
  LOCAL_COMERCIAL = 'LOCAL_COMERCIAL',
  OFICINA = 'OFICINA',
  BODEGA = 'BODEGA',
  TERRENO = 'TERRENO'
}

export interface Propiedad {
  id: number;
  nombre: string;
  tipo: PropiedadTipo;
  direccion: string;
  observaciones: string;
  inmobiliariaId: number;
  inmobiliariaNombre: string;
}

export interface PropiedadCreateRequest {
  nombre: string;
  tipo: PropiedadTipo;
  direccion: string;
  observaciones: string;
  inmobiliariaId: number;
}

export interface PropiedadUpdateRequest extends PropiedadCreateRequest {
  id: number;
}
```

**File:** `src/types/persona.types.ts`
```typescript
export enum PersonaTipo {
  ARRENDADOR = 'ARRENDADOR',
  ARRENDATARIO = 'ARRENDATARIO',
  FIADOR = 'FIADOR',
  OTRO = 'OTRO'
}

export interface Persona {
  id: number;
  tipoPersona: PersonaTipo;
  nombre: string;
  apellidos: string;
  razonSocial: string;
  rfc: string;
  curp: string;
  email: string;
  telefono: string;
  fechaAlta: string;
  activo: boolean;
}

export interface PersonaCreateRequest {
  tipoPersona: PersonaTipo;
  nombre: string;
  apellidos: string;
  razonSocial?: string;
  rfc: string;
  curp?: string;
  email: string;
  telefono: string;
}

export interface PersonaUpdateRequest extends PersonaCreateRequest {
  id: number;
}
```

**File:** `src/types/contrato.types.ts`
```typescript
// Define based on backend DTO when available
export interface Contrato {
  id: number;
  // Add fields based on backend implementation
}
```

**File:** `src/types/index.ts`
```typescript
// Re-export all types for easier imports
export * from './inmobiliaria.types';
export * from './propiedad.types';
export * from './persona.types';
export * from './contrato.types';
export * from './auth.types';
export * from './chat.types';
```

**Deliverables:**
- ✅ `src/types/inmobiliaria.types.ts`
- ✅ `src/types/propiedad.types.ts`
- ✅ `src/types/persona.types.ts`
- ✅ `src/types/contrato.types.ts`
- ✅ `src/types/index.ts`

---

### 1.4 Create Entity Services

Create service files to interact with backend REST APIs.

**File:** `src/services/inmobiliariaService.ts`
```typescript
import api from './api';
import type { Inmobiliaria, InmobiliariaCreateRequest, InmobiliariaUpdateRequest } from '@/types';

export const inmobiliariaService = {
  getAll: async (): Promise<Inmobiliaria[]> => {
    const response = await api.get<Inmobiliaria[]>('/inmobiliarias');
    return response.data;
  },

  getById: async (id: number): Promise<Inmobiliaria> => {
    const response = await api.get<Inmobiliaria>(`/inmobiliarias/${id}`);
    return response.data;
  },

  create: async (data: InmobiliariaCreateRequest): Promise<Inmobiliaria> => {
    const response = await api.post<Inmobiliaria>('/inmobiliarias', data);
    return response.data;
  },

  update: async (id: number, data: InmobiliariaUpdateRequest): Promise<Inmobiliaria> => {
    const response = await api.put<Inmobiliaria>(`/inmobiliarias/${id}`, data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/inmobiliarias/${id}`);
  }
};
```

**File:** `src/services/propiedadService.ts`
```typescript
import api from './api';
import type { Propiedad, PropiedadCreateRequest, PropiedadUpdateRequest } from '@/types';

export const propiedadService = {
  getAll: async (inmobiliariaId?: number): Promise<Propiedad[]> => {
    const params = inmobiliariaId ? { inmobiliariaId } : {};
    const response = await api.get<Propiedad[]>('/propiedades', { params });
    return response.data;
  },

  getById: async (id: number): Promise<Propiedad> => {
    const response = await api.get<Propiedad>(`/propiedades/${id}`);
    return response.data;
  },

  create: async (data: PropiedadCreateRequest): Promise<Propiedad> => {
    const response = await api.post<Propiedad>('/propiedades', data);
    return response.data;
  },

  update: async (id: number, data: PropiedadUpdateRequest): Promise<Propiedad> => {
    const response = await api.put<Propiedad>(`/propiedades/${id}`, data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/propiedades/${id}`);
  }
};
```

**File:** `src/services/personaService.ts`
```typescript
import api from './api';
import type { Persona, PersonaCreateRequest, PersonaUpdateRequest } from '@/types';

export const personaService = {
  getAll: async (): Promise<Persona[]> => {
    const response = await api.get<Persona[]>('/personas');
    return response.data;
  },

  getById: async (id: number): Promise<Persona> => {
    const response = await api.get<Persona>(`/personas/${id}`);
    return response.data;
  },

  create: async (data: PersonaCreateRequest): Promise<Persona> => {
    const response = await api.post<Persona>('/personas', data);
    return response.data;
  },

  update: async (id: number, data: PersonaUpdateRequest): Promise<Persona> => {
    const response = await api.put<Persona>(`/personas/${id}`, data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/personas/${id}`);
  }
};
```

**Update:** `src/services/index.ts` (create if doesn't exist)
```typescript
export * from './api';
export * from './authService';
export * from './agentService';
export * from './inmobiliariaService';
export * from './propiedadService';
export * from './personaService';
```

**Deliverables:**
- ✅ `src/services/inmobiliariaService.ts`
- ✅ `src/services/propiedadService.ts`
- ✅ `src/services/personaService.ts`
- ✅ `src/services/index.ts` (updated)

---

### 1.5 Add Internationalization Keys

Add translations for CRUD operations in both languages.

**File:** `src/i18n/locales/es.json` (add to existing JSON)
```json
{
  "common": {
    "create": "Crear",
    "edit": "Editar",
    "delete": "Eliminar",
    "cancel": "Cancelar",
    "save": "Guardar",
    "search": "Buscar",
    "actions": "Acciones",
    "loading": "Cargando...",
    "noData": "No hay datos disponibles",
    "confirmDelete": "¿Estás seguro de que deseas eliminar este registro?",
    "deleteSuccess": "Registro eliminado exitosamente",
    "deleteError": "Error al eliminar el registro",
    "saveSuccess": "Registro guardado exitosamente",
    "saveError": "Error al guardar el registro"
  },
  "nav": {
    "dashboard": "Panel Principal",
    "inmobiliarias": "Inmobiliarias",
    "propiedades": "Propiedades",
    "personas": "Personas",
    "contratos": "Contratos",
    "chat": "Chat con Agentes"
  },
  "inmobiliarias": {
    "title": "Gestión de Inmobiliarias",
    "create": "Nueva Inmobiliaria",
    "edit": "Editar Inmobiliaria",
    "fields": {
      "nombre": "Nombre",
      "rfc": "RFC",
      "nombreContacto": "Nombre de Contacto",
      "correo": "Correo Electrónico",
      "telefono": "Teléfono"
    },
    "validation": {
      "nombreRequired": "El nombre es requerido",
      "rfcRequired": "El RFC es requerido",
      "rfcInvalid": "RFC inválido",
      "correoRequired": "El correo es requerido",
      "correoInvalid": "Correo electrónico inválido"
    }
  },
  "propiedades": {
    "title": "Gestión de Propiedades",
    "create": "Nueva Propiedad",
    "edit": "Editar Propiedad",
    "fields": {
      "nombre": "Nombre",
      "tipo": "Tipo",
      "direccion": "Dirección",
      "observaciones": "Observaciones",
      "inmobiliaria": "Inmobiliaria"
    },
    "tipos": {
      "CASA": "Casa",
      "DEPARTAMENTO": "Departamento",
      "LOCAL_COMERCIAL": "Local Comercial",
      "OFICINA": "Oficina",
      "BODEGA": "Bodega",
      "TERRENO": "Terreno"
    }
  },
  "personas": {
    "title": "Gestión de Personas",
    "create": "Nueva Persona",
    "edit": "Editar Persona",
    "fields": {
      "tipoPersona": "Tipo",
      "nombre": "Nombre",
      "apellidos": "Apellidos",
      "razonSocial": "Razón Social",
      "rfc": "RFC",
      "curp": "CURP",
      "email": "Correo Electrónico",
      "telefono": "Teléfono",
      "activo": "Activo"
    },
    "tipos": {
      "ARRENDADOR": "Arrendador",
      "ARRENDATARIO": "Arrendatario",
      "FIADOR": "Fiador",
      "OTRO": "Otro"
    }
  }
}
```

**File:** `src/i18n/locales/en.json` (add to existing JSON)
```json
{
  "common": {
    "create": "Create",
    "edit": "Edit",
    "delete": "Delete",
    "cancel": "Cancel",
    "save": "Save",
    "search": "Search",
    "actions": "Actions",
    "loading": "Loading...",
    "noData": "No data available",
    "confirmDelete": "Are you sure you want to delete this record?",
    "deleteSuccess": "Record deleted successfully",
    "deleteError": "Error deleting record",
    "saveSuccess": "Record saved successfully",
    "saveError": "Error saving record"
  },
  "nav": {
    "dashboard": "Dashboard",
    "inmobiliarias": "Agencies",
    "propiedades": "Properties",
    "personas": "Contacts",
    "contratos": "Contracts",
    "chat": "Agent Chat"
  },
  "inmobiliarias": {
    "title": "Agency Management",
    "create": "New Agency",
    "edit": "Edit Agency",
    "fields": {
      "nombre": "Name",
      "rfc": "Tax ID (RFC)",
      "nombreContacto": "Contact Name",
      "correo": "Email",
      "telefono": "Phone"
    },
    "validation": {
      "nombreRequired": "Name is required",
      "rfcRequired": "RFC is required",
      "rfcInvalid": "Invalid RFC",
      "correoRequired": "Email is required",
      "correoInvalid": "Invalid email address"
    }
  },
  "propiedades": {
    "title": "Property Management",
    "create": "New Property",
    "edit": "Edit Property",
    "fields": {
      "nombre": "Name",
      "tipo": "Type",
      "direccion": "Address",
      "observaciones": "Notes",
      "inmobiliaria": "Agency"
    },
    "tipos": {
      "CASA": "House",
      "DEPARTAMENTO": "Apartment",
      "LOCAL_COMERCIAL": "Commercial Space",
      "OFICINA": "Office",
      "BODEGA": "Warehouse",
      "TERRENO": "Land"
    }
  },
  "personas": {
    "title": "Contact Management",
    "create": "New Contact",
    "edit": "Edit Contact",
    "fields": {
      "tipoPersona": "Type",
      "nombre": "First Name",
      "apellidos": "Last Name",
      "razonSocial": "Company Name",
      "rfc": "Tax ID (RFC)",
      "curp": "CURP",
      "email": "Email",
      "telefono": "Phone",
      "activo": "Active"
    },
    "tipos": {
      "ARRENDADOR": "Landlord",
      "ARRENDATARIO": "Tenant",
      "FIADOR": "Guarantor",
      "OTRO": "Other"
    }
  }
}
```

**Deliverables:**
- ✅ Updated `src/i18n/locales/es.json`
- ✅ Updated `src/i18n/locales/en.json`

---

## Phase 2: Common Components (Week 1, Days 3-5)

### 2.1 Create Reusable Components

**File:** `src/components/Common/LoadingSpinner.tsx`
```typescript
import { Box, CircularProgress } from '@mui/material';

interface LoadingSpinnerProps {
  size?: number;
  fullScreen?: boolean;
}

export const LoadingSpinner = ({ size = 40, fullScreen = false }: LoadingSpinnerProps) => {
  return (
    <Box
      display="flex"
      justifyContent="center"
      alignItems="center"
      minHeight={fullScreen ? '100vh' : '200px'}
    >
      <CircularProgress size={size} />
    </Box>
  );
};
```

**File:** `src/components/Common/DeleteConfirmDialog.tsx`
```typescript
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Button
} from '@mui/material';
import { useTranslation } from 'react-i18next';

interface DeleteConfirmDialogProps {
  open: boolean;
  title: string;
  message?: string;
  onConfirm: () => void;
  onCancel: () => void;
  loading?: boolean;
}

export const DeleteConfirmDialog = ({
  open,
  title,
  message,
  onConfirm,
  onCancel,
  loading = false
}: DeleteConfirmDialogProps) => {
  const { t } = useTranslation();

  return (
    <Dialog open={open} onClose={onCancel}>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <DialogContentText>
          {message || t('common.confirmDelete')}
        </DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button onClick={onCancel} disabled={loading}>
          {t('common.cancel')}
        </Button>
        <Button onClick={onConfirm} color="error" variant="contained" disabled={loading}>
          {t('common.delete')}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
```

**File:** `src/components/Common/SnackbarNotification.tsx`
```typescript
import { Snackbar, Alert, AlertColor } from '@mui/material';

interface SnackbarNotificationProps {
  open: boolean;
  message: string;
  severity: AlertColor;
  onClose: () => void;
  autoHideDuration?: number;
}

export const SnackbarNotification = ({
  open,
  message,
  severity,
  onClose,
  autoHideDuration = 6000
}: SnackbarNotificationProps) => {
  return (
    <Snackbar
      open={open}
      autoHideDuration={autoHideDuration}
      onClose={onClose}
      anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
    >
      <Alert onClose={onClose} severity={severity} sx={{ width: '100%' }}>
        {message}
      </Alert>
    </Snackbar>
  );
};
```

**File:** `src/components/Common/index.ts`
```typescript
export { LoadingSpinner } from './LoadingSpinner';
export { DeleteConfirmDialog } from './DeleteConfirmDialog';
export { SnackbarNotification } from './SnackbarNotification';
```

**Deliverables:**
- ✅ `src/components/Common/LoadingSpinner.tsx`
- ✅ `src/components/Common/DeleteConfirmDialog.tsx`
- ✅ `src/components/Common/SnackbarNotification.tsx`
- ✅ `src/components/Common/index.ts`

---

### 2.2 Create Navigation Drawer

**File:** `src/components/Layout/NavigationDrawer.tsx`
```typescript
import { Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Divider } from '@mui/material';
import { useNavigate, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import DashboardIcon from '@mui/icons-material/Dashboard';
import BusinessIcon from '@mui/icons-material/Business';
import HomeIcon from '@mui/icons-material/Home';
import PeopleIcon from '@mui/icons-material/People';
import DescriptionIcon from '@mui/icons-material/Description';
import ChatIcon from '@mui/icons-material/Chat';

interface NavigationDrawerProps {
  open: boolean;
  onClose: () => void;
}

export const NavigationDrawer = ({ open, onClose }: NavigationDrawerProps) => {
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
      <List sx={{ width: 250 }}>
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
            <ListItemIcon><ChatIcon /></ListItemIcon>
            <ListItemText primary={t('nav.chat')} />
          </ListItemButton>
        </ListItem>
      </List>
    </Drawer>
  );
};
```

**Update File:** `src/components/Layout/Header.tsx`
Add menu button to open drawer:
```typescript
import { useState } from 'react';
import { AppBar, Toolbar, Typography, IconButton, Box } from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import { NavigationDrawer } from './NavigationDrawer';
// ... existing imports

export const Header = () => {
  const [drawerOpen, setDrawerOpen] = useState(false);
  // ... existing code

  return (
    <>
      <AppBar position="static">
        <Toolbar>
          <IconButton
            edge="start"
            color="inherit"
            onClick={() => setDrawerOpen(true)}
            sx={{ mr: 2 }}
          >
            <MenuIcon />
          </IconButton>
          {/* ... rest of existing Header code */}
        </Toolbar>
      </AppBar>
      <NavigationDrawer open={drawerOpen} onClose={() => setDrawerOpen(false)} />
    </>
  );
};
```

**Deliverables:**
- ✅ `src/components/Layout/NavigationDrawer.tsx`
- ✅ Updated `src/components/Layout/Header.tsx`

---

## Phase 3: Inmobiliarias CRUD (Week 2)

### 3.1 Create Inmobiliarias List Page

**File:** `src/pages/Inmobiliarias/InmobiliariasPage.tsx`
```typescript
import { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  IconButton,
  TextField,
  InputAdornment
} from '@mui/material';
import { DataGrid, GridColDef, GridActionsCellItem } from '@mui/x-data-grid';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import SearchIcon from '@mui/icons-material/Search';
import { useTranslation } from 'react-i18next';
import { inmobiliariaService } from '@services';
import type { Inmobiliaria } from '@/types';
import { LoadingSpinner, DeleteConfirmDialog, SnackbarNotification } from '@components/Common';
import { InmobiliariaFormDialog } from './InmobiliariaFormDialog';

export const InmobiliariasPage = () => {
  const { t } = useTranslation();
  const [inmobiliarias, setInmobiliarias] = useState<Inmobiliaria[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchText, setSearchText] = useState('');
  const [formOpen, setFormOpen] = useState(false);
  const [selectedInmobiliaria, setSelectedInmobiliaria] = useState<Inmobiliaria | null>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [itemToDelete, setItemToDelete] = useState<number | null>(null);
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as any });

  useEffect(() => {
    loadInmobiliarias();
  }, []);

  const loadInmobiliarias = async () => {
    try {
      setLoading(true);
      const data = await inmobiliariaService.getAll();
      setInmobiliarias(data);
    } catch (error) {
      setSnackbar({ open: true, message: t('common.saveError'), severity: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setSelectedInmobiliaria(null);
    setFormOpen(true);
  };

  const handleEdit = (inmobiliaria: Inmobiliaria) => {
    setSelectedInmobiliaria(inmobiliaria);
    setFormOpen(true);
  };

  const handleDeleteClick = (id: number) => {
    setItemToDelete(id);
    setDeleteDialogOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (itemToDelete === null) return;

    try {
      await inmobiliariaService.delete(itemToDelete);
      setSnackbar({ open: true, message: t('common.deleteSuccess'), severity: 'success' });
      loadInmobiliarias();
    } catch (error) {
      setSnackbar({ open: true, message: t('common.deleteError'), severity: 'error' });
    } finally {
      setDeleteDialogOpen(false);
      setItemToDelete(null);
    }
  };

  const handleFormSuccess = () => {
    setFormOpen(false);
    loadInmobiliarias();
    setSnackbar({ open: true, message: t('common.saveSuccess'), severity: 'success' });
  };

  const columns: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 70 },
    { field: 'nombre', headerName: t('inmobiliarias.fields.nombre'), flex: 1, minWidth: 200 },
    { field: 'rfc', headerName: t('inmobiliarias.fields.rfc'), width: 150 },
    { field: 'nombreContacto', headerName: t('inmobiliarias.fields.nombreContacto'), width: 200 },
    { field: 'correo', headerName: t('inmobiliarias.fields.correo'), width: 200 },
    { field: 'telefono', headerName: t('inmobiliarias.fields.telefono'), width: 150 },
    {
      field: 'actions',
      type: 'actions',
      headerName: t('common.actions'),
      width: 100,
      getActions: (params) => [
        <GridActionsCellItem
          icon={<EditIcon />}
          label={t('common.edit')}
          onClick={() => handleEdit(params.row)}
        />,
        <GridActionsCellItem
          icon={<DeleteIcon />}
          label={t('common.delete')}
          onClick={() => handleDeleteClick(params.row.id)}
        />
      ]
    }
  ];

  const filteredInmobiliarias = inmobiliarias.filter((item) =>
    Object.values(item).some((val) =>
      String(val).toLowerCase().includes(searchText.toLowerCase())
    )
  );

  if (loading) return <LoadingSpinner fullScreen />;

  return (
    <Box sx={{ p: 3 }}>
      <Paper sx={{ p: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
          <Typography variant="h4">{t('inmobiliarias.title')}</Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleCreate}
          >
            {t('inmobiliarias.create')}
          </Button>
        </Box>

        <TextField
          fullWidth
          placeholder={t('common.search')}
          value={searchText}
          onChange={(e) => setSearchText(e.target.value)}
          sx={{ mb: 2 }}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon />
              </InputAdornment>
            )
          }}
        />

        <DataGrid
          rows={filteredInmobiliarias}
          columns={columns}
          initialState={{
            pagination: { paginationModel: { pageSize: 10 } }
          }}
          pageSizeOptions={[5, 10, 25, 50]}
          disableRowSelectionOnClick
          autoHeight
        />
      </Paper>

      <InmobiliariaFormDialog
        open={formOpen}
        inmobiliaria={selectedInmobiliaria}
        onClose={() => setFormOpen(false)}
        onSuccess={handleFormSuccess}
      />

      <DeleteConfirmDialog
        open={deleteDialogOpen}
        title={t('common.delete')}
        onConfirm={handleDeleteConfirm}
        onCancel={() => setDeleteDialogOpen(false)}
      />

      <SnackbarNotification
        open={snackbar.open}
        message={snackbar.message}
        severity={snackbar.severity}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
      />
    </Box>
  );
};
```

**Deliverable:** ✅ `src/pages/Inmobiliarias/InmobiliariasPage.tsx`

---

### 3.2 Create Inmobiliarias Form Dialog

**File:** `src/pages/Inmobiliarias/InmobiliariaFormDialog.tsx`
```typescript
import { useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Grid
} from '@mui/material';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { useTranslation } from 'react-i18next';
import { inmobiliariaService } from '@services';
import type { Inmobiliaria, InmobiliariaCreateRequest } from '@/types';

interface InmobiliariaFormDialogProps {
  open: boolean;
  inmobiliaria: Inmobiliaria | null;
  onClose: () => void;
  onSuccess: () => void;
}

const getValidationSchema = (t: any) => yup.object({
  nombre: yup.string().required(t('inmobiliarias.validation.nombreRequired')),
  rfc: yup.string().required(t('inmobiliarias.validation.rfcRequired')),
  nombreContacto: yup.string().required(t('inmobiliarias.validation.nombreRequired')),
  correo: yup.string()
    .email(t('inmobiliarias.validation.correoInvalid'))
    .required(t('inmobiliarias.validation.correoRequired')),
  telefono: yup.string().required()
});

export const InmobiliariaFormDialog = ({
  open,
  inmobiliaria,
  onClose,
  onSuccess
}: InmobiliariaFormDialogProps) => {
  const { t } = useTranslation();
  const isEdit = !!inmobiliaria;

  const { control, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm({
    resolver: yupResolver(getValidationSchema(t)),
    defaultValues: {
      nombre: '',
      rfc: '',
      nombreContacto: '',
      correo: '',
      telefono: ''
    }
  });

  useEffect(() => {
    if (inmobiliaria) {
      reset(inmobiliaria);
    } else {
      reset({
        nombre: '',
        rfc: '',
        nombreContacto: '',
        correo: '',
        telefono: ''
      });
    }
  }, [inmobiliaria, reset]);

  const onSubmit = async (data: InmobiliariaCreateRequest) => {
    try {
      if (isEdit && inmobiliaria) {
        await inmobiliariaService.update(inmobiliaria.id, data);
      } else {
        await inmobiliariaService.create(data);
      }
      onSuccess();
    } catch (error) {
      console.error('Error saving inmobiliaria:', error);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <form onSubmit={handleSubmit(onSubmit)}>
        <DialogTitle>
          {isEdit ? t('inmobiliarias.edit') : t('inmobiliarias.create')}
        </DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <Controller
                name="nombre"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    fullWidth
                    label={t('inmobiliarias.fields.nombre')}
                    error={!!errors.nombre}
                    helperText={errors.nombre?.message}
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <Controller
                name="rfc"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    fullWidth
                    label={t('inmobiliarias.fields.rfc')}
                    error={!!errors.rfc}
                    helperText={errors.rfc?.message}
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <Controller
                name="nombreContacto"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    fullWidth
                    label={t('inmobiliarias.fields.nombreContacto')}
                    error={!!errors.nombreContacto}
                    helperText={errors.nombreContacto?.message}
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <Controller
                name="correo"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    fullWidth
                    type="email"
                    label={t('inmobiliarias.fields.correo')}
                    error={!!errors.correo}
                    helperText={errors.correo?.message}
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <Controller
                name="telefono"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    fullWidth
                    label={t('inmobiliarias.fields.telefono')}
                    error={!!errors.telefono}
                    helperText={errors.telefono?.message}
                  />
                )}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose} disabled={isSubmitting}>
            {t('common.cancel')}
          </Button>
          <Button type="submit" variant="contained" disabled={isSubmitting}>
            {t('common.save')}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};
```

**Deliverable:** ✅ `src/pages/Inmobiliarias/InmobiliariaFormDialog.tsx`

---

### 3.3 Create Index Export

**File:** `src/pages/Inmobiliarias/index.ts`
```typescript
export { InmobiliariasPage } from './InmobiliariasPage';
export { InmobiliariaFormDialog } from './InmobiliariaFormDialog';
```

**Deliverable:** ✅ `src/pages/Inmobiliarias/index.ts`

---

### 3.4 Testing Checklist for Inmobiliarias

- [ ] Can view list of inmobiliarias
- [ ] Search/filter works correctly
- [ ] Can create new inmobiliaria
- [ ] Form validation works (required fields, email format, etc.)
- [ ] Can edit existing inmobiliaria
- [ ] Can delete inmobiliaria with confirmation
- [ ] Success/error messages display correctly
- [ ] Loading states work properly
- [ ] Responsive design on mobile/tablet
- [ ] i18n works (Spanish/English)

---

## Phase 4: Propiedades CRUD (Week 2)

### 4.1 Create Propiedades List Page

**File:** `src/pages/Propiedades/PropiedadesPage.tsx`

Follow the same pattern as `InmobiliariasPage.tsx` with these additions:
- Add filter dropdown for Inmobiliaria
- Display `tipo` enum as translated label
- Display `inmobiliariaNombre` in grid
- Include `observaciones` in detail view or tooltip

**Key differences:**
```typescript
// Add filter by inmobiliaria
const [selectedInmobiliariaFilter, setSelectedInmobiliariaFilter] = useState<number | null>(null);

// Load inmobiliarias for dropdown
const [inmobiliarias, setInmobiliarias] = useState<Inmobiliaria[]>([]);

// Columns include tipo with translated label
{
  field: 'tipo',
  headerName: t('propiedades.fields.tipo'),
  width: 150,
  valueGetter: (params) => t(`propiedades.tipos.${params.row.tipo}`)
}
```

**Deliverable:** ✅ `src/pages/Propiedades/PropiedadesPage.tsx`

---

### 4.2 Create Propiedades Form Dialog

**File:** `src/pages/Propiedades/PropiedadFormDialog.tsx`

Follow the same pattern as `InmobiliariaFormDialog.tsx` with these additions:
- Dropdown for `tipo` (PropiedadTipo enum)
- Dropdown for `inmobiliariaId` (select from available inmobiliarias)
- Multiline TextField for `observaciones`

**Key differences:**
```typescript
// Load inmobiliarias for dropdown
useEffect(() => {
  const loadInmobiliarias = async () => {
    const data = await inmobiliariaService.getAll();
    setInmobiliarias(data);
  };
  loadInmobiliarias();
}, []);

// Tipo dropdown
<Controller
  name="tipo"
  control={control}
  render={({ field }) => (
    <TextField
      {...field}
      select
      fullWidth
      label={t('propiedades.fields.tipo')}
    >
      {Object.values(PropiedadTipo).map((tipo) => (
        <MenuItem key={tipo} value={tipo}>
          {t(`propiedades.tipos.${tipo}`)}
        </MenuItem>
      ))}
    </TextField>
  )}
/>
```

**Deliverable:** ✅ `src/pages/Propiedades/PropiedadFormDialog.tsx`

---

### 4.3 Create Index Export

**File:** `src/pages/Propiedades/index.ts`

**Deliverable:** ✅ `src/pages/Propiedades/index.ts`

---

## Phase 5: Personas CRUD (Week 3)

### 5.1 Create Personas List Page

**File:** `src/pages/Personas/PersonasPage.tsx`

Follow the same pattern with these additions:
- Display `tipoPersona` as translated label
- Show `activo` status with badge/chip
- Format `fechaAlta` as readable date
- Display full name (nombre + apellidos) or razonSocial

**Deliverable:** ✅ `src/pages/Personas/PersonasPage.tsx`

---

### 5.2 Create Personas Form Dialog

**File:** `src/pages/Personas/PersonaFormDialog.tsx`

Follow the same pattern with these additions:
- Dropdown for `tipoPersona`
- Conditional fields based on persona type (individual vs company)
- Switch for `activo` status
- Optional fields marked clearly

**Deliverable:** ✅ `src/pages/Personas/PersonaFormDialog.tsx`

---

## Phase 6: Dashboard & Routing Integration (Week 3)

### 6.1 Create Dashboard Page

**File:** `src/pages/Dashboard/DashboardPage.tsx`
```typescript
import { Grid, Paper, Typography, Box } from '@mui/material';
import { useTranslation } from 'react-i18next';
import BusinessIcon from '@mui/icons-material/Business';
import HomeIcon from '@mui/icons-material/Home';
import PeopleIcon from '@mui/icons-material/People';
import DescriptionIcon from '@mui/icons-material/Description';

export const DashboardPage = () => {
  const { t } = useTranslation();

  const stats = [
    { label: t('nav.inmobiliarias'), value: 0, icon: <BusinessIcon fontSize="large" />, color: '#1976d2' },
    { label: t('nav.propiedades'), value: 0, icon: <HomeIcon fontSize="large" />, color: '#388e3c' },
    { label: t('nav.personas'), value: 0, icon: <PeopleIcon fontSize="large" />, color: '#f57c00' },
    { label: t('nav.contratos'), value: 0, icon: <DescriptionIcon fontSize="large" />, color: '#d32f2f' }
  ];

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" sx={{ mb: 3 }}>
        {t('nav.dashboard')}
      </Typography>
      <Grid container spacing={3}>
        {stats.map((stat) => (
          <Grid item xs={12} sm={6} md={3} key={stat.label}>
            <Paper sx={{ p: 3, display: 'flex', alignItems: 'center', gap: 2 }}>
              <Box sx={{ color: stat.color }}>{stat.icon}</Box>
              <Box>
                <Typography variant="h4">{stat.value}</Typography>
                <Typography variant="body2" color="text.secondary">
                  {stat.label}
                </Typography>
              </Box>
            </Paper>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};
```

**Optional Enhancement:** Load actual counts from backend API

**Deliverable:** ✅ `src/pages/Dashboard/DashboardPage.tsx`

---

### 6.2 Update App Routes

**Update File:** `src/App.tsx`
```typescript
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { LoginForm } from '@components/Auth';
import { PrivateRoute } from '@components/Auth';
import { ChatContainer } from '@components/Chat';
import { DashboardPage } from '@/pages/Dashboard/DashboardPage';
import { InmobiliariasPage } from '@/pages/Inmobiliarias';
import { PropiedadesPage } from '@/pages/Propiedades';
import { PersonasPage } from '@/pages/Personas';
// Import Header, etc.

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<LoginForm />} />
        <Route
          path="/"
          element={
            <PrivateRoute>
              <Header />
              <DashboardPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/inmobiliarias"
          element={
            <PrivateRoute>
              <Header />
              <InmobiliariasPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/propiedades"
          element={
            <PrivateRoute>
              <Header />
              <PropiedadesPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/personas"
          element={
            <PrivateRoute>
              <Header />
              <PersonasPage />
            </PrivateRoute>
          }
        />
        <Route
          path="/chat"
          element={
            <PrivateRoute>
              <Header />
              <ChatContainer />
            </PrivateRoute>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
```

**Deliverable:** ✅ Updated `src/App.tsx`

---

## Phase 7: Testing & Refinement (Week 4)

### 7.1 End-to-End Testing Checklist

**For each entity (Inmobiliarias, Propiedades, Personas):**
- [ ] List view loads correctly with data from backend
- [ ] Search/filter functionality works
- [ ] Create new record successfully
- [ ] Edit existing record successfully
- [ ] Delete record with confirmation
- [ ] Form validation prevents invalid submissions
- [ ] Error messages display correctly
- [ ] Success notifications appear
- [ ] Loading states show during API calls
- [ ] Navigation between sections works
- [ ] Mobile responsive layout works
- [ ] i18n switches between Spanish/English correctly

---

### 7.2 Cross-Browser Testing

Test on:
- [ ] Chrome/Edge (Chromium)
- [ ] Firefox
- [ ] Safari (if applicable)
- [ ] Mobile browsers (iOS Safari, Chrome Mobile)

---

### 7.3 Performance Optimization

- [ ] Implement pagination for large datasets
- [ ] Add debouncing to search inputs
- [ ] Lazy load components with `React.lazy()`
- [ ] Optimize DataGrid rendering for 1000+ rows
- [ ] Cache API responses where appropriate

---

### 7.4 Accessibility (a11y)

- [ ] All interactive elements keyboard accessible
- [ ] Form fields have proper labels
- [ ] Error messages announced to screen readers
- [ ] Color contrast meets WCAG AA standards
- [ ] Focus indicators visible

---

## Phase 8: Documentation & Handoff (Week 4)

### 8.1 Create User Documentation

**File:** `docs/USER-GUIDE-CRUD.md`
- How to navigate between sections
- How to create, edit, delete records
- Search/filter usage
- Screenshots of each page

---

### 8.2 Create Developer Documentation

**File:** `docs/FRONTEND-ARCHITECTURE.md`
- Component structure
- Service layer patterns
- Type definitions
- Adding new CRUD modules
- Internationalization workflow

---

### 8.3 Update README

Update main `README.md` with:
- New features added
- How to run frontend
- Build instructions
- Environment variables (if any)

---

## Appendix A: Common Issues & Solutions

### Issue: API Calls Failing in Development
**Solution:** Ensure Vite proxy is configured in `vite.config.ts`:
```typescript
server: {
  proxy: {
    '/api': 'http://localhost:8080'
  }
}
```

### Issue: TypeScript Path Aliases Not Working
**Solution:** Verify both `tsconfig.json` and `vite.config.ts` have matching path configurations.

### Issue: Form Validation Not Triggering
**Solution:** Ensure `yup` schema is correctly linked via `yupResolver` in `useForm`.

### Issue: DataGrid Not Displaying Data
**Solution:** Ensure rows have unique `id` field. If backend uses different identifier, map it:
```typescript
rows={data.map(item => ({ ...item, id: item.customId }))}
```

---

## Appendix B: Code Review Checklist

Before submitting for review:
- [ ] All TypeScript types properly defined
- [ ] No `any` types used
- [ ] All strings externalized to i18n
- [ ] Error handling implemented
- [ ] Loading states shown
- [ ] Forms have validation
- [ ] Components follow existing patterns
- [ ] Code formatted consistently
- [ ] No console.log statements
- [ ] Imports use path aliases (`@/`, `@components/`, etc.)

---

## Appendix C: Deployment Checklist

### Before Production Deployment:
- [ ] Run `npm run build` successfully
- [ ] Test production build with `npm run preview`
- [ ] Verify all API endpoints use relative paths
- [ ] Check bundle size (`dist/` folder)
- [ ] Remove any development/debug code
- [ ] Update environment variables if needed
- [ ] Test on production-like environment
- [ ] Verify authentication flow works
- [ ] Check all routes are accessible
- [ ] Confirm Spring Boot serves static files correctly

---

## Summary

This guide provides a complete roadmap for implementing traditional CRUD interfaces alongside the existing agent chat. The phased approach ensures:

1. **Solid foundation** - Types, services, and common components first
2. **Iterative development** - One entity at a time, reusing patterns
3. **Quality assurance** - Testing at each phase
4. **Maintainability** - Clear documentation and consistent patterns

**Estimated Timeline:**
- Week 1: Foundation + Common Components + Navigation
- Week 2: Inmobiliarias + Propiedades CRUD
- Week 3: Personas CRUD + Dashboard + Routing
- Week 4: Testing, Refinement, Documentation

**Team Recommendations:**
- 2-3 developers
- 1 designer (optional, MUI provides good defaults)
- 1 QA tester

**Success Criteria:**
- All entities have functional CRUD interfaces
- Navigation between Chat and CRUD works seamlessly
- Forms validate correctly
- Error handling provides clear feedback
- i18n works for both languages
- Mobile responsive
- Passes accessibility standards

Good luck with the implementation! 🚀
