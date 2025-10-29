import React from 'react';
import {
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  SelectChangeEvent,
} from '@mui/material';
import { useTranslation } from 'react-i18next';

const SUPPORTED_LANGUAGES = [
  { value: 'es', labelKey: 'language.spanish' },
  { value: 'en', labelKey: 'language.english' },
];

export const LanguageSwitcher: React.FC = () => {
  const { i18n, t } = useTranslation();
  const currentLanguage = (i18n.resolvedLanguage ?? i18n.language)?.split('-')[0];

  const handleChange = (event: SelectChangeEvent<string>) => {
    const newLanguage = event.target.value;
    void i18n.changeLanguage(newLanguage);
  };

  return (
    <FormControl size="small" sx={{ minWidth: 140 }}>
      <InputLabel id="language-switcher-label">{t('language.select')}</InputLabel>
      <Select
        labelId="language-switcher-label"
        value={SUPPORTED_LANGUAGES.some((lang) => lang.value === currentLanguage)
          ? currentLanguage
          : 'es'}
        label={t('language.select')}
        onChange={handleChange}
      >
        {SUPPORTED_LANGUAGES.map((language) => (
          <MenuItem key={language.value} value={language.value}>
            {t(language.labelKey)}
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  );
};
