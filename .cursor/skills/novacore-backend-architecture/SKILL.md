---
name: novacore-backend-architecture
description: Enforces the NovaCore Spring Boot backend architecture: clean controller/service/repository/domain separation, consistent DTOs, shared error handling, and project-specific conventions. Use when editing Java code under src/main/java/com/novacore or when implementing or reviewing backend features in this repository.
---

# NovaCore Backend Architecture

## When to use this skill

Use this skill whenever:

- Working in the `novacore` repository on backend code, especially under `src/main/java/com/novacore`.
- Adding or changing REST endpoints (`controller` classes).
- Modifying business logic in `service` classes.
- Changing persistence logic in `repository` or `domain` entities.
- Reviewing whether the code structure is "clean" and aligned with project conventions.

If you are unsure, assume this skill applies to any Java/Spring Boot code in this project.

---

## Project structure overview

The backend is organized roughly as:

- `com.novacore`
  - `auth` – authentication and authorization
  - `server` – server/workspace domain (servers, invites, members, join requests, settings)
  - `user` – user profile domain
  - `shared` – cross-cutting concerns:
    - `exception`, `constants`, `response`, `context`, `util`, etc.
  - `config` – Spring configuration (security, CORS, OpenAPI, async, properties)
  - `infrastructure.persistence` – shared persistence base classes (for example, `BaseEntity`)
  - `health` – health checks

Within each bounded context such as `server`, code follows:

- `controller` – HTTP/web layer
- `service` (and optional `service.impl`) – application/service layer
- `domain` – JPA entities and domain enums
- `repository` – JPA repositories
- `dto` – input/output models for APIs
- `mapper` – mapping between entities and DTOs

Always prefer following and reusing these existing patterns instead of inventing new layouts.

---

## Core architectural principles

### Controller layer (HTTP API)

When editing or creating controllers (for example, `com.novacore.server.controller.ServerController`):

- Controllers must:
  - Be thin: no business logic, no complex conditions.
  - Accept only DTOs and primitive or path parameters.
  - Delegate to services for all non-trivial logic.
  - Set appropriate HTTP status codes and return DTOs wrapped in `ResponseEntity` or project-standard `ApiResponse` if used.

- Controllers must not:
  - Access repositories directly.
  - Construct or persist JPA entities.
  - Contain authorization logic beyond simple annotations.
  - Duplicate error-handling logic already handled in `GlobalExceptionHandler`.

When adding endpoints:

1. Define input DTOs in the appropriate `dto` package and annotate with `jakarta.validation` as needed.
2. Use `@Valid` on request bodies where validation is required.
3. Delegate to a `service` method that returns a DTO (or domain result mapped to a DTO).

### Service layer (application logic)

When editing or creating services (for example, `ServerServiceImpl`):

- Service interfaces go in `service`, implementations in `service.impl` (be consistent across the module).
- Services should:
  - Encapsulate all business rules and orchestration.
  - Use injected repositories, mappers, and utilities (such as `SecurityUtils`).
  - Throw project-specific exceptions (`BusinessException`, `ResourceNotFoundException`, and similar) with `ErrorCode`.

- Services must not:
  - Depend on `@Controller`, `ResponseEntity`, or web-only classes.
  - Depend on concrete web frameworks directly.

When implementing a service method:

1. Resolve current user or other context via injected utilities (for example, `SecurityUtils`) instead of static calls where possible.
2. Load entities via repositories and handle "not found" with `ResourceNotFoundException`.
3. Enforce all business rules (such as ownership, membership, status constraints).
4. Use mappers to convert entities to DTOs instead of building DTOs manually.
5. Keep the method focused; if it grows too large, extract private helper methods.

### Repository layer (data access)

When editing or creating repositories:

- Keep repositories as Spring Data JPA interfaces in the `repository` package.
- Allowed responsibilities:
  - Define query methods, derived queries, or annotated `@Query`.
  - Return entities or projections as appropriate.

Repositories must not:

- Contain business logic; they only fetch and persist data.
- Depend on web-related or service-level types.

### Domain layer (entities and enums)

For JPA entities (for example, `Server`):

- Place them in `domain`.
- Keep them focused on data and minimal domain behavior.
- Avoid mixing in web or persistence implementation details beyond JPA annotations.
- Be careful with Lombok:
  - Prefer `@Getter` or `@Setter` and controlled `@EqualsAndHashCode` rather than `@Data` if it risks loading lazy relations or causing recursion.

### Shared layer (errors, responses, context)

When handling errors and responses:

- Use `ErrorCode` enums and project-specific exceptions (`BusinessException`, `SystemException`, `ResourceNotFoundException`, and related types).
- Let `GlobalExceptionHandler` produce consistent API error responses using `ApiResponse` and `ApiResponseBuilder`.
- Do not re-implement ad-hoc error responses in controllers or services.

If you introduce a new domain of errors:

1. Add appropriate entries to `ErrorCode`.
2. Create a specific exception class extending the base exception type if needed.
3. Verify `GlobalExceptionHandler` handles it appropriately or add a handler method.

---

## Implementation checklist

When adding a new REST endpoint:

1. DTOs
   - [ ] Create request and response DTOs under the module’s `dto` package.
   - [ ] Add validation annotations to request DTOs where appropriate.

2. Service
   - [ ] Define a clear method in the service interface in `service`.
   - [ ] Implement it in the corresponding `service.impl` class (or equivalent, but keep consistent).
   - [ ] Use repositories and utilities via constructor injection (`@RequiredArgsConstructor`).
   - [ ] Throw appropriate exceptions with `ErrorCode` for invalid states.

3. Controller
   - [ ] Add an endpoint in the correct `controller` class.
   - [ ] Accept DTOs via `@RequestBody`, `@PathVariable`, or `@RequestParam`.
   - [ ] Delegate directly to the service.
   - [ ] Choose a suitable HTTP status and response type.

4. Error handling
   - [ ] Ensure no raw exception leaks; rely on `GlobalExceptionHandler`.
   - [ ] Map any new error condition to a proper `ErrorCode`.

5. Consistency
   - [ ] Naming aligns with existing patterns (`createX`, `updateX`, `getX`, `listX`).
   - [ ] Package and class placement mirror similar features (for example, follow existing `server` module patterns).

---

## Refactoring and review workflow

When reviewing or refactoring existing code:

1. Check layering
   - [ ] Controllers are thin and free of business logic.
   - [ ] Services encapsulate rules and orchestration.
   - [ ] Repositories are pure data access.
   - [ ] Domain objects are free from web and infrastructure concerns.

2. Check cross-cutting concerns
   - [ ] Exceptions use `ErrorCode` and shared exception types.
   - [ ] Logging uses consistent patterns (for example, via context-aware logging where available).
   - [ ] Responses are standardized through `ApiResponse` and/or `ResponseEntity`.

3. Check dependency direction
   - [ ] `controller` → `service` → `repository` or `domain` or `shared`, never reversed.
   - [ ] No direct dependency from `domain` to `controller` or `config`.

4. Check naming and structure
   - [ ] Packages roughly follow the existing domain boundaries: `auth`, `server`, `user`, and similar.
   - [ ] No unnecessary generic packages such as `utils` inside domain modules unless necessary and well-scoped.

---

## Examples

### Adding a feature in the `server` context

When adding a new operation in the `server` context:

1. DTOs: Put new request and response objects under `com.novacore.server.dto`.
2. Service: Add a method to `ServerService` and implement it in `ServerServiceImpl` (or a class under `service.impl` if that pattern is standardized).
3. Repository: If new queries are required, add them to `ServerRepository`.
4. Controller: Add an endpoint in `ServerController` that delegates to the new service method.
5. Error handling: Use `BusinessException` or `ResourceNotFoundException` with appropriate `ErrorCode`.

---

## Guidelines for using this skill

- Default to incremental improvements: keep the existing structure but move code gradually toward these conventions.
- If the existing code violates these principles but refactoring would be large or risky, do not over-refactor in one step. Instead:
  - Note the issue.
  - Propose small, safe steps that move things closer to the desired architecture.
- Always favor clarity, testability, and maintainability over cleverness or over-engineering.

