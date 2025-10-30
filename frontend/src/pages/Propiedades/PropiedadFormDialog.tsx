import React, { useEffect, useMemo } from 'react';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Grid,
  MenuItem,
  TextField,
} from '@mui/material';
import { Controller, useForm, type Resolver } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { useTranslation } from 'react-i18next';

import { propiedadService } from '@services';
import type { Inmobiliaria, Propiedad, PropiedadCreateRequest } from '@/types';
import { PropiedadTipo } from '@/types';

interface PropiedadFormDialogProps {
  open: boolean;
  propiedad: Propiedad | null;
  inmobiliarias: Inmobiliaria[];
  onClose: () => void;
  onSuccess: () => void;
}

type PropiedadFormValues = {
  nombre: string;
  tipo: PropiedadTipo;
  direccion: string;
  observaciones: string;
  inmobiliariaId: string;
};

const buildValidationSchema = (t: ReturnType<typeof useTranslation>['t']) =>
  yup.object({
    nombre: yup.string().required(t('propiedades.validation.nombreRequired')),
    tipo: yup
      .mixed<PropiedadTipo>()
      .oneOf(Object.values(PropiedadTipo), t('propiedades.validation.tipoRequired'))
      .required(t('propiedades.validation.tipoRequired')),
    direccion: yup.string().required(t('propiedades.validation.direccionRequired')),
    inmobiliariaId: yup
      .string()
      .required(t('propiedades.validation.inmobiliariaRequired')),
    observaciones: yup.string().transform((value) => value ?? '').optional(),
  });

export const PropiedadFormDialog: React.FC<PropiedadFormDialogProps> = ({
  open,
  propiedad,
  inmobiliarias,
  onClose,
  onSuccess,
}) => {
  const { t } = useTranslation();
  const isEdit = Boolean(propiedad);

  const validationSchema = useMemo(() => buildValidationSchema(t), [t]);

  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<PropiedadFormValues>({
    resolver: yupResolver(validationSchema) as Resolver<PropiedadFormValues>,
    defaultValues: {
      nombre: '',
      tipo: PropiedadTipo.CASA,
      direccion: '',
      observaciones: '',
      inmobiliariaId: '',
    },
  });

  useEffect(() => {
    if (propiedad) {
      reset({
        nombre: propiedad.nombre,
        tipo: propiedad.tipo,
        direccion: propiedad.direccion,
        observaciones: propiedad.observaciones,
        inmobiliariaId: String(propiedad.inmobiliariaId),
      });
    } else {
      reset({
        nombre: '',
        tipo: PropiedadTipo.CASA,
        direccion: '',
        observaciones: '',
        inmobiliariaId: '',
      });
    }
  }, [propiedad, reset]);

  const onSubmit = async (formValues: PropiedadFormValues) => {
    const payload: PropiedadCreateRequest = {
      nombre: formValues.nombre.trim(),
      tipo: formValues.tipo,
      direccion: formValues.direccion.trim(),
      observaciones: formValues.observaciones?.trim() ?? '',
      inmobiliariaId: Number(formValues.inmobiliariaId),
    };

    try {
      if (isEdit && propiedad) {
        await propiedadService.update(propiedad.id, { ...payload, id: propiedad.id });
      } else {
        await propiedadService.create(payload);
      }
      onSuccess();
    } catch (error) {
      console.error('Error saving propiedad', error);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <form onSubmit={handleSubmit(onSubmit)}>
        <DialogTitle>{isEdit ? t('propiedades.edit') : t('propiedades.create')}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} md={6}>
              <Controller
                name="nombre"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    fullWidth
                    label={t('propiedades.fields.nombre')}
                    error={Boolean(errors.nombre)}
                    helperText={errors.nombre?.message}
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <Controller
                name="tipo"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    select
                    fullWidth
                    label={t('propiedades.fields.tipo')}
                    error={Boolean(errors.tipo)}
                    helperText={errors.tipo?.message}
                  >
                    {Object.values(PropiedadTipo).map((tipo) => (
                      <MenuItem key={tipo} value={tipo}>
                        {t(`propiedades.tipos.${tipo}`)}
                      </MenuItem>
                    ))}
                  </TextField>
                )}
              />
            </Grid>
            <Grid item xs={12}>
              <Controller
                name="direccion"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    fullWidth
                    label={t('propiedades.fields.direccion')}
                    error={Boolean(errors.direccion)}
                    helperText={errors.direccion?.message}
                  />
                )}
              />
            </Grid>
            <Grid item xs={12}>
              <Controller
                name="observaciones"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    fullWidth
                    multiline
                    minRows={3}
                    label={t('propiedades.fields.observaciones')}
                    error={Boolean(errors.observaciones)}
                    helperText={errors.observaciones?.message}
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <Controller
                name="inmobiliariaId"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    select
                    fullWidth
                    label={t('propiedades.fields.inmobiliaria')}
                    value={field.value}
                    onChange={field.onChange}
                    error={Boolean(errors.inmobiliariaId)}
                    helperText={errors.inmobiliariaId?.message}
                  >
                    <MenuItem value="">
                      {t('propiedades.fields.inmobiliaria')}
                    </MenuItem>
                    {inmobiliarias.map((inmobiliaria) => (
                      <MenuItem key={inmobiliaria.id} value={inmobiliaria.id}>
                        {inmobiliaria.nombre}
                      </MenuItem>
                    ))}
                  </TextField>
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
