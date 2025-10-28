The AI Constitution for the User Management Service

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
    * `dto`: Data Transfer Objects. Use records for immutability. These are used for API request/response payloads to decouple the API layer from the domain model.
    * `config`: Spring configuration classes (e.g., `SecurityConfig`).
    * `exception`: Custom exception classes and global exception handlers (`@RestControllerAdvice`).
    * `security`: Security-related components like JWT utilities, user details services, etc.

## 3. Code Style and Conventions

* **Formatting:** Adhere strictly to the **Google Java Style Guide**. The `formatter-maven-plugin` is configured to enforce this on every build.
* **Naming Conventions:**
    * Classes & Records: `PascalCase` (e.g., `UserProfileController`)
    * Methods & Variables: `camelCase` (e.g., `findUserById`)
    * Constants: `UPPER_SNAKE_CASE` (e.g., `MAX_LOGIN_ATTEMPTS`)
    * REST Endpoints: Plural nouns for resources (e.g., `/users`, `/users/{userId}/profiles`)
* **Logging:**
    * Use **SLF4J** with Logback (provided by default).
    * Use the `@Slf4j` annotation from Lombok to get a logger instance.
    * Log meaningful messages. Use parameterized logging (`log.info("User {} created", userId);`) instead of string concatenation.
* **Null Handling:** Use `java.util.Optional` for return types where a value may be absent, especially in the service and repository layers. Avoid returning `null`.
* **Immutability:** Prefer immutable objects, especially for DTOs. Use Java `record` for DTOs and configuration properties (`@ConfigurationProperties`).

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
* **Lombok:** Use Lombok extensively to reduce boilerplate code (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Slf4j`).

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


