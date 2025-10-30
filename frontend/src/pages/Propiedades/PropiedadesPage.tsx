import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Box,
  Button,
  FormControl,
  InputAdornment,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import type { AlertColor } from '@mui/material';
import type { SelectChangeEvent } from '@mui/material/Select';
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
import { inmobiliariaService, propiedadService } from '@services';
import type { Inmobiliaria, Propiedad } from '@/types';
import { useDebounce } from '@hooks/useDebounce';
import { PropiedadFormDialog } from './PropiedadFormDialog';

type SnackbarState = {
  open: boolean;
  message: string;
  severity: AlertColor;
};

type InmobiliariaFilter = 'all' | number;

export const PropiedadesPage: React.FC = () => {
  const { t } = useTranslation();
  const [propiedades, setPropiedades] = useState<Propiedad[]>([]);
  const [inmobiliarias, setInmobiliarias] = useState<Inmobiliaria[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [searchText, setSearchText] = useState<string>('');
  const [selectedFilter, setSelectedFilter] = useState<InmobiliariaFilter>('all');
  const [formOpen, setFormOpen] = useState<boolean>(false);
  const [selectedPropiedad, setSelectedPropiedad] = useState<Propiedad | null>(null);
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
      const data = await inmobiliariaService.getAll();
      setInmobiliarias(data);
    } catch (error) {
      console.error('Error loading inmobiliarias', error);
      setSnackbar({
        open: true,
        message: t('common.saveError'),
        severity: 'error',
      });
    }
  }, [t]);

  const loadPropiedades = useCallback(
    async (inmobiliariaId?: number) => {
      try {
        setLoading(true);
        const data = await propiedadService.getAll(inmobiliariaId);
        setPropiedades(data);
      } catch (error) {
        console.error('Error loading propiedades', error);
        setSnackbar({
          open: true,
          message: t('common.saveError'),
          severity: 'error',
        });
      } finally {
        setLoading(false);
      }
    },
    [t],
  );

  useEffect(() => {
    void loadInmobiliarias();
  }, [loadInmobiliarias]);

  useEffect(() => {
    const inmobiliariaId = selectedFilter === 'all' ? undefined : selectedFilter;
    void loadPropiedades(inmobiliariaId);
  }, [loadPropiedades, selectedFilter]);

  const handleCreate = useCallback(() => {
    setSelectedPropiedad(null);
    setFormOpen(true);
  }, []);

  const handleEdit = useCallback((propiedad: Propiedad) => {
    setSelectedPropiedad(propiedad);
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
      await propiedadService.delete(itemToDelete);
      setSnackbar({
        open: true,
        message: t('common.deleteSuccess'),
        severity: 'success',
      });
      const inmobiliariaId = selectedFilter === 'all' ? undefined : selectedFilter;
      await loadPropiedades(inmobiliariaId);
    } catch (error) {
      console.error('Error deleting propiedad', error);
      setSnackbar({
        open: true,
        message: t('common.deleteError'),
        severity: 'error',
      });
    } finally {
      setDeleteDialogOpen(false);
      setItemToDelete(null);
    }
  }, [itemToDelete, loadPropiedades, selectedFilter, t]);

  const handleFormSuccess = useCallback(async () => {
    setFormOpen(false);
    const inmobiliariaId = selectedFilter === 'all' ? undefined : selectedFilter;
    await loadPropiedades(inmobiliariaId);
    setSnackbar({
      open: true,
      message: t('common.saveSuccess'),
      severity: 'success',
    });
  }, [loadPropiedades, selectedFilter, t]);

  const handleFilterChange = useCallback((event: SelectChangeEvent<string>) => {
    const value = event.target.value;
    setSelectedFilter(value === 'all' ? 'all' : Number(value));
  }, []);

  const columns: GridColDef[] = useMemo(
    () => [
      { field: 'id', headerName: 'ID', width: 80 },
      { field: 'nombre', headerName: t('propiedades.fields.nombre'), flex: 1, minWidth: 200 },
      {
        field: 'tipo',
        headerName: t('propiedades.fields.tipo'),
        width: 160,
        valueFormatter: ({ value }: { value: unknown }) =>
          t(`propiedades.tipos.${value as string}`),
      },
      {
        field: 'direccion',
        headerName: t('propiedades.fields.direccion'),
        flex: 1,
        minWidth: 240,
      },
      {
        field: 'observaciones',
        headerName: t('propiedades.fields.observaciones'),
        flex: 1,
        minWidth: 240,
        renderCell: (params: GridRenderCellParams<any, string | null>) => {
          const value = (params.value as string) ?? '';
          const displayValue = value.length > 40 ? `${value.slice(0, 37)}…` : value;
          return (
            <Tooltip title={value}>
              <span>{value ? displayValue : '-'}</span>
            </Tooltip>
          );
        },
      },
      {
        field: 'inmobiliariaNombre',
        headerName: t('propiedades.fields.inmobiliaria'),
        width: 220,
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
            onClick={() => handleEdit(params.row as Propiedad)}
            showInMenu={false}
          />,
          <GridActionsCellItem
            key="delete"
            icon={<DeleteIcon />}
            label={t('common.delete')}
            onClick={() => handleDeleteClick((params.row as Propiedad).id)}
            showInMenu={false}
          />,
        ],
      },
    ],
    [handleDeleteClick, handleEdit, t],
  );

  const filteredPropiedades = useMemo(
    () =>
      propiedades.filter((item) =>
        Object.values(item).some((value) =>
          String(value ?? '')
            .toLowerCase()
            .includes(debouncedSearchText.trim().toLowerCase()),
        ),
      ),
    [propiedades, debouncedSearchText],
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
            flexWrap: 'wrap',
            justifyContent: 'space-between',
            gap: 2,
            mb: 3,
          }}
        >
          <Typography variant="h4">{t('propiedades.title')}</Typography>
          <Button variant="contained" startIcon={<AddIcon />} onClick={handleCreate}>
            {t('propiedades.create')}
          </Button>
        </Box>

        <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 2, mb: 2 }}>
          <TextField
            fullWidth
            placeholder={t('common.search')}
            value={searchText}
            onChange={(event) => setSearchText(event.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              ),
            }}
          />

          <FormControl sx={{ minWidth: { xs: '100%', md: 240 } }} size="small">
            <InputLabel id="propiedades-inmobiliaria-filter-label">
              {t('propiedades.fields.inmobiliaria')}
            </InputLabel>
            <Select
              labelId="propiedades-inmobiliaria-filter-label"
              value={selectedFilter === 'all' ? 'all' : String(selectedFilter)}
              label={t('propiedades.fields.inmobiliaria')}
              onChange={handleFilterChange}
            >
              <MenuItem value="all">{t('common.all')}</MenuItem>
              {inmobiliarias.map((inmobiliaria) => (
                <MenuItem key={inmobiliaria.id} value={inmobiliaria.id}>
                  {inmobiliaria.nombre}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>

        <DataGrid
          rows={filteredPropiedades}
          columns={columns}
          initialState={{
            pagination: { paginationModel: { pageSize: 10 } },
          }}
          pageSizeOptions={[5, 10, 25, 50]}
          disableRowSelectionOnClick
          autoHeight
        />
      </Paper>

      <PropiedadFormDialog
        open={formOpen}
        propiedad={selectedPropiedad}
        onClose={() => setFormOpen(false)}
        onSuccess={handleFormSuccess}
        inmobiliarias={inmobiliarias}
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
