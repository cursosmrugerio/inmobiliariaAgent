import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Box, Button, InputAdornment, Paper, TextField, Typography } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import SearchIcon from '@mui/icons-material/Search';
import {
  DataGrid,
  GridActionsCellItem,
  GridColDef,
  GridRowParams,
} from '@mui/x-data-grid';
import { useTranslation } from 'react-i18next';

import { DeleteConfirmDialog, LoadingSpinner, SnackbarNotification } from '@components/Common';
import { inmobiliariaService } from '@services';
import type { Inmobiliaria } from '@/types';
import { useDebounce } from '@hooks/useDebounce';
import { InmobiliariaFormDialog } from './InmobiliariaFormDialog';
import type { AlertColor } from '@mui/material';

interface SnackbarState {
  open: boolean;
  message: string;
  severity: AlertColor;
}

export const InmobiliariasPage: React.FC = () => {
  const { t } = useTranslation();
  const [inmobiliarias, setInmobiliarias] = useState<Inmobiliaria[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [searchText, setSearchText] = useState<string>('');
  const [formOpen, setFormOpen] = useState<boolean>(false);
  const [selectedInmobiliaria, setSelectedInmobiliaria] = useState<Inmobiliaria | null>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState<boolean>(false);
  const [itemToDelete, setItemToDelete] = useState<number | null>(null);
  const [snackbar, setSnackbar] = useState<SnackbarState>({
    open: false,
    message: '',
    severity: 'success',
  });
  const debouncedSearchText = useDebounce(searchText, 300);

  const loadInmobiliarias = useCallback(async () => {
    try {
      setLoading(true);
      const data = await inmobiliariaService.getAll();
      setInmobiliarias(data);
    } catch (error) {
      console.error('Error loading inmobiliarias', error);
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
    void loadInmobiliarias();
  }, [loadInmobiliarias]);

  const handleCreate = useCallback(() => {
    setSelectedInmobiliaria(null);
    setFormOpen(true);
  }, []);

  const handleEdit = useCallback((inmobiliaria: Inmobiliaria) => {
    setSelectedInmobiliaria(inmobiliaria);
    setFormOpen(true);
  }, []);

  const handleDeleteClick = useCallback((id: number) => {
    setItemToDelete(id);
    setDeleteDialogOpen(true);
  }, []);

  const handleDeleteConfirm = async () => {
    if (itemToDelete === null) {
      return;
    }

    try {
      await inmobiliariaService.delete(itemToDelete);
      setSnackbar({
        open: true,
        message: t('common.deleteSuccess'),
        severity: 'success',
      });
      await loadInmobiliarias();
    } catch (error) {
      console.error('Error deleting inmobiliaria', error);
      setSnackbar({
        open: true,
        message: t('common.deleteError'),
        severity: 'error',
      });
    } finally {
      setDeleteDialogOpen(false);
      setItemToDelete(null);
    }
  };

  const handleFormSuccess = useCallback(async () => {
    setFormOpen(false);
    await loadInmobiliarias();
    setSnackbar({
      open: true,
      message: t('common.saveSuccess'),
      severity: 'success',
    });
  }, [loadInmobiliarias, t]);

  const columns: GridColDef<Inmobiliaria>[] = useMemo(
    () => [
      { field: 'id', headerName: 'ID', width: 80 },
      { field: 'nombre', headerName: t('inmobiliarias.fields.nombre'), flex: 1, minWidth: 200 },
      { field: 'rfc', headerName: t('inmobiliarias.fields.rfc'), width: 160 },
      {
        field: 'nombreContacto',
        headerName: t('inmobiliarias.fields.nombreContacto'),
        width: 220,
      },
      { field: 'correo', headerName: t('inmobiliarias.fields.correo'), width: 220 },
      { field: 'telefono', headerName: t('inmobiliarias.fields.telefono'), width: 160 },
      {
        field: 'actions',
        type: 'actions',
        headerName: t('common.actions'),
        width: 120,
        getActions: (params: GridRowParams<Inmobiliaria>) => [
          <GridActionsCellItem
            key="edit"
            icon={<EditIcon />}
            label={t('common.edit')}
            onClick={() => handleEdit(params.row)}
            showInMenu={false}
          />,
          <GridActionsCellItem
            key="delete"
            icon={<DeleteIcon />}
            label={t('common.delete')}
            onClick={() => handleDeleteClick(params.row.id)}
            showInMenu={false}
          />,
        ],
      },
    ],
    [handleDeleteClick, handleEdit, t],
  );

  const filteredInmobiliarias = useMemo(
    () =>
      inmobiliarias.filter((item) =>
        Object.values(item).some((value) =>
          String(value ?? '')
            .toLowerCase()
            .includes(debouncedSearchText.trim().toLowerCase()),
        ),
      ),
    [inmobiliarias, debouncedSearchText],
  );

  if (loading) {
    return <LoadingSpinner fullScreen />;
  }

  return (
    <Box sx={{ p: 3 }}>
      <Paper sx={{ p: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3, flexWrap: 'wrap', gap: 2 }}>
          <Typography variant="h4">{t('inmobiliarias.title')}</Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={handleCreate}>
            {t('inmobiliarias.create')}
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
          rows={filteredInmobiliarias}
          columns={columns}
          initialState={{
            pagination: { paginationModel: { pageSize: 10 } },
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
        onClose={() => setSnackbar((prev) => ({ ...prev, open: false }))}
      />
    </Box>
  );
};
