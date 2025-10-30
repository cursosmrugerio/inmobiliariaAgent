import React, { useEffect, useMemo } from 'react';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  Grid,
  MenuItem,
  Switch,
  TextField,
} from '@mui/material';
import { Controller, useForm, useWatch } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { useTranslation } from 'react-i18next';

import { personaService } from '@services';
import type {
  Persona,
  PersonaCreateRequest,
  PersonaUpdateRequest,
} from '@/types';
import { PersonaTipo } from '@/types';

interface PersonaFormDialogProps {
  open: boolean;
  persona: Persona | null;
  onClose: () => void;
  onSuccess: () => void;
}

type PersonaFormValues = PersonaCreateRequest;

const buildValidationSchema = (t: ReturnType<typeof useTranslation>['t']) =>
  yup.object({
    tipoPersona: yup
      .mixed<PersonaTipo>()
      .oneOf(Object.values(PersonaTipo), t('personas.validation.tipoRequired')),
    nombre: yup
      .string()
      .nullable()
      .when('tipoPersona', {
        is: (value: PersonaTipo) =>
          value === PersonaTipo.ARRENDADOR ||
          value === PersonaTipo.ARRENDATARIO ||
          value === PersonaTipo.FIADOR,
        then: (schema) => schema.required(t('personas.validation.nombreRequired')),
      }),
    apellidos: yup
      .string()
      .nullable()
      .when('tipoPersona', {
        is: (value: PersonaTipo) =>
          value === PersonaTipo.ARRENDADOR ||
          value === PersonaTipo.ARRENDATARIO ||
          value === PersonaTipo.FIADOR,
        then: (schema) => schema.required(t('personas.validation.apellidosRequired')),
      }),
    razonSocial: yup.string().nullable(),
    rfc: yup.string().required(t('personas.validation.rfcRequired')),
    curp: yup.string().nullable(),
    email: yup.string().email(t('personas.validation.emailInvalid')).required(t('personas.validation.emailRequired')),
    telefono: yup.string().required(t('personas.validation.telefonoRequired')),
    fechaAlta: yup.string().required(),
    activo: yup.boolean(),
  });

const shouldShowCompanyFields = (tipoPersona: PersonaTipo) => tipoPersona === PersonaTipo.OTRO;

export const PersonaFormDialog: React.FC<PersonaFormDialogProps> = ({
  open,
  persona,
  onClose,
  onSuccess,
}) => {
  const { t } = useTranslation();
  const isEdit = Boolean(persona);

  const validationSchema = useMemo(() => buildValidationSchema(t), [t]);

  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<PersonaFormValues>({
    resolver: yupResolver(validationSchema),
    defaultValues: {
      tipoPersona: PersonaTipo.ARRENDADOR,
      nombre: '',
      apellidos: '',
      razonSocial: '',
      rfc: '',
      curp: '',
      email: '',
      telefono: '',
      fechaAlta: new Date().toISOString(),
      activo: true,
    },
  });

  const tipoPersonaValue = useWatch({
    control,
    name: 'tipoPersona',
  });

  useEffect(() => {
    if (persona) {
      reset({
        tipoPersona: persona.tipoPersona,
        nombre: persona.nombre ?? '',
        apellidos: persona.apellidos ?? '',
        razonSocial: persona.razonSocial ?? '',
        rfc: persona.rfc,
        curp: persona.curp ?? '',
        email: persona.email,
        telefono: persona.telefono,
        fechaAlta: persona.fechaAlta,
        activo: persona.activo,
      });
    } else {
      reset({
        tipoPersona: PersonaTipo.ARRENDADOR,
        nombre: '',
        apellidos: '',
        razonSocial: '',
        rfc: '',
        curp: '',
        email: '',
        telefono: '',
        fechaAlta: new Date().toISOString(),
        activo: true,
      });
    }
  }, [persona, reset]);

  const onSubmit = async (formValues: PersonaFormValues) => {
    const payload: PersonaCreateRequest | PersonaUpdateRequest = {
      ...formValues,
      razonSocial: shouldShowCompanyFields(formValues.tipoPersona)
        ? formValues.razonSocial ?? ''
        : '',
      curp: formValues.curp ?? '',
      fechaAlta: formValues.fechaAlta,
    };

    try {
      if (isEdit && persona) {
        await personaService.update(persona.id, { ...payload, id: persona.id });
      } else {
        await personaService.create(payload);
      }
      onSuccess();
    } catch (error) {
      console.error('Error saving persona', error);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <form onSubmit={handleSubmit(onSubmit)}>
        <DialogTitle>{isEdit ? t('personas.edit') : t('personas.create')}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} md={6}>
              <Controller
                name="tipoPersona"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    select
                    fullWidth
                    label={t('personas.fields.tipoPersona')}
                    error={Boolean(errors.tipoPersona)}
                    helperText={errors.tipoPersona?.message}
                  >
                    {Object.values(PersonaTipo).map((tipo) => (
                      <MenuItem key={tipo} value={tipo}>
                        {t(`personas.tipos.${tipo}`)}
                      </MenuItem>
                    ))}
                  </TextField>
                )}
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <Controller
                name="rfc"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    fullWidth
                    label={t('personas.fields.rfc')}
                    error={Boolean(errors.rfc)}
                    helperText={errors.rfc?.message}
                  />
                )}
              />
            </Grid>

            {shouldShowCompanyFields(tipoPersonaValue as PersonaTipo) ? (
              <Grid item xs={12}>
                <Controller
                  name="razonSocial"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      fullWidth
                      label={t('personas.fields.razonSocial')}
                    />
                  )}
                />
              </Grid>
            ) : (
              <>
                <Grid item xs={12} md={6}>
                  <Controller
                    name="nombre"
                    control={control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        fullWidth
                        label={t('personas.fields.nombre')}
                        error={Boolean(errors.nombre)}
                        helperText={errors.nombre?.message}
                      />
                    )}
                  />
                </Grid>
                <Grid item xs={12} md={6}>
                  <Controller
                    name="apellidos"
                    control={control}
                    render={({ field }) => (
                      <TextField
                        {...field}
                        fullWidth
                        label={t('personas.fields.apellidos')}
                        error={Boolean(errors.apellidos)}
                        helperText={errors.apellidos?.message}
                      />
                    )}
                  />
                </Grid>
              </>
            )}

            <Grid item xs={12} md={6}>
              <Controller
                name="curp"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    fullWidth
                    label={t('personas.fields.curp')}
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <Controller
                name="email"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    fullWidth
                    type="email"
                    label={t('personas.fields.email')}
                    error={Boolean(errors.email)}
                    helperText={errors.email?.message}
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <Controller
                name="telefono"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    fullWidth
                    label={t('personas.fields.telefono')}
                    error={Boolean(errors.telefono)}
                    helperText={errors.telefono?.message}
                  />
                )}
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <Controller
                name="fechaAlta"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    fullWidth
                    type="datetime-local"
                    label={t('personas.fields.fechaAlta')}
                    value={field.value ? field.value.slice(0, 16) : ''}
                    onChange={(event) => {
                      const value = event.target.value;
                      field.onChange(value ? new Date(value).toISOString() : '');
                    }}
                    InputLabelProps={{ shrink: true }}
                    error={Boolean(errors.fechaAlta)}
                    helperText={errors.fechaAlta?.message}
                  />
                )}
              />
            </Grid>
            <Grid item xs={12}>
              <Controller
                name="activo"
                control={control}
                render={({ field }) => (
                  <FormControlLabel
                    control={<Switch {...field} checked={field.value} />}
                    label={t('personas.fields.activo')}
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
