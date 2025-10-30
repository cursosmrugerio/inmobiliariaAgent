import React, { useEffect, useMemo } from 'react';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Grid,
  TextField,
} from '@mui/material';
import { Controller, useForm } from 'react-hook-form';
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

const buildValidationSchema = (t: ReturnType<typeof useTranslation>['t']) =>
  yup.object({
    nombre: yup.string().required(t('inmobiliarias.validation.nombreRequired')),
    rfc: yup.string().required(t('inmobiliarias.validation.rfcRequired')),
    nombreContacto: yup.string().required(t('inmobiliarias.validation.nombreRequired')),
    correo: yup
      .string()
      .email(t('inmobiliarias.validation.correoInvalid'))
      .required(t('inmobiliarias.validation.correoRequired')),
    telefono: yup.string().required(t('inmobiliarias.validation.telefonoRequired')),
  });

export const InmobiliariaFormDialog: React.FC<InmobiliariaFormDialogProps> = ({
  open,
  inmobiliaria,
  onClose,
  onSuccess,
}) => {
  const { t } = useTranslation();
  const isEdit = Boolean(inmobiliaria);

  const validationSchema = useMemo(() => buildValidationSchema(t), [t]);

  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<InmobiliariaCreateRequest>({
    resolver: yupResolver(validationSchema),
    defaultValues: {
      nombre: '',
      rfc: '',
      nombreContacto: '',
      correo: '',
      telefono: '',
    },
  });

  useEffect(() => {
    if (inmobiliaria) {
      reset({
        nombre: inmobiliaria.nombre,
        rfc: inmobiliaria.rfc,
        nombreContacto: inmobiliaria.nombreContacto,
        correo: inmobiliaria.correo,
        telefono: inmobiliaria.telefono,
      });
    } else {
      reset({
        nombre: '',
        rfc: '',
        nombreContacto: '',
        correo: '',
        telefono: '',
      });
    }
  }, [inmobiliaria, reset]);

  const onSubmit = async (data: InmobiliariaCreateRequest) => {
    const payload: InmobiliariaCreateRequest = {
      nombre: data.nombre.trim(),
      rfc: data.rfc.trim(),
      nombreContacto: data.nombreContacto.trim(),
      correo: data.correo.trim(),
      telefono: data.telefono.trim(),
    };
    try {
      if (isEdit && inmobiliaria) {
        await inmobiliariaService.update(inmobiliaria.id, { ...payload, id: inmobiliaria.id });
      } else {
        await inmobiliariaService.create(payload);
      }
      onSuccess();
    } catch (error) {
      console.error('Error saving inmobiliaria', error);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <form onSubmit={handleSubmit(onSubmit)}>
        <DialogTitle>{isEdit ? t('inmobiliarias.edit') : t('inmobiliarias.create')}</DialogTitle>
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
                    error={Boolean(errors.nombre)}
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
                    error={Boolean(errors.rfc)}
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
                    error={Boolean(errors.nombreContacto)}
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
                    error={Boolean(errors.correo)}
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
                    error={Boolean(errors.telefono)}
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
