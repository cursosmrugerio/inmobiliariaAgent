The AI Constitution for the Inmobiliaria Management System

This file serves as a persistent system prompt and contextual guide for any AI assistant working on this project. Adherence to these guidelines is crucial for maintaining code quality, consistency, and alignment with our architectural vision. This document is the single source of truth for project standards.

***

## 1. Core Project Definition

* **Project Name:** Sistema de Gestión Inmobiliaria y Arrendamientos
* **Description:** Una plataforma robusta y flexible para la administración integral de carteras inmobiliarias y contratos de arrendamiento. El sistema permite a las agencias gestionar sus propiedades, configurar conceptos de pago dinámicos (renta, servicios, mantenimiento), y automatizar las políticas de recargos por pagos tardíos. Ofrece un seguimiento detallado de los pagos y un historial completo de todos los movimientos financieros, proporcionando una solución completa para la gestión de cobranza y la administración de contratos.
* **Primary Language:** Java 25
* **Framework:** Spring Boot 3.5.7
* **Build Tool:** Apache Maven

## 2. Architectural Principles & Structure

* **Architectural Style:** Layered, Domain-Driven Design (DDD) principles are encouraged.
* **Package Structure:** Organizar el código por módulos de negocio. Todo el código relacionado con una funcionalidad específica (ej. gestión de propiedades, administración de contratos) debe residir en un único paquete principal.
    * Example: `com.inmobiliaria.gestion.propiedades`, `com.inmobiliaria.gestion.contratos`, `com.inmobiliaria.gestion.cobranza`
* **Layer Responsibilities:**
    * `controller`: Exposes RESTful endpoints. It is the entry point for API requests. Should be thin and delegate all business logic to the service layer. Handles HTTP request/response transformation and validation.
    * `service`: Contains the core business logic. Orchestrates calls to repositories and other services. Manages transactions with `@Transactional`.
    * `repository`: Data access layer. Use Spring Data JPA interfaces extending `JpaRepository`. Avoid complex queries here; use Specification or Querydsl for dynamic queries.
    * `model` or `domain`: Contains the JPA entity classes. These represent the core domain objects.
    * `dto`: Data Transfer Objects. Currently implemented as final classes with manual constructors for immutability and validation compatibility. These are used for API request/response payloads to decouple the API layer from the domain model.
    * `config`: Spring configuration classes (e.g., `SecurityConfig`, `AgentConfig`). Security-related components currently reside here.
    * `exception`: Custom exception classes and global exception handlers (`@RestControllerAdvice`).

## 3. Code Style and Conventions

* **Formatting:** Adhere strictly to the **Google Java Style Guide**. The `formatter-maven-plugin` is configured to enforce this on every build.
* **Naming Conventions:**
    * Classes & Records: `PascalCase` (e.g., `UserProfileController`)
    * Methods & Variables: `camelCase` (e.g., `findUserById`)
    * Constants: `UPPER_SNAKE_CASE` (e.g., `MAX_LOGIN_ATTEMPTS`)
    * REST Endpoints: Plural nouns for resources (e.g., `/inmobiliarias`, `/inmobiliarias/{id}`). The `/api` prefix is optional; agent endpoints use `/api/agent/*` while entity CRUD endpoints may omit it.
* **Logging:**
    * Use **SLF4J** with Logback (provided by default).
    * Initialize loggers manually: `private static final Logger log = LoggerFactory.getLogger(ClassName.class);`
    * Note: While Lombok's `@Slf4j` is available, the current codebase uses manual logger initialization for compatibility.
    * Log meaningful messages. Use parameterized logging (`log.info("User {} created", userId);`) instead of string concatenation.
* **Null Handling:** Use `java.util.Optional` internally (especially in repository operations). Service layer methods should throw domain-specific exceptions (e.g., `ResourceNotFoundException`) instead of returning `Optional<T>`, allowing the global exception handler to provide clear error messages. Avoid returning `null`.
* **Immutability:** Prefer immutable objects, especially for DTOs. While Java `record` is recommended for new code, the current codebase uses final classes with manual constructors for DTOs to maintain compatibility with validation annotations. Both patterns are acceptable.

## 4. Technology Stack & Dependency Rules

* **Java Features:** Leverage modern Java 25 features like Records, Virtual Threads (configure in `application.properties`), `switch` expressions, and text blocks where appropriate.
* **Spring Boot Starters (Priority):**
    * `spring-boot-starter-web`: For REST APIs.
    * `spring-boot-starter-data-jpa`: For database persistence with Hibernate.
    * `spring-boot-starter-security`: For authentication and authorization.
    * `spring-boot-starter-validation`: For request validation using Jakarta Bean Validation.
    * `spring-boot-starter-actuator`: For monitoring and management endpoints.
* **API Documentation:**
    * **Primary Tool:** **Springdoc OpenAPI** (`springdoc-openapi-starter-webmvc-ui`).
    * **Rule:** Every endpoint must be documented with `@Operation` and `@ApiResponse` annotations. DTOs must have clear `@Schema` annotations on their properties.
* **Testing:**
    * **Frameworks:** JUnit 5 and Mockito are the standard. Use `spring-boot-starter-test`.
    * **Rule:** New business logic in the service layer requires corresponding unit tests. New REST endpoints require integration tests using `@SpringBootTest` and `MockMvc`.
* **Database:**
    * **Development:** H2 in-memory database.
    * **Production:** PostgreSQL.
    * **Migrations:** Use **Flyway** or **Liquibase** for schema management (Flyway is preferred).
* **Lombok:** Lombok dependency is available but currently not used in the codebase. The project uses manual implementations for constructors, getters/setters, and loggers. If adding Lombok annotations in the future, ensure annotation processing is properly configured in your IDE.

## 5. AI Assistant Directives

* **When generating a new feature (e.g., a REST endpoint):**
    1.  Create a new package under `com.inmobiliaria.gestion`.
    2.  Create the DTOs (as records), Controller, Service, and Repository classes within this package.
    3.  Follow RESTful principles for the endpoint design.
    4.  Implement OpenAPI documentation for the new endpoint.
    5.  Generate a corresponding test class under `src/test/java`.
* **When modifying existing code:** First, analyze the surrounding code and this document to understand the established patterns. Your generated code must seamlessly integrate with the existing style and architecture.
* **Code Generation Priority:** Prioritize using Spring Data JPA query methods (`findBy...`) over custom `@Query` annotations. For complex queries, use the JPA Criteria API.
* **Dependency Suggestions:** Do not suggest adding new Maven dependencies unless explicitly asked. The existing stack is curated.

## 6. Conversational Agent Modules

* **Purpose:** Conversational agents provide natural language interfaces to domain operations using Google's Agent Development Kit (ADK) with Vertex AI.

* **Package Layout:** Place agent components under `com.inmobiliaria.gestion.agent`, using subpackages such as `agent.controller`, `agent.tools`, and `agent.dto`. **Keep domain entities, services, and repositories in their existing bounded context packages** (e.g., `com.inmobiliaria.gestion.inmobiliaria`). The agent layer should be thin wrappers that delegate to existing service methods.

  Example for PropertyAgent:
  ```
  com.inmobiliaria.gestion.agent.tools.PropertyTool
  com.inmobiliaria.gestion.agent.PropertyAgent
  com.inmobiliaria.gestion.agent.config.AgentConfig (shared)
  com.inmobiliaria.gestion.propiedad.service.PropertyService (existing)
  ```

* **Patterns:** Every conversational module must expose Spring `FunctionTool` wrappers around service-layer operations, register them with an `LlmAgent`, and wire the agent through an `InMemoryRunner` bean. Controllers remain thin REST adapters that delegate to the runner.

* **Dependencies:** Use Google ADK 0.3.0 (`google-adk` and `google-adk-dev` artifacts). RxJava is included transitively.

* **Model Configuration:** Agents use **Gemini 2.0 Flash** via Vertex AI. Configure in agent classes using `LlmAgent.builder().model("gemini-2.0-flash")`.

* **Documentation Alignment:** Before extending or creating CRUD agents, review:
  - `docs/README-AGENT.md` for architecture and usage patterns
  - `docs/vertex-ai.md` for Vertex AI configuration details
  - `docs/reference/README-TESTING.md` for testing workflow and best practices

* **Environment Requirements:** Ensure the following environment variables are available whenever conversational agent tests run:
  - `GOOGLE_GENAI_USE_VERTEXAI=true` (enables Vertex AI mode)
  - `GOOGLE_CLOUD_PROJECT` (your GCP project ID)
  - `GOOGLE_CLOUD_LOCATION` (e.g., `us-central1`)
  - `GOOGLE_APPLICATION_CREDENTIALS` (path to service account JSON)

  These are prerequisites for the scripts and integration tests in `scripts/test-agent_inmobiliarias.sh`.

* **Testing:** New or updated agent logic must be covered by:
  1. Shell-based regression suite (`scripts/test-agent_inmobiliarias.sh`)
  2. Unit tests under `src/test/java` (with Mockito for service mocking)
  3. Integration tests using `@SpringBootTest` and MockMvc

  Keep session-dependent flows deterministic by resetting the H2 database with the provided `TestDataController` endpoint when needed.

* **Security:**
  - Agent endpoints must follow the same authentication/authorization patterns as REST endpoints
  - Never commit `credentials.json` to version control (already in `.gitignore`)
  - Use Workload Identity or Cloud Run service accounts in production, not file-based credentials

## 7. Frontend Development Guidelines

The project includes a React + TypeScript frontend located in the `frontend/` directory within the backend project structure. This integration allows the frontend to be served as static resources by Spring Boot in production while maintaining a fast development workflow with hot reload.

### 7.1 Frontend Technology Stack

* **Framework:** React 18.2 with TypeScript 5.2
* **Build Tool:** Vite 4.4 with Hot Module Replacement (HMR)
* **UI Library:** Material-UI (MUI) 5.14
* **Form Management:** React Hook Form 7.65 + Yup 1.7
* **HTTP Client:** Axios 1.5 with interceptors for JWT authentication
* **Testing:** Playwright 1.48 for E2E tests
* **Routing:** React Router DOM 6.15

### 7.2 Development Workflow (CRITICAL)

**There are two distinct development modes. Choose the correct one for your task:**

#### Mode 1: Full Development Mode (RECOMMENDED for all frontend work)

Use this mode when developing React components, forms, UI features, or any frontend code.

**Terminal 1 - Backend:**
```bash
cd /path/to/backend
mvn spring-boot:run
```

**Terminal 2 - Frontend:**
```bash
cd /path/to/backend/frontend
npm run dev
```

**Access the application at:** `http://localhost:5173`

**Benefits:**
- ✅ Instant hot reload (HMR) - changes appear immediately
- ✅ Fast feedback loop
- ✅ TypeScript errors shown in real-time
- ✅ Vite dev server proxies `/api` calls to backend at `localhost:8080`
- ✅ Best developer experience

**How it works:**
The Vite dev server runs on port 5173 and serves the React application with HMR. All `/api/*` requests are automatically proxied to the Spring Boot backend running on port 8080. The `vite.config.ts` is configured with file watching using polling (300ms interval) to ensure changes are detected even when running processes in background or with `nohup`.

#### Mode 2: Production-like Mode (ONLY for testing final build)

Use this mode ONLY when testing the production build before deployment.

**Step 1 - Build Frontend:**
```bash
cd /path/to/backend/frontend
npm run build
```

**Step 2 - Run Backend:**
```bash
cd /path/to/backend
mvn spring-boot:run
```

**Access the application at:** `http://localhost:8080`

**Important:**
- ❌ NO hot reload
- ❌ Must run `npm run build` after EVERY frontend change
- ❌ Slow development cycle
- ✅ Use ONLY for production testing before deployment

#### Helper Script (Optional)

For convenience, use the provided script to run both servers concurrently:

```bash
cd /path/to/backend
./scripts/dev.sh
```

This script:
- Starts both backend and frontend in the background
- Shows logs from both processes
- Kills both processes when you press Ctrl+C

### 7.3 Build Configuration

The frontend build is configured to output directly to Spring Boot's static resources directory:

```typescript
// vite.config.ts
export default defineConfig({
  build: {
    outDir: '../src/main/resources/static',  // Spring Boot serves from here
    emptyOutDir: true,
  },
  server: {
    host: '0.0.0.0',
    port: 5173,
    watch: {
      usePolling: true,   // Ensures HMR works even with nohup
      interval: 300       // Poll every 300ms
    },
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
});
```

**Important Notes:**
- The `src/main/resources/static/` directory should be in `.gitignore`
- Only commit source files in `frontend/src/`, never the build output
- The Vite dev server uses polling to detect file changes, ensuring HMR works reliably

### 7.4 Frontend Architecture Patterns

* **Feature-Based Organization:** Organize code by business domain (e.g., `pages/Propiedades/`, `pages/Inmobiliarias/`) to mirror the backend's DDD structure.
* **Type Safety First:** Always define TypeScript interfaces that match backend DTOs. Use enums for fixed value sets.
* **Path Aliases:** Use configured path aliases (`@services`, `@components`, `@hooks`) instead of relative imports.
* **Service Layer:** Create domain-specific service modules in `src/services/` that wrap Axios calls to backend APIs.
* **Form Validation:** Use React Hook Form with Yup for all forms. Define validation schemas that mirror backend validation.
* **Error Handling:** Implement global error handling via Axios interceptors (401 redirects to login, etc.).

### 7.5 AI Assistant Directives for Frontend

* **When developing frontend features:**
  1. Always use Mode 1 (Full Development Mode) workflow
  2. Create TypeScript interfaces in `src/types/` that match backend DTOs
  3. Create service modules in `src/services/` for API integration
  4. Create page components in `src/pages/[Feature]/`
  5. Use Material-UI components consistently with the existing design
  6. Implement forms with React Hook Form + Yup validation
  7. Add E2E tests in `tests/` using Playwright

* **When modifying frontend code:**
  1. Read and follow the patterns in existing components
  2. Use the same styling approach (MUI `sx` prop or styled components)
  3. Maintain type safety - never use `any` type
  4. Follow the path alias conventions
  5. Test changes in the Vite dev server (localhost:5173)

* **Common pitfall to avoid:**
  - ❌ **NEVER** tell users to run `npm run build` during active development
  - ❌ **NEVER** suggest using `localhost:8080` for frontend development
  - ✅ **ALWAYS** recommend Mode 1 (Full Development Mode) for frontend work
  - ✅ **ALWAYS** recommend `localhost:5173` for development

* **File watching troubleshooting:**
  If HMR stops working:
  1. Verify `vite.config.ts` has `watch.usePolling: true` and `interval: 300`
  2. Restart the Vite dev server: `pkill -f "vite" && npm run dev`
  3. Hard refresh browser: `Cmd+Shift+R` (Mac) or `Ctrl+Shift+R` (Windows/Linux)

### 7.6 Frontend Development Resources

For comprehensive frontend development guidance, refer to:
- `DEVELOPMENT-WORKFLOW.md` - Detailed explanation of development modes and workflows
- `frontend/README.md` - Frontend-specific documentation (if exists)
- `inmobiliaria-frontend-expert` skill - Claude Code skill for React/TypeScript patterns

