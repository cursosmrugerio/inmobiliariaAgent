# Frontend Implementation Plan
## Conversational AI Interface for Inmobiliaria Management System

**Document Version**: 1.0
**Last Updated**: 2025-10-28
**Target Audience**: Frontend developers implementing the React-based chat interface

---

## Table of Contents

1. [Overview](#overview)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [Backend Analysis](#backend-analysis)
5. [Implementation Phases](#implementation-phases)
6. [Component Architecture](#component-architecture)
7. [Authentication & Authorization](#authentication--authorization)
8. [Internationalization (i18n)](#internationalization-i18n)
9. [Deployment Strategy](#deployment-strategy)
10. [Security Considerations](#security-considerations)
11. [UI/UX Design](#uiux-design)
12. [Testing Strategy](#testing-strategy)
13. [Timeline & Milestones](#timeline--milestones)
14. [Success Metrics](#success-metrics)

---

## Overview

### Purpose

This document outlines the complete implementation plan for building a React-based conversational AI interface that integrates with the existing Spring Boot backend's agent endpoints. The frontend will enable users to interact with three specialized AI agents using natural language in Spanish or English.

### Key Requirements

Based on user preferences, the frontend must:

- ✅ Use **React with TypeScript** as the core framework
- ✅ Provide a **standard/polished chat UI** with professional appearance
- ✅ Deploy as **static files served by Spring Boot** (single JAR deployment)
- ✅ Support **mobile-responsive design** (works on all screen sizes)
- ✅ Implement **authentication/authorization** with JWT
- ✅ Support **multi-language** interface (Spanish and English)

### Available Agent Endpoints

The backend currently exposes three conversational agents:

| Agent | Endpoint | Purpose |
|-------|----------|---------|
| **Inmobiliaria Agent** | `POST /api/agent/chat` | Manage real estate agencies |
| **Propiedad Agent** | `POST /api/agent/propiedades/chat` | Manage properties |
| **Persona Agent** | `POST /api/agent/personas/chat` | Manage contacts/people |

---

## Technology Stack

### Core Technologies

#### Frontend Framework
- **React 18+**: Modern, component-based UI framework
- **TypeScript**: Type-safe JavaScript for better developer experience
- **Vite**: Fast build tool and development server (replacement for Create React App)

#### UI Component Library
**Recommendation**: **Material-UI (MUI) v5**

**Justification**:
- Comprehensive component library with excellent documentation
- Built-in mobile responsiveness with Grid and responsive utilities
- Professional, polished appearance out of the box
- Excellent i18n support with locale packages
- Active community and regular updates
- Spanish locale available (`@mui/material/locale/esES`)

**Alternative**: Ant Design (also excellent, slightly different design language)

#### State Management
- **React Context API + Hooks**: Sufficient for this application's complexity
- **Alternative**: Zustand or Redux Toolkit if state grows complex

#### HTTP Client
- **Axios**: Feature-rich HTTP client with interceptor support
- Interceptors for automatic JWT token injection
- Centralized error handling

#### Internationalization
- **react-i18next**: Industry-standard i18n library for React
- **i18next**: Core i18n framework
- **i18next-browser-languagedetector**: Auto-detect user's preferred language

#### Routing (Optional)
- **React Router v6**: If multi-page navigation is needed
- For a pure chat interface, routing may be optional

### Development Dependencies

```json
{
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "@mui/material": "^5.14.0",
    "@mui/icons-material": "^5.14.0",
    "@emotion/react": "^11.11.0",
    "@emotion/styled": "^11.11.0",
    "axios": "^1.5.0",
    "react-router-dom": "^6.15.0",
    "react-i18next": "^13.2.0",
    "i18next": "^23.5.0",
    "i18next-browser-languagedetector": "^7.1.0"
  },
  "devDependencies": {
    "@types/react": "^18.2.0",
    "@types/react-dom": "^18.2.0",
    "@types/node": "^20.5.0",
    "@vitejs/plugin-react": "^4.0.0",
    "typescript": "^5.2.0",
    "vite": "^4.4.0"
  }
}
```

---

## Project Structure

### Recommended Directory Layout

```
backend/
├── src/main/
│   ├── java/
│   │   └── com/inmobiliaria/gestion/
│   │       ├── agent/                    # Existing agent code
│   │       ├── auth/                     # New: Authentication endpoints
│   │       │   ├── controller/
│   │       │   │   └── AuthController.java
│   │       │   ├── dto/
│   │       │   │   ├── LoginRequest.java
│   │       │   │   ├── LoginResponse.java
│   │       │   │   └── UserResponse.java
│   │       │   └── service/
│   │       │       └── AuthService.java
│   │       └── ...
│   └── resources/
│       ├── application.properties        # Update: Add static resource config
│       └── static/                       # Frontend build output (generated)
│           ├── index.html
│           ├── assets/
│           │   ├── index-[hash].js
│           │   └── index-[hash].css
│           └── ...
├── frontend/                             # NEW: Frontend source code
│   ├── package.json
│   ├── tsconfig.json
│   ├── vite.config.ts
│   ├── index.html
│   ├── public/
│   │   └── favicon.ico
│   └── src/
│       ├── main.tsx                      # Entry point
│       ├── App.tsx                       # Root component
│       ├── vite-env.d.ts                 # Vite type definitions
│       │
│       ├── i18n/                         # Internationalization
│       │   ├── config.ts                 # i18next configuration
│       │   └── locales/
│       │       ├── en.json               # English translations
│       │       └── es.json               # Spanish translations
│       │
│       ├── contexts/                     # React Context providers
│       │   ├── AuthContext.tsx           # Authentication state
│       │   └── ThemeContext.tsx          # MUI theme customization
│       │
│       ├── components/                   # Reusable UI components
│       │   ├── Chat/
│       │   │   ├── ChatContainer.tsx     # Main chat interface
│       │   │   ├── MessageList.tsx       # Scrollable message history
│       │   │   ├── MessageBubble.tsx     # Individual message display
│       │   │   ├── MessageInput.tsx      # Text input + send button
│       │   │   ├── AgentSelector.tsx     # Dropdown to switch agents
│       │   │   ├── SessionInfo.tsx       # Display session ID
│       │   │   └── TypingIndicator.tsx   # Loading state
│       │   ├── Auth/
│       │   │   ├── LoginForm.tsx         # Login page
│       │   │   └── PrivateRoute.tsx      # Protected route wrapper
│       │   └── Layout/
│       │       ├── Header.tsx            # App header with lang switcher
│       │       ├── Sidebar.tsx           # Optional: navigation sidebar
│       │       └── Footer.tsx            # App footer
│       │
│       ├── services/                     # API integration layer
│       │   ├── api.ts                    # Axios instance configuration
│       │   ├── agentService.ts           # Agent API methods
│       │   └── authService.ts            # Auth API methods
│       │
│       ├── types/                        # TypeScript type definitions
│       │   ├── chat.types.ts             # ChatRequest, ChatResponse, Message
│       │   ├── auth.types.ts             # User, LoginRequest, LoginResponse
│       │   └── agent.types.ts            # AgentType enum
│       │
│       ├── hooks/                        # Custom React hooks
│       │   ├── useChat.ts                # Chat logic and state management
│       │   ├── useAuth.ts                # Authentication logic
│       │   └── useLocalStorage.ts        # localStorage helper hook
│       │
│       └── utils/                        # Utility functions
│           ├── storage.ts                # localStorage/sessionStorage helpers
│           ├── constants.ts              # API URLs, agent configs
│           └── formatters.ts             # Date/time formatting
│
└── docs/
    ├── FRONTEND-IMPLEMENTATION-PLAN.md   # This document
    └── ...
```

---

## Backend Analysis

### Current Agent Architecture

The backend uses Google's Agent Development Kit (ADK) with Gemini 2.0 Flash for conversational AI.

#### Request Format

All agent endpoints accept the same request structure:

```typescript
interface ChatRequest {
  message: string;      // Natural language query (required)
  sessionId?: string;   // Optional session ID for context
}
```

**Example**:
```json
{
  "message": "Muéstrame todas las agencias inmobiliarias",
  "sessionId": "user-abc123"
}
```

#### Response Format

```typescript
interface ChatResponse {
  response: string;     // Natural language response from agent
  sessionId: string;    // Session ID (generated if not provided)
  success: boolean;     // Operation success indicator
  error?: string;       // Error message if success is false
}
```

**Success Example**:
```json
{
  "response": "Encontré 3 agencias registradas:\n1. Inmobiliaria XYZ (activa)\n2. Propiedades ABC (activa)\n3. Casas del Valle (inactiva)",
  "sessionId": "user-abc123",
  "success": true,
  "error": null
}
```

**Error Example**:
```json
{
  "response": "",
  "sessionId": "user-abc123",
  "success": false,
  "error": "No se pudo conectar con el servicio de agentes"
}
```

### Session Management

- **In-Memory Sessions**: Google ADK's `InMemoryRunner` manages sessions
- **Session Resolution**:
  - If `sessionId` is provided and exists → Reuse session (maintains context)
  - If `sessionId` is provided but doesn't exist → Create new session with that ID
  - If `sessionId` is null/blank → Auto-generate: `"user-{UUID}"`
- **Multi-turn Conversations**: Sessions preserve conversation history
- **Best Practice**: Store `sessionId` from first response and include in subsequent requests

### Current Security Configuration

⚠️ **Important**: Currently, all endpoints are **publicly accessible** (no authentication required).

```java
// SecurityConfig.java
.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
```

**For Production**: Must implement authentication and update this configuration.

### OpenAPI Documentation

- **Swagger UI**: Available at `http://localhost:8080/swagger-ui.html`
- All agent endpoints are documented with `@Operation` and `@ApiResponse` annotations
- DTOs include `@Schema` annotations for field descriptions

---

## Implementation Phases

### Phase 1: Backend Integration & Authentication (1 day)

#### Objectives
- Configure Spring Boot to serve static frontend files
- Implement JWT-based authentication endpoints
- Update security configuration for protected routes

#### Tasks

**1.1 Configure Static Resource Serving**

Update `src/main/resources/application.properties`:

```properties
# Static resource configuration
spring.web.resources.static-locations=classpath:/static/
spring.web.resources.add-mappings=true

# Optional: Cache control for production
spring.web.resources.cache.cachecontrol.max-age=365d
spring.web.resources.chain.strategy.content.enabled=true
```

**1.2 Create Authentication Endpoints**

Create new package: `com.inmobiliaria.gestion.auth`

**AuthController.java**:
```java
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication endpoints")
public class AuthController {

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // Implementation
    }

    @PostMapping("/register")
    @Operation(summary = "User registration")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Implementation
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User user) {
        // Implementation
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout")
    public ResponseEntity<Void> logout() {
        // Implementation (optional, frontend can just discard token)
    }
}
```

**LoginRequest.java**:
```java
public final class LoginRequest {
    @NotBlank
    @Email
    private final String email;

    @NotBlank
    @Size(min = 6)
    private final String password;

    @JsonCreator
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters
}
```

**LoginResponse.java**:
```java
public final class LoginResponse {
    private final String token;
    private final String tokenType = "Bearer";
    private final UserResponse user;

    // Constructor, getters
}
```

**1.3 Update SecurityConfig**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable()) // Disabled for JWT
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // Protected agent endpoints
                .requestMatchers("/api/agent/**").authenticated()
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:5173", // Vite dev server
            "http://localhost:8080"  // Production
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }
}
```

**1.4 Implement JWT Utilities**

Create `JwtUtil.java` for token generation/validation and `JwtAuthenticationFilter.java` for request interception.

#### Deliverables
- ✅ Authentication endpoints functional
- ✅ JWT token generation and validation working
- ✅ Security configuration updated
- ✅ Static resource serving configured
- ✅ CORS configured for development

---

### Phase 2: Frontend Project Setup (1 day)

#### Objectives
- Initialize React + TypeScript + Vite project
- Install and configure all dependencies
- Set up build configuration for Spring Boot integration
- Configure development proxy

#### Tasks

**2.1 Initialize Vite Project**

```bash
cd backend
npm create vite@latest frontend -- --template react-ts
cd frontend
```

**2.2 Install Dependencies**

```bash
# UI Framework
npm install @mui/material @emotion/react @emotion/styled @mui/icons-material

# HTTP Client
npm install axios

# Routing
npm install react-router-dom

# Internationalization
npm install react-i18next i18next i18next-browser-languagedetector

# Type Definitions
npm install -D @types/react-router-dom @types/node
```

**2.3 Configure Vite for Spring Boot Integration**

**vite.config.ts**:
```typescript
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],

  // Build configuration
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
    sourcemap: false, // Set to true for debugging production issues
  },

  // Development server configuration
  server: {
    port: 5173,
    proxy: {
      // Proxy API requests to Spring Boot during development
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },

  // Path aliases
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
      '@components': path.resolve(__dirname, './src/components'),
      '@services': path.resolve(__dirname, './src/services'),
      '@types': path.resolve(__dirname, './src/types'),
      '@hooks': path.resolve(__dirname, './src/hooks'),
      '@utils': path.resolve(__dirname, './src/utils'),
    },
  },
});
```

**2.4 Configure TypeScript**

**tsconfig.json**:
```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,

    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx",

    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,

    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"],
      "@components/*": ["./src/components/*"],
      "@services/*": ["./src/services/*"],
      "@types/*": ["./src/types/*"],
      "@hooks/*": ["./src/hooks/*"],
      "@utils/*": ["./src/utils/*"]
    }
  },
  "include": ["src"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

**2.5 Update package.json Scripts**

```json
{
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview",
    "lint": "eslint . --ext ts,tsx --report-unused-disable-directives --max-warnings 0",
    "type-check": "tsc --noEmit"
  }
}
```

#### Deliverables
- ✅ Vite project initialized with React + TypeScript
- ✅ All dependencies installed
- ✅ Build configured to output to Spring Boot static folder
- ✅ Development proxy configured
- ✅ TypeScript strict mode enabled
- ✅ Path aliases configured

---

### Phase 3: Core Chat UI (2 days)

#### Objectives
- Build core chat interface components
- Implement chat logic with session management
- Integrate with agent API endpoints
- Ensure mobile-responsive design

#### Tasks

**3.1 Create Type Definitions**

**src/types/chat.types.ts**:
```typescript
export enum AgentType {
  INMOBILIARIA = 'inmobiliaria',
  PROPIEDAD = 'propiedad',
  PERSONA = 'persona',
}

export interface ChatRequest {
  message: string;
  sessionId?: string;
}

export interface ChatResponse {
  response: string;
  sessionId: string;
  success: boolean;
  error?: string;
}

export interface Message {
  id: string;
  content: string;
  sender: 'user' | 'agent';
  timestamp: Date;
  success?: boolean;
  error?: string;
}

export interface AgentConfig {
  type: AgentType;
  name: string;
  description: string;
  endpoint: string;
  icon: string;
}
```

**3.2 Create API Service**

**src/services/api.ts**:
```typescript
import axios, { AxiosInstance, AxiosError } from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

export const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor: Add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('auth_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor: Handle errors
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      // Unauthorized: clear token and redirect to login
      localStorage.removeItem('auth_token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

**src/services/agentService.ts**:
```typescript
import api from './api';
import { AgentType, ChatRequest, ChatResponse } from '@types/chat.types';

const AGENT_ENDPOINTS: Record<AgentType, string> = {
  [AgentType.INMOBILIARIA]: '/agent/chat',
  [AgentType.PROPIEDAD]: '/agent/propiedades/chat',
  [AgentType.PERSONA]: '/agent/personas/chat',
};

export const agentService = {
  sendMessage: async (
    agentType: AgentType,
    request: ChatRequest
  ): Promise<ChatResponse> => {
    const endpoint = AGENT_ENDPOINTS[agentType];
    const response = await api.post<ChatResponse>(endpoint, request);
    return response.data;
  },
};

export default agentService;
```

**3.3 Create Custom Chat Hook**

**src/hooks/useChat.ts**:
```typescript
import { useState, useCallback } from 'react';
import { AgentType, Message, ChatRequest } from '@types/chat.types';
import { agentService } from '@services/agentService';
import { v4 as uuidv4 } from 'uuid';

export const useChat = (initialAgent: AgentType) => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [currentAgent, setCurrentAgent] = useState<AgentType>(initialAgent);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const sendMessage = useCallback(
    async (content: string) => {
      if (!content.trim()) return;

      // Add user message
      const userMessage: Message = {
        id: uuidv4(),
        content: content.trim(),
        sender: 'user',
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, userMessage]);
      setIsLoading(true);
      setError(null);

      try {
        const request: ChatRequest = {
          message: content,
          sessionId: sessionId || undefined,
        };

        const response = await agentService.sendMessage(currentAgent, request);

        // Update session ID if this is first message
        if (!sessionId && response.sessionId) {
          setSessionId(response.sessionId);
        }

        // Add agent response
        const agentMessage: Message = {
          id: uuidv4(),
          content: response.response,
          sender: 'agent',
          timestamp: new Date(),
          success: response.success,
          error: response.error,
        };
        setMessages((prev) => [...prev, agentMessage]);

        if (!response.success) {
          setError(response.error || 'Unknown error occurred');
        }
      } catch (err: any) {
        const errorMessage = err.response?.data?.message || err.message || 'Network error';
        setError(errorMessage);

        // Add error message to chat
        const errorMsg: Message = {
          id: uuidv4(),
          content: errorMessage,
          sender: 'agent',
          timestamp: new Date(),
          success: false,
          error: errorMessage,
        };
        setMessages((prev) => [...prev, errorMsg]);
      } finally {
        setIsLoading(false);
      }
    },
    [currentAgent, sessionId]
  );

  const clearConversation = useCallback(() => {
    setMessages([]);
    setSessionId(null);
    setError(null);
  }, []);

  const changeAgent = useCallback((newAgent: AgentType) => {
    setCurrentAgent(newAgent);
    clearConversation();
  }, [clearConversation]);

  return {
    messages,
    sessionId,
    currentAgent,
    isLoading,
    error,
    sendMessage,
    clearConversation,
    changeAgent,
  };
};
```

**3.4 Build Chat Components**

**src/components/Chat/MessageBubble.tsx**:
```typescript
import React from 'react';
import { Box, Paper, Typography, Alert } from '@mui/material';
import { Message } from '@types/chat.types';
import { formatTime } from '@utils/formatters';

interface MessageBubbleProps {
  message: Message;
}

export const MessageBubble: React.FC<MessageBubbleProps> = ({ message }) => {
  const isUser = message.sender === 'user';
  const isError = message.success === false;

  return (
    <Box
      sx={{
        display: 'flex',
        justifyContent: isUser ? 'flex-end' : 'flex-start',
        mb: 2,
      }}
    >
      <Box sx={{ maxWidth: { xs: '85%', sm: '70%' } }}>
        {isError ? (
          <Alert severity="error">{message.content}</Alert>
        ) : (
          <Paper
            elevation={1}
            sx={{
              p: 2,
              bgcolor: isUser ? 'primary.main' : 'grey.100',
              color: isUser ? 'primary.contrastText' : 'text.primary',
              borderRadius: 2,
            }}
          >
            <Typography variant="body1" sx={{ whiteSpace: 'pre-wrap' }}>
              {message.content}
            </Typography>
            <Typography
              variant="caption"
              sx={{
                display: 'block',
                mt: 0.5,
                opacity: 0.7,
                textAlign: 'right',
              }}
            >
              {formatTime(message.timestamp)}
            </Typography>
          </Paper>
        )}
      </Box>
    </Box>
  );
};
```

**src/components/Chat/MessageInput.tsx**:
```typescript
import React, { useState, KeyboardEvent } from 'react';
import { Box, TextField, IconButton, Paper } from '@mui/material';
import SendIcon from '@mui/icons-material/Send';
import { useTranslation } from 'react-i18next';

interface MessageInputProps {
  onSendMessage: (message: string) => void;
  disabled?: boolean;
}

export const MessageInput: React.FC<MessageInputProps> = ({
  onSendMessage,
  disabled = false,
}) => {
  const { t } = useTranslation();
  const [message, setMessage] = useState('');

  const handleSend = () => {
    if (message.trim() && !disabled) {
      onSendMessage(message);
      setMessage('');
    }
  };

  const handleKeyPress = (e: KeyboardEvent<HTMLDivElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <Paper elevation={3} sx={{ p: 2 }}>
      <Box sx={{ display: 'flex', gap: 1, alignItems: 'flex-end' }}>
        <TextField
          fullWidth
          multiline
          maxRows={4}
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder={t('chat.placeholder')}
          disabled={disabled}
          variant="outlined"
          size="small"
        />
        <IconButton
          color="primary"
          onClick={handleSend}
          disabled={!message.trim() || disabled}
        >
          <SendIcon />
        </IconButton>
      </Box>
    </Paper>
  );
};
```

**src/components/Chat/ChatContainer.tsx**:
```typescript
import React, { useEffect, useRef } from 'react';
import { Box, Container, Paper, Typography, Chip } from '@mui/material';
import { MessageBubble } from './MessageBubble';
import { MessageInput } from './MessageInput';
import { AgentSelector } from './AgentSelector';
import { TypingIndicator } from './TypingIndicator';
import { useChat } from '@hooks/useChat';
import { AgentType } from '@types/chat.types';
import { useTranslation } from 'react-i18next';

export const ChatContainer: React.FC = () => {
  const { t } = useTranslation();
  const {
    messages,
    sessionId,
    currentAgent,
    isLoading,
    sendMessage,
    clearConversation,
    changeAgent,
  } = useChat(AgentType.INMOBILIARIA);

  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Auto-scroll to bottom on new messages
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  return (
    <Container maxWidth="md" sx={{ height: '100vh', display: 'flex', flexDirection: 'column', py: 2 }}>
      {/* Header */}
      <Paper elevation={2} sx={{ p: 2, mb: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
          <Typography variant="h5">{t('chat.title')}</Typography>
          {sessionId && (
            <Chip label={`Session: ${sessionId.substring(0, 8)}...`} size="small" />
          )}
        </Box>
        <AgentSelector
          currentAgent={currentAgent}
          onChangeAgent={changeAgent}
          onClearConversation={clearConversation}
        />
      </Paper>

      {/* Messages Area */}
      <Paper
        elevation={1}
        sx={{
          flex: 1,
          overflow: 'auto',
          p: 2,
          mb: 2,
          bgcolor: 'grey.50',
        }}
      >
        {messages.length === 0 ? (
          <Box sx={{ textAlign: 'center', py: 4 }}>
            <Typography color="text.secondary">
              {t('chat.emptyState')}
            </Typography>
          </Box>
        ) : (
          messages.map((msg) => <MessageBubble key={msg.id} message={msg} />)
        )}
        {isLoading && <TypingIndicator />}
        <div ref={messagesEndRef} />
      </Paper>

      {/* Input Area */}
      <MessageInput onSendMessage={sendMessage} disabled={isLoading} />
    </Container>
  );
};
```

#### Deliverables
- ✅ Core chat UI components built
- ✅ Custom `useChat` hook for state management
- ✅ API service layer with Axios integration
- ✅ Message display with user/agent differentiation
- ✅ Session management working
- ✅ Mobile-responsive design
- ✅ Auto-scroll to latest message

---

### Phase 4: Authentication & Authorization (1-2 days)

#### Objectives
- Implement login/logout functionality
- Create AuthContext for global auth state
- Implement PrivateRoute wrapper for protected pages
- Store JWT tokens securely

#### Tasks

**4.1 Create Auth Types**

**src/types/auth.types.ts**:
```typescript
export interface User {
  id: number;
  email: string;
  name: string;
  roles: string[];
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  tokenType: string;
  user: User;
}
```

**4.2 Create Auth Service**

**src/services/authService.ts**:
```typescript
import api from './api';
import { LoginRequest, LoginResponse, User } from '@types/auth.types';

export const authService = {
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/auth/login', credentials);

    // Store token and user
    localStorage.setItem('auth_token', response.data.token);
    localStorage.setItem('user', JSON.stringify(response.data.user));

    return response.data;
  },

  logout: () => {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('user');
  },

  getCurrentUser: (): User | null => {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },

  getToken: (): string | null => {
    return localStorage.getItem('auth_token');
  },

  isAuthenticated: (): boolean => {
    return !!authService.getToken();
  },
};

export default authService;
```

**4.3 Create AuthContext**

**src/contexts/AuthContext.tsx**:
```typescript
import React, { createContext, useState, useContext, useEffect, ReactNode } from 'react';
import { User } from '@types/auth.types';
import { authService } from '@services/authService';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Check for existing auth on mount
    const currentUser = authService.getCurrentUser();
    setUser(currentUser);
    setIsLoading(false);
  }, []);

  const login = async (email: string, password: string) => {
    const response = await authService.login({ email, password });
    setUser(response.user);
  };

  const logout = () => {
    authService.logout();
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        login,
        logout,
        isLoading,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};
```

**4.4 Create Login Component**

**src/components/Auth/LoginForm.tsx**:
```typescript
import React, { useState } from 'react';
import {
  Box,
  Paper,
  TextField,
  Button,
  Typography,
  Alert,
  Container,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@contexts/AuthContext';
import { useTranslation } from 'react-i18next';

export const LoginForm: React.FC = () => {
  const { t } = useTranslation();
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      await login(email, password);
      navigate('/chat');
    } catch (err: any) {
      setError(err.response?.data?.message || t('auth.loginError'));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Container maxWidth="xs">
      <Box
        sx={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <Paper elevation={3} sx={{ p: 4, width: '100%' }}>
          <Typography variant="h4" gutterBottom textAlign="center">
            {t('auth.login')}
          </Typography>

          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <form onSubmit={handleSubmit}>
            <TextField
              fullWidth
              label={t('auth.email')}
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              margin="normal"
              required
            />
            <TextField
              fullWidth
              label={t('auth.password')}
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              margin="normal"
              required
            />
            <Button
              fullWidth
              type="submit"
              variant="contained"
              size="large"
              disabled={isLoading}
              sx={{ mt: 3 }}
            >
              {isLoading ? t('auth.loggingIn') : t('auth.login')}
            </Button>
          </form>
        </Paper>
      </Box>
    </Container>
  );
};
```

**4.5 Create PrivateRoute Wrapper**

**src/components/Auth/PrivateRoute.tsx**:
```typescript
import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '@contexts/AuthContext';
import { CircularProgress, Box } from '@mui/material';

interface PrivateRouteProps {
  children: React.ReactNode;
}

export const PrivateRoute: React.FC<PrivateRouteProps> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: '100vh',
        }}
      >
        <CircularProgress />
      </Box>
    );
  }

  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
};
```

**4.6 Update App.tsx with Routing**

**src/App.tsx**:
```typescript
import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material';
import { AuthProvider } from '@contexts/AuthContext';
import { LoginForm } from '@components/Auth/LoginForm';
import { ChatContainer } from '@components/Chat/ChatContainer';
import { PrivateRoute } from '@components/Auth/PrivateRoute';

const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<LoginForm />} />
            <Route
              path="/chat"
              element={
                <PrivateRoute>
                  <ChatContainer />
                </PrivateRoute>
              }
            />
            <Route path="/" element={<Navigate to="/chat" replace />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
```

#### Deliverables
- ✅ Login form with email/password
- ✅ AuthContext managing global auth state
- ✅ JWT token storage in localStorage
- ✅ Axios interceptor adding Authorization header
- ✅ PrivateRoute protecting chat interface
- ✅ Auto-redirect to login on 401 responses
- ✅ Logout functionality

---

### Phase 5: Internationalization (1 day)

#### Objectives
- Configure i18next for Spanish and English
- Create translation files
- Add language switcher to header
- Translate all UI strings

#### Tasks

**5.1 Configure i18next**

**src/i18n/config.ts**:
```typescript
import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';
import en from './locales/en.json';
import es from './locales/es.json';

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources: {
      en: { translation: en },
      es: { translation: es },
    },
    fallbackLng: 'es',
    defaultNS: 'translation',
    interpolation: {
      escapeValue: false,
    },
    detection: {
      order: ['localStorage', 'navigator'],
      caches: ['localStorage'],
    },
  });

export default i18n;
```

**5.2 Create Translation Files**

**src/i18n/locales/es.json**:
```json
{
  "app": {
    "title": "Sistema Inmobiliario",
    "subtitle": "Asistente Conversacional con IA"
  },
  "chat": {
    "title": "Asistente Inmobiliario",
    "placeholder": "Escribe tu mensaje aquí...",
    "send": "Enviar",
    "newConversation": "Nueva Conversación",
    "emptyState": "Inicia una conversación con el asistente",
    "typing": "El asistente está escribiendo...",
    "sessionLabel": "Sesión"
  },
  "agents": {
    "inmobiliaria": "Gestión de Agencias",
    "inmobiliariaDesc": "Administrar inmobiliarias y agencias",
    "propiedad": "Gestión de Propiedades",
    "propiedadDesc": "Administrar propiedades inmobiliarias",
    "persona": "Gestión de Contactos",
    "personaDesc": "Administrar clientes y contactos",
    "selectAgent": "Seleccionar Agente"
  },
  "auth": {
    "login": "Iniciar Sesión",
    "logout": "Cerrar Sesión",
    "email": "Correo Electrónico",
    "password": "Contraseña",
    "loggingIn": "Iniciando sesión...",
    "loginError": "Error al iniciar sesión. Verifica tus credenciales.",
    "welcome": "Bienvenido"
  },
  "errors": {
    "networkError": "Error de conexión. Por favor, intenta de nuevo.",
    "unauthorized": "No autorizado. Por favor, inicia sesión.",
    "serverError": "Error del servidor. Por favor, intenta más tarde."
  },
  "language": {
    "spanish": "Español",
    "english": "English",
    "select": "Idioma"
  }
}
```

**src/i18n/locales/en.json**:
```json
{
  "app": {
    "title": "Real Estate System",
    "subtitle": "Conversational AI Assistant"
  },
  "chat": {
    "title": "Real Estate Assistant",
    "placeholder": "Type your message here...",
    "send": "Send",
    "newConversation": "New Conversation",
    "emptyState": "Start a conversation with the assistant",
    "typing": "Assistant is typing...",
    "sessionLabel": "Session"
  },
  "agents": {
    "inmobiliaria": "Agency Management",
    "inmobiliariaDesc": "Manage real estate agencies",
    "propiedad": "Property Management",
    "propiedadDesc": "Manage real estate properties",
    "persona": "Contact Management",
    "personaDesc": "Manage clients and contacts",
    "selectAgent": "Select Agent"
  },
  "auth": {
    "login": "Log In",
    "logout": "Log Out",
    "email": "Email Address",
    "password": "Password",
    "loggingIn": "Logging in...",
    "loginError": "Login failed. Please check your credentials.",
    "welcome": "Welcome"
  },
  "errors": {
    "networkError": "Connection error. Please try again.",
    "unauthorized": "Unauthorized. Please log in.",
    "serverError": "Server error. Please try later."
  },
  "language": {
    "spanish": "Español",
    "english": "English",
    "select": "Language"
  }
}
```

**5.3 Create Language Switcher Component**

**src/components/Layout/LanguageSwitcher.tsx**:
```typescript
import React from 'react';
import { Select, MenuItem, FormControl, InputLabel } from '@mui/material';
import { useTranslation } from 'react-i18next';

export const LanguageSwitcher: React.FC = () => {
  const { i18n, t } = useTranslation();

  const handleChange = (event: any) => {
    i18n.changeLanguage(event.target.value);
  };

  return (
    <FormControl size="small" sx={{ minWidth: 120 }}>
      <InputLabel>{t('language.select')}</InputLabel>
      <Select value={i18n.language} onChange={handleChange} label={t('language.select')}>
        <MenuItem value="es">{t('language.spanish')}</MenuItem>
        <MenuItem value="en">{t('language.english')}</MenuItem>
      </Select>
    </FormControl>
  );
};
```

**5.4 Create Header with Language Switcher**

**src/components/Layout/Header.tsx**:
```typescript
import React from 'react';
import { AppBar, Toolbar, Typography, Button, Box } from '@mui/material';
import LogoutIcon from '@mui/icons-material/Logout';
import { useAuth } from '@contexts/AuthContext';
import { LanguageSwitcher } from './LanguageSwitcher';
import { useTranslation } from 'react-i18next';

export const Header: React.FC = () => {
  const { user, logout, isAuthenticated } = useAuth();
  const { t } = useTranslation();

  return (
    <AppBar position="static">
      <Toolbar>
        <Typography variant="h6" sx={{ flexGrow: 1 }}>
          {t('app.title')}
        </Typography>

        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          <LanguageSwitcher />

          {isAuthenticated && (
            <>
              <Typography variant="body2">
                {t('auth.welcome')}, {user?.name}
              </Typography>
              <Button
                color="inherit"
                startIcon={<LogoutIcon />}
                onClick={logout}
              >
                {t('auth.logout')}
              </Button>
            </>
          )}
        </Box>
      </Toolbar>
    </AppBar>
  );
};
```

**5.5 Initialize i18n in main.tsx**

**src/main.tsx**:
```typescript
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './i18n/config'; // Initialize i18next

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
```

#### Deliverables
- ✅ i18next configured with Spanish and English
- ✅ Translation files created for all UI strings
- ✅ Language switcher in header
- ✅ Language preference persisted to localStorage
- ✅ All components using `useTranslation` hook
- ✅ Automatic language detection from browser

---

### Phase 6: Polish, Testing & Optimization (1 day)

#### Objectives
- Add loading states and error handling
- Implement UX improvements
- Manual testing across devices
- Performance optimization

#### Tasks

**6.1 Add Typing Indicator**

**src/components/Chat/TypingIndicator.tsx**:
```typescript
import React from 'react';
import { Box, Typography } from '@mui/material';
import { useTranslation } from 'react-i18next';

export const TypingIndicator: React.FC = () => {
  const { t } = useTranslation();

  return (
    <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
      <Box
        sx={{
          display: 'flex',
          gap: 0.5,
          bgcolor: 'grey.100',
          p: 2,
          borderRadius: 2,
        }}
      >
        <Box
          className="typing-dot"
          sx={{
            width: 8,
            height: 8,
            borderRadius: '50%',
            bgcolor: 'grey.500',
            animation: 'typing 1.4s infinite',
            animationDelay: '0s',
          }}
        />
        <Box
          className="typing-dot"
          sx={{
            width: 8,
            height: 8,
            borderRadius: '50%',
            bgcolor: 'grey.500',
            animation: 'typing 1.4s infinite',
            animationDelay: '0.2s',
          }}
        />
        <Box
          className="typing-dot"
          sx={{
            width: 8,
            height: 8,
            borderRadius: '50%',
            bgcolor: 'grey.500',
            animation: 'typing 1.4s infinite',
            animationDelay: '0.4s',
          }}
        />
      </Box>
      <Typography variant="caption" sx={{ ml: 1, color: 'text.secondary' }}>
        {t('chat.typing')}
      </Typography>
    </Box>
  );
};
```

Add animation to global CSS:
```css
@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-10px);
  }
}
```

**6.2 Add Error Boundary**

**src/components/ErrorBoundary.tsx**:
```typescript
import React, { Component, ReactNode } from 'react';
import { Box, Typography, Button, Container } from '@mui/material';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error?: Error;
}

export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('Error caught by boundary:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <Container>
          <Box
            sx={{
              minHeight: '100vh',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              textAlign: 'center',
            }}
          >
            <Typography variant="h4" gutterBottom>
              Oops! Algo salió mal
            </Typography>
            <Typography color="text.secondary" paragraph>
              {this.state.error?.message}
            </Typography>
            <Button
              variant="contained"
              onClick={() => window.location.reload()}
            >
              Recargar Aplicación
            </Button>
          </Box>
        </Container>
      );
    }

    return this.props.children;
  }
}
```

**6.3 Add Utility Functions**

**src/utils/formatters.ts**:
```typescript
export const formatTime = (date: Date): string => {
  return new Intl.DateTimeFormat('default', {
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
};

export const formatDate = (date: Date): string => {
  return new Intl.DateTimeFormat('default', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  }).format(date);
};
```

**src/utils/storage.ts**:
```typescript
export const storage = {
  get: <T>(key: string): T | null => {
    const item = localStorage.getItem(key);
    return item ? JSON.parse(item) : null;
  },

  set: <T>(key: string, value: T): void => {
    localStorage.setItem(key, JSON.stringify(value));
  },

  remove: (key: string): void => {
    localStorage.removeItem(key);
  },

  clear: (): void => {
    localStorage.clear();
  },
};
```

**6.4 Testing Checklist**

Manual testing across:
- ✅ Desktop browsers (Chrome, Firefox, Safari)
- ✅ Mobile devices (iOS Safari, Android Chrome)
- ✅ Tablet sizes
- ✅ All three agent types
- ✅ Login/logout flow
- ✅ Session persistence across page refresh
- ✅ Language switching
- ✅ Error scenarios (network failure, invalid credentials)
- ✅ Multi-turn conversations

**6.5 Performance Optimization**

- Lazy load routes with React.lazy()
- Code splitting for better initial load time
- Optimize bundle size (analyze with `vite-plugin-bundle-analyzer`)
- Add service worker for offline capability (optional)

**src/App.tsx** (with lazy loading):
```typescript
import React, { Suspense, lazy } from 'react';
import { CircularProgress, Box } from '@mui/material';

const LoginForm = lazy(() => import('@components/Auth/LoginForm'));
const ChatContainer = lazy(() => import('@components/Chat/ChatContainer'));

const LoadingFallback = () => (
  <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
    <CircularProgress />
  </Box>
);

// In Routes:
<Route
  path="/login"
  element={
    <Suspense fallback={<LoadingFallback />}>
      <LoginForm />
    </Suspense>
  }
/>
```

#### Deliverables
- ✅ Typing indicator during agent responses
- ✅ Error boundary for graceful error handling
- ✅ Utility functions for formatting
- ✅ Comprehensive manual testing completed
- ✅ Performance optimizations applied
- ✅ Production build tested

---

## Component Architecture

### Component Hierarchy

```
App
├── ErrorBoundary
├── ThemeProvider (MUI)
├── AuthProvider (Context)
├── BrowserRouter
    ├── Header
    │   ├── LanguageSwitcher
    │   └── UserMenu (logout)
    └── Routes
        ├── /login → LoginForm
        └── /chat → PrivateRoute → ChatContainer
            ├── AgentSelector
            ├── SessionInfo
            ├── MessageList
            │   └── MessageBubble (×N)
            ├── TypingIndicator
            └── MessageInput
```

### State Management Strategy

**Global State (React Context)**:
- Authentication state (`AuthContext`)
- Theme configuration (MUI `ThemeProvider`)
- Language preference (i18next)

**Local Component State (useState/useReducer)**:
- Chat messages
- Current agent selection
- Session ID
- Loading states
- Form inputs

**Server State**:
- No additional library needed (simple use case)
- If complexity grows, consider React Query or SWR

---

## Authentication & Authorization

### JWT Token Flow

1. **Login**:
   - User submits credentials → `POST /api/auth/login`
   - Backend validates → Returns JWT token + user info
   - Frontend stores token in `localStorage`
   - Axios interceptor adds token to all requests

2. **Protected Requests**:
   - Every API request includes `Authorization: Bearer <token>` header
   - Backend validates token
   - If invalid/expired → Return 401
   - Frontend intercepts 401 → Redirect to login

3. **Logout**:
   - Remove token from `localStorage`
   - Redirect to login page

### Security Best Practices

**Token Storage**:
- **localStorage** (current approach): Simple, works across tabs
- **Alternative**: `httpOnly` cookies (more secure, requires backend support)

**Token Refresh** (future enhancement):
- Implement refresh token mechanism
- Auto-refresh before expiration
- Silent token renewal in background

**CSRF Protection**:
- Not needed with JWT in Authorization header
- Only needed if using cookie-based auth

---

## Internationalization (i18n)

### Supported Languages

- **Spanish (es)**: Primary language (default)
- **English (en)**: Secondary language

### Language Detection Order

1. User's explicit selection (stored in `localStorage`)
2. Browser's preferred language (`navigator.language`)
3. Fallback to Spanish

### Adding New Translations

1. Add new key-value pairs to `src/i18n/locales/es.json` and `en.json`
2. Use in components: `const { t } = useTranslation(); t('your.key')`
3. For pluralization: Use i18next plural forms
4. For interpolation: `t('welcome', { name: user.name })`

### MUI Component Localization

Add MUI locale to `ThemeProvider`:

```typescript
import { esES, enUS } from '@mui/material/locale';
import { useTranslation } from 'react-i18next';

const theme = createTheme(
  {
    palette: { /* ... */ },
  },
  i18n.language === 'es' ? esES : enUS
);
```

---

## Deployment Strategy

### Development Workflow

```bash
# Terminal 1: Start Spring Boot backend
cd backend
mvn spring-boot:run
# Backend runs on http://localhost:8080

# Terminal 2: Start Vite dev server
cd frontend
npm run dev
# Frontend runs on http://localhost:5173
# API requests proxied to localhost:8080
```

### Production Build Process

**Step 1: Build Frontend**
```bash
cd frontend
npm run build
# Output: backend/src/main/resources/static/
```

**Step 2: Build Spring Boot JAR**
```bash
cd backend
mvn clean package -DskipTests
# Output: target/inmobiliaria-0.0.1-SNAPSHOT.jar
```

**Step 3: Run Production JAR**
```bash
java -jar target/inmobiliaria-0.0.1-SNAPSHOT.jar
# Application runs on http://localhost:8080
# Frontend served from /
# API available at /api/*
```

### Deployment to Cloud

**Google Cloud Platform (Recommended)**:

1. **Cloud Run** (Serverless):
   ```bash
   # Create Dockerfile
   FROM eclipse-temurin:25-jdk-alpine
   COPY target/*.jar app.jar
   EXPOSE 8080
   ENTRYPOINT ["java", "-jar", "/app.jar"]

   # Deploy
   gcloud run deploy inmobiliaria-app \
     --source . \
     --region us-central1 \
     --allow-unauthenticated
   ```

2. **App Engine**:
   ```yaml
   # app.yaml
   runtime: java17
   instance_class: F2
   env_variables:
     GOOGLE_APPLICATION_CREDENTIALS: /path/to/credentials.json
   ```

3. **Compute Engine** (VM):
   - Install Java 25
   - Copy JAR file
   - Run with systemd service

### CI/CD Pipeline (GitHub Actions Example)

```yaml
name: Build and Deploy

on:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 25
        uses: actions/setup-java@v3
        with:
          java-version: '25'

      - name: Set up Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'

      - name: Build Frontend
        run: |
          cd frontend
          npm ci
          npm run build

      - name: Build Backend
        run: |
          cd backend
          mvn clean package -DskipTests

      - name: Deploy to Cloud Run
        run: |
          gcloud run deploy inmobiliaria-app \
            --image gcr.io/${{ secrets.GCP_PROJECT }}/inmobiliaria:${{ github.sha }}
```

---

## Security Considerations

### Frontend Security

1. **XSS Protection**:
   - React's default escaping prevents XSS
   - Avoid `dangerouslySetInnerHTML`
   - Sanitize any HTML from agent responses (if needed)

2. **Dependency Security**:
   - Regularly run `npm audit`
   - Keep dependencies updated
   - Use Dependabot for automated updates

3. **Sensitive Data**:
   - Never store passwords in frontend
   - Clear tokens on logout
   - Use HTTPS in production

4. **API Keys**:
   - Never commit `.env` files
   - Use environment variables for config
   - Example `.env.production`:
     ```
     VITE_API_BASE_URL=https://api.inmobiliaria.com
     ```

### Backend Security

1. **JWT Token Security**:
   - Use strong secret key (256-bit minimum)
   - Set reasonable expiration time (1 hour)
   - Implement refresh tokens
   - Validate token on every request

2. **CORS Configuration**:
   - Only allow specific origins in production
   - Never use `allowedOrigins("*")` with `allowCredentials(true)`

3. **Rate Limiting**:
   - Protect agent endpoints from abuse
   - Implement per-user rate limits
   - Example: 100 requests per hour per user

4. **Input Validation**:
   - Already handled by Jakarta Bean Validation
   - Additional validation in service layer
   - Sanitize agent inputs (ADK handles this)

### Environment-Specific Configuration

**Development** (`application-dev.properties`):
```properties
spring.security.enabled=false
cors.allowed-origins=http://localhost:5173
logging.level.root=DEBUG
```

**Production** (`application-prod.properties`):
```properties
spring.security.enabled=true
cors.allowed-origins=https://inmobiliaria.com
logging.level.root=WARN
jwt.secret=${JWT_SECRET}
```

---

## UI/UX Design

### Design Principles

1. **Mobile-First**: Design for smallest screen, enhance for larger
2. **Accessibility**: WCAG 2.1 AA compliance
3. **Consistency**: Follow Material Design guidelines
4. **Clarity**: Clear visual hierarchy and typography
5. **Feedback**: Immediate response to user actions

### Color Palette

```typescript
const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',      // Blue
      light: '#42a5f5',
      dark: '#1565c0',
    },
    secondary: {
      main: '#dc004e',      // Pink/Red
      light: '#e33371',
      dark: '#9a0036',
    },
    success: {
      main: '#4caf50',
    },
    error: {
      main: '#f44336',
    },
    background: {
      default: '#fafafa',
      paper: '#ffffff',
    },
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    h5: {
      fontWeight: 600,
    },
  },
  shape: {
    borderRadius: 8,
  },
});
```

### Responsive Breakpoints

- **xs**: 0-600px (Mobile)
- **sm**: 600-900px (Tablet portrait)
- **md**: 900-1200px (Tablet landscape)
- **lg**: 1200-1536px (Desktop)
- **xl**: 1536px+ (Large desktop)

### Component Spacing

- Use MUI's `sx` prop for spacing
- Follow 8px grid system
- Consistent padding/margin values

### Accessibility Features

- **Keyboard Navigation**: All interactive elements accessible via Tab
- **ARIA Labels**: Descriptive labels for screen readers
- **Color Contrast**: Minimum 4.5:1 ratio for text
- **Focus Indicators**: Clear focus states for all inputs
- **Semantic HTML**: Proper heading hierarchy, landmarks

---

## Testing Strategy

### Frontend Testing

**Unit Tests** (Optional, for Phase 2):
- Jest + React Testing Library
- Test custom hooks (`useChat`, `useAuth`)
- Test utility functions

**Integration Tests**:
- Test complete user flows
- Test API integration with mock server (MSW)

**E2E Tests** (Optional):
- Playwright or Cypress
- Test critical paths: login → chat → logout

### Backend Testing

Already implemented:
- Unit tests (Service layer with Mockito)
- Integration tests (Controller with MockMvc)
- Regression scripts (Shell-based)

### Manual Testing Checklist

**Authentication**:
- [ ] Login with valid credentials
- [ ] Login with invalid credentials
- [ ] Logout
- [ ] Protected route redirect
- [ ] Token persistence across refresh

**Chat Functionality**:
- [ ] Send message to Inmobiliaria agent
- [ ] Send message to Propiedad agent
- [ ] Send message to Persona agent
- [ ] Multi-turn conversation (session maintained)
- [ ] New conversation (session reset)
- [ ] Error handling (network failure)
- [ ] Empty message validation

**UI/UX**:
- [ ] Responsive design (mobile, tablet, desktop)
- [ ] Language switcher (Spanish ↔ English)
- [ ] Loading states visible
- [ ] Error messages clear
- [ ] Auto-scroll to bottom
- [ ] Typing indicator appears

**Production Build**:
- [ ] Build completes without errors
- [ ] JAR runs successfully
- [ ] Frontend served correctly
- [ ] API endpoints accessible
- [ ] No console errors

---

## Timeline & Milestones

| Phase | Duration | Milestone | Deliverable |
|-------|----------|-----------|-------------|
| **1. Backend Integration** | 1 day | Authentication ready | JWT endpoints, SecurityConfig updated |
| **2. Frontend Setup** | 1 day | Project initialized | React + Vite + MUI configured |
| **3. Core Chat UI** | 2 days | Chat functional | Working chat with all 3 agents |
| **4. Authentication** | 1-2 days | Login/logout working | Protected routes, token management |
| **5. Internationalization** | 1 day | Multi-language support | Spanish/English translations |
| **6. Polish & Testing** | 1 day | Production ready | All features tested and optimized |
| **Total** | **7-8 days** | **MVP Complete** | **Deployable application** |

### Milestone Checklist

**Milestone 1: Backend Integration Complete**
- [ ] Authentication endpoints implemented
- [ ] JWT generation/validation working
- [ ] Security configuration updated
- [ ] Static resource serving configured
- [ ] CORS configured

**Milestone 2: Frontend Setup Complete**
- [ ] Vite project initialized
- [ ] All dependencies installed
- [ ] Build configuration complete
- [ ] Development proxy working
- [ ] TypeScript configured

**Milestone 3: Core Chat UI Complete**
- [ ] MessageBubble component
- [ ] MessageInput component
- [ ] ChatContainer component
- [ ] AgentSelector component
- [ ] API service layer
- [ ] useChat custom hook
- [ ] Mobile responsive

**Milestone 4: Authentication Complete**
- [ ] LoginForm component
- [ ] AuthContext implemented
- [ ] PrivateRoute wrapper
- [ ] Token storage
- [ ] Axios interceptors
- [ ] Logout functionality

**Milestone 5: i18n Complete**
- [ ] i18next configured
- [ ] Translation files (es/en)
- [ ] LanguageSwitcher component
- [ ] All strings translated
- [ ] Language persistence

**Milestone 6: Production Ready**
- [ ] All manual tests passed
- [ ] Error handling robust
- [ ] Loading states implemented
- [ ] Performance optimized
- [ ] Production build tested
- [ ] Documentation updated

---

## Success Metrics

### Functional Requirements

- ✅ **Authentication**: Users can log in and access protected routes
- ✅ **Multi-Agent Support**: Users can switch between 3 agents
- ✅ **Session Management**: Conversations maintain context
- ✅ **Multi-language**: Spanish and English fully supported
- ✅ **Mobile Responsive**: Works on all screen sizes (320px+)
- ✅ **Error Handling**: Graceful error messages for all failure cases
- ✅ **Single JAR Deployment**: Frontend embedded in Spring Boot

### Performance Metrics

- **Initial Load Time**: < 3 seconds on 3G
- **Time to Interactive**: < 5 seconds
- **Bundle Size**: < 500KB (gzipped)
- **Agent Response Time**: < 2 seconds (depends on Gemini)
- **Lighthouse Score**: > 90 (Performance, Accessibility)

### User Experience Metrics

- **Login Success Rate**: > 95%
- **Message Send Success Rate**: > 98%
- **Mobile Usability**: No horizontal scrolling, touch targets > 48px
- **Language Switch Latency**: Instant (< 100ms)

### Testing Coverage

- **Backend Unit Tests**: > 80% coverage (already achieved)
- **Backend Integration Tests**: All endpoints covered (already achieved)
- **Frontend Manual Tests**: All critical paths verified
- **Cross-browser Compatibility**: Chrome, Firefox, Safari, Edge

---

## Appendix

### Useful Commands

```bash
# Frontend Development
cd frontend
npm run dev              # Start dev server
npm run build            # Production build
npm run preview          # Preview production build
npm run lint             # Run ESLint
npm run type-check       # TypeScript type checking

# Backend Development
cd backend
mvn spring-boot:run      # Start Spring Boot
mvn clean package        # Build JAR
mvn test                 # Run tests

# Production Deployment
cd frontend && npm run build && cd ..
mvn clean package
java -jar target/inmobiliaria-*.jar

# Docker (Optional)
docker build -t inmobiliaria-app .
docker run -p 8080:8080 inmobiliaria-app
```

### Environment Variables

**Frontend (.env.development)**:
```bash
VITE_API_BASE_URL=http://localhost:8080/api
```

**Frontend (.env.production)**:
```bash
VITE_API_BASE_URL=/api
```

**Backend**:
```bash
GOOGLE_APPLICATION_CREDENTIALS=/path/to/credentials.json
GOOGLE_GENAI_USE_VERTEXAI=true
GOOGLE_CLOUD_PROJECT=inmobiliaria-adk
GOOGLE_CLOUD_LOCATION=us-central1
JWT_SECRET=your-secret-key-here
```

### Additional Resources

- **React Documentation**: https://react.dev
- **Material-UI Documentation**: https://mui.com
- **Vite Documentation**: https://vitejs.dev
- **i18next Documentation**: https://www.i18next.com
- **Axios Documentation**: https://axios-http.com
- **TypeScript Documentation**: https://www.typescriptlang.org

### Troubleshooting

**Issue: Vite dev server not proxying API requests**
- Solution: Check `vite.config.ts` proxy configuration
- Ensure Spring Boot is running on port 8080

**Issue: CORS errors in production**
- Solution: Update `SecurityConfig` allowed origins
- Ensure frontend and backend are on same domain

**Issue: Authentication token not persisted**
- Solution: Check browser localStorage
- Verify Axios interceptor is adding token

**Issue: Language not switching**
- Solution: Check i18next configuration
- Verify translation keys exist in both locales

**Issue: Production build fails**
- Solution: Run `npm run type-check` to find TypeScript errors
- Check console for build errors

---

## Next Steps

After completing this implementation:

1. **Analytics Integration**: Add Google Analytics or similar
2. **Conversation History**: Store past conversations in database
3. **Voice Input**: Implement Web Speech API
4. **File Uploads**: Support document/image uploads for properties
5. **Admin Dashboard**: Monitor agent usage, view metrics
6. **WebSocket Support**: Real-time streaming responses
7. **PWA Features**: Offline support, push notifications
8. **Advanced Security**: 2FA, session timeout, audit logs

---

**Document End**

For questions or clarifications, refer to:
- Backend architecture: `CLAUDE.md`
- Agent implementation: `docs/README-AGENT.md`
- Testing procedures: `docs/reference/README-TESTING.md`
