import React from 'react';
import {
  Box,
  Button,
  ToggleButton,
  ToggleButtonGroup,
  Typography,
} from '@mui/material';
import BusinessIcon from '@mui/icons-material/Business';
import HomeWorkIcon from '@mui/icons-material/HomeWork';
import PersonIcon from '@mui/icons-material/Person';
import { useTranslation } from 'react-i18next';
import { AgentConfig, AgentType } from '@/types/chat.types';

interface AgentSelectorProps {
  currentAgent: AgentType;
  onChangeAgent: (agent: AgentType) => void;
  onClearConversation: () => void;
}

const ICON_MAP: Record<string, React.ElementType> = {
  business: BusinessIcon,
  homework: HomeWorkIcon,
  person: PersonIcon,
};

const resolveIconComponent = (key: string): React.ElementType => {
  const normalizedKey = key.toLowerCase();
  return ICON_MAP[normalizedKey] ?? BusinessIcon;
};

export const AgentSelector: React.FC<AgentSelectorProps> = ({
  currentAgent,
  onChangeAgent,
  onClearConversation,
}) => {
  const { t } = useTranslation();

  const agents: Array<AgentConfig & { Icon: React.ElementType }> = [
    {
      type: AgentType.INMOBILIARIA,
      name: t('agents.inmobiliaria.name'),
      description: t('agents.inmobiliaria.description'),
      endpoint: '/agent/chat',
      icon: 'Business',
      Icon: resolveIconComponent('business'),
    },
    {
      type: AgentType.PROPIEDAD,
      name: t('agents.propiedad.name'),
      description: t('agents.propiedad.description'),
      endpoint: '/agent/propiedades/chat',
      icon: 'HomeWork',
      Icon: resolveIconComponent('homework'),
    },
    {
      type: AgentType.PERSONA,
      name: t('agents.persona.name'),
      description: t('agents.persona.description'),
      endpoint: '/agent/personas/chat',
      icon: 'Person',
      Icon: resolveIconComponent('person'),
    },
  ];

  const handleAgentChange = (
    _: React.MouseEvent<HTMLElement>,
    value: AgentType | null,
  ) => {
    if (value && value !== currentAgent) {
      onChangeAgent(value);
    }
  };

  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: { xs: 'column', sm: 'row' },
        gap: 1,
        alignItems: { xs: 'stretch', sm: 'center' },
      }}
    >
      <ToggleButtonGroup
        value={currentAgent}
        exclusive
        onChange={handleAgentChange}
        color="primary"
        sx={{
          flexWrap: 'wrap',
          flexGrow: 1,
          '& .MuiToggleButton-root': {
            textTransform: 'none',
            px: 2,
            py: 1.5,
            borderRadius: 2,
          },
        }}
      >
        {agents.map(({ type, name, description, Icon }) => (
          <ToggleButton key={type} value={type}>
            <Box sx={{ textAlign: 'left' }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Icon fontSize="small" />
                <Typography variant="subtitle2">{name}</Typography>
              </Box>
              <Typography variant="caption" color="text.secondary">
                {description}
              </Typography>
            </Box>
          </ToggleButton>
        ))}
      </ToggleButtonGroup>

      <Button variant="outlined" color="secondary" onClick={onClearConversation}>
        {t('chat.clearConversation')}
      </Button>
    </Box>
  );
};
