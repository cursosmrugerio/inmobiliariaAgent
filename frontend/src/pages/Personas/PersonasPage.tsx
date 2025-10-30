import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Box, Button, Chip, InputAdornment, Paper, TextField, Typography } from '@mui/material';
import type { AlertColor } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import SearchIcon from '@mui/icons-material/Search';
import {
  DataGrid,
  GridActionsCellItem,
  GridColDef,
  GridRenderCellParams,
  GridRowParams,
} from '@mui/x-data-grid';
import { useTranslation } from 'react-i18next';

import { DeleteConfirmDialog, LoadingSpinner, SnackbarNotification } from '@components/Common';
import { personaService } from '@services';
import type { Persona } from '@/types';
import { PersonaTipo } from '@/types';
import { useDebounce } from '@hooks/useDebounce';
import { PersonaFormDialog } from './PersonaFormDialog';

type SnackbarState = {
  open: boolean;
  message: string;
  severity: AlertColor;
};

const formatDisplayName = (persona: Persona) => {
  if (persona.nombre || persona.apellidos) {
    return `${persona.nombre ?? ''} ${persona.apellidos ?? ''}`.trim();
  }

  if (persona.razonSocial) {
    return persona.razonSocial;
  }

  return '';
};

const formatDate = (value?: string) => {
  if (!value) {
    return '-';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return '-';
  }
  return new Intl.DateTimeFormat(undefined, {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(date);
};

export const PersonasPage: React.FC = () => {
  const { t } = useTranslation();
  const [personas, setPersonas] = useState<Persona[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [searchText, setSearchText] = useState<string>('');
  const [formOpen, setFormOpen] = useState<boolean>(false);
  const [selectedPersona, setSelectedPersona] = useState<Persona | null>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState<boolean>(false);
  const [itemToDelete, setItemToDelete] = useState<number | null>(null);
  const [snackbar, setSnackbar] = useState<SnackbarState>({
    open: false,
    message: '',
    severity: 'success',
  });
  const debouncedSearchText = useDebounce(searchText, 300);

  const loadPersonas = useCallback(async () => {
    try {
      setLoading(true);
      const data = await personaService.getAll();
      setPersonas(data);
    } catch (error) {
      console.error('Error loading personas', error);
      setSnackbar({
        open: true,
        message: t('common.saveError'),
        severity: 'error',
      });
    } finally {
      setLoading(false);
    }
  }, [t]);

  useEffect(() => {
    void loadPersonas();
  }, [loadPersonas]);

  const handleCreate = useCallback(() => {
    setSelectedPersona(null);
    setFormOpen(true);
  }, []);

  const handleEdit = useCallback((persona: Persona) => {
    setSelectedPersona(persona);
    setFormOpen(true);
  }, []);

  const handleDeleteClick = useCallback((id: number) => {
    setItemToDelete(id);
    setDeleteDialogOpen(true);
  }, []);

  const handleDeleteConfirm = useCallback(async () => {
    if (itemToDelete === null) {
      return;
    }

    try {
      await personaService.delete(itemToDelete);
      setSnackbar({
        open: true,
        message: t('common.deleteSuccess'),
        severity: 'success',
      });
      await loadPersonas();
    } catch (error) {
      console.error('Error deleting persona', error);
      setSnackbar({
        open: true,
        message: t('common.deleteError'),
        severity: 'error',
      });
    } finally {
      setDeleteDialogOpen(false);
      setItemToDelete(null);
    }
  }, [itemToDelete, loadPersonas, t]);

  const handleFormSuccess = useCallback(async () => {
    setFormOpen(false);
    await loadPersonas();
    setSnackbar({
      open: true,
      message: t('common.saveSuccess'),
      severity: 'success',
    });
  }, [loadPersonas, t]);

  const columns: GridColDef[] = useMemo(
    () => [
      { field: 'id', headerName: 'ID', width: 80 },
      {
        field: 'tipoPersona',
        headerName: t('personas.fields.tipoPersona'),
        width: 160,
        valueFormatter: (value: PersonaTipo) =>
          t(`personas.tipos.${value}`),
      },
      {
        field: 'nombreCompleto',
        headerName: t('personas.fields.nombre'),
        flex: 1,
        minWidth: 220,
        valueGetter: (_value: any, row: Persona) => formatDisplayName(row),
      },
      {
        field: 'rfc',
        headerName: t('personas.fields.rfc'),
        width: 160,
      },
      {
        field: 'email',
        headerName: t('personas.fields.email'),
        width: 220,
      },
      {
        field: 'telefono',
        headerName: t('personas.fields.telefono'),
        width: 160,
      },
      {
        field: 'fechaAlta',
        headerName: t('personas.fields.fechaAlta'),
        width: 160,
        valueFormatter: ({ value }: { value: unknown }) => formatDate(value as string),
      },
      {
        field: 'activo',
        headerName: t('personas.fields.activo'),
        width: 140,
        renderCell: (params: GridRenderCellParams<any, boolean>) => (
          <Chip
            label={params.value ? t('personas.status.active') : t('personas.status.inactive')}
            color={params.value ? 'success' : 'default'}
            size="small"
            variant={params.value ? 'filled' : 'outlined'}
          />
        ),
      },
      {
        field: 'actions',
        type: 'actions',
        headerName: t('common.actions'),
        width: 120,
        getActions: (params: GridRowParams) => [
          <GridActionsCellItem
            key="edit"
            icon={<EditIcon />}
            label={t('common.edit')}
            onClick={() => handleEdit(params.row as Persona)}
            showInMenu={false}
          />,
          <GridActionsCellItem
            key="delete"
            icon={<DeleteIcon />}
            label={t('common.delete')}
            onClick={() => handleDeleteClick((params.row as Persona).id)}
            showInMenu={false}
          />,
        ],
      },
    ],
    [handleDeleteClick, handleEdit, t],
  );

  const filteredPersonas = useMemo(
    () =>
      personas.filter((persona) =>
        Object.values(persona).some((value) =>
          String(value ?? '')
            .toLowerCase()
            .includes(debouncedSearchText.trim().toLowerCase()),
        ),
      ),
    [personas, debouncedSearchText],
  );

  if (loading) {
    return <LoadingSpinner fullScreen />;
  }

  return (
    <Box sx={{ p: 3 }}>
      <Paper sx={{ p: 3 }}>
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'space-between',
            flexWrap: 'wrap',
            gap: 2,
            mb: 3,
          }}
        >
          <Typography variant="h4">{t('personas.title')}</Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={handleCreate}>
            {t('personas.create')}
          </Button>
        </Box>

        <TextField
          fullWidth
          placeholder={t('common.search')}
          value={searchText}
          onChange={(event) => setSearchText(event.target.value)}
          sx={{ mb: 2 }}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon />
              </InputAdornment>
            ),
          }}
        />

        <DataGrid
          rows={filteredPersonas}
          columns={columns}
          initialState={{
            pagination: { paginationModel: { pageSize: 10 } },
          }}
          pageSizeOptions={[5, 10, 25, 50]}
          disableRowSelectionOnClick
          autoHeight
        />
      </Paper>

      <PersonaFormDialog
        open={formOpen}
        persona={selectedPersona}
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
        onClose={() => setSnackbar((prev) => ({ ...prev, open: false }))}
      />
    </Box>
  );
};
