# TaskForge

Multi-tenant project management platform built with Spring Boot 3, React, and PostgreSQL.

## Tech Stack

| Layer         | Technology                                      |
|---------------|--------------------------------------------------|
| Language      | Java 17                                          |
| Framework     | Spring Boot 3.5, Spring Security 6 (JWT)         |
| Persistence   | Spring Data JPA, Hibernate (multi-tenant filters) |
| Database      | PostgreSQL 16                                    |
| Migrations    | Flyway                                           |
| Infrastructure| Docker, Redis                                    |

## Key Features

- **Multi-tenancy** — Row-level data isolation using Hibernate filters with a `tenant_id` discriminator. Every query is automatically scoped to the authenticated user's tenant.
- **JWT Authentication** — Stateless auth with access and refresh tokens. Login is scoped per tenant since emails are unique within a tenant, not globally.
- **Role-Based Access Control** — Four roles (OWNER, ADMIN, MEMBER, VIEWER) enforced at the API layer via Spring Security's `@PreAuthorize`.
- **Project Management** — Projects with auto-generated keys (e.g., "Bug Tracker" becomes `BT`), unique per tenant.
- **Kanban Boards** — Each project gets default columns (To Do, In Progress, Done). Tasks track column position for drag-and-drop reordering.
- **Audit Logging** — Every create, update, and move operation is logged with before/after JSON snapshots, the acting user, and a timestamp.
- **API Documentation** — Interactive Swagger UI with all endpoints documented via OpenAPI 3.

## Architecture

```
                         ┌─────────────────────────────────────────────┐
                         │              Spring Boot App                │
                         │                                             │
  Client Request         │  ┌──────────────┐    ┌──────────────────┐  │
  (Bearer JWT)    ────►  │  │  JWT Filter   │──►│  Tenant Resolver  │  │
                         │  └──────────────┘    └────────┬─────────┘  │
                         │                               │            │
                         │                      ┌────────▼─────────┐  │
                         │                      │   Controllers    │  │
                         │                      └────────┬─────────┘  │
                         │                               │            │
                         │  ┌──────────────┐    ┌────────▼─────────┐  │
                         │  │ Audit Aspect  │◄──│    Services       │  │
                         │  └──────┬───────┘    └────────┬─────────┘  │
                         │         │                     │            │
                         │         │            ┌────────▼─────────┐  │
                         │         │            │  Repositories    │  │
                         │         │            └────────┬─────────┘  │
                         └─────────┼─────────────────────┼────────────┘
                                   │                     │
                                   ▼                     ▼
                             ┌───────────┐        ┌─────────────┐
                             │ audit_logs │        │ PostgreSQL  │
                             └───────────┘        └─────────────┘
```

The JWT filter authenticates the request and sets the security context. The tenant resolver reads the tenant ID from the JWT claims and sets it in a `ThreadLocal`. A Hibernate `@Filter` aspect enables row-level filtering before every repository call — no manual `WHERE tenant_id = ?` needed.

## Getting Started

### Prerequisites

- Java 17+
- Docker & Docker Compose

### Quick Start

```bash
# Start PostgreSQL and Redis
docker compose up -d

# Run the application
cd taskforge-api
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`.

API docs are available at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).

### First Steps

1. **Register** a new tenant and owner account via `POST /api/auth/register`
2. **Login** to get an access token via `POST /api/auth/login`
3. Use the token as `Authorization: Bearer <token>` on all subsequent requests

## API Endpoints

| Method | Endpoint                              | Access          | Description                  |
|--------|---------------------------------------|-----------------|------------------------------|
| POST   | `/api/auth/register`                  | Public          | Register tenant + owner      |
| POST   | `/api/auth/login`                     | Public          | Login, get JWT tokens        |
| POST   | `/api/auth/refresh`                   | Public          | Refresh access token         |
| GET    | `/api/users`                          | Authenticated   | List tenant users            |
| POST   | `/api/users`                          | OWNER           | Invite user to tenant        |
| GET    | `/api/projects`                       | Authenticated   | List projects                |
| POST   | `/api/projects`                       | OWNER, ADMIN    | Create project               |
| GET    | `/api/projects/{id}`                  | Authenticated   | Get project details          |
| GET    | `/api/projects/{id}/tasks`            | Authenticated   | List tasks in project        |
| POST   | `/api/projects/{id}/tasks`            | OWNER-MEMBER    | Create task                  |
| PUT    | `/api/projects/{id}/tasks/{taskId}`   | OWNER-MEMBER    | Update task                  |
| PATCH  | `/api/projects/{id}/tasks/{id}/move`  | OWNER-MEMBER    | Move task to column          |
| GET    | `/api/tasks/{id}/comments`            | Authenticated   | List task comments           |
| POST   | `/api/tasks/{id}/comments`            | OWNER-MEMBER    | Add comment                  |
| GET    | `/api/audit`                          | OWNER, ADMIN    | View audit logs              |

## Roadmap

- [x] Multi-tenant data isolation
- [x] JWT authentication
- [x] Role-based access control
- [x] Audit logging with JSON snapshots
- [ ] React frontend with kanban board
- [ ] GitHub Actions CI/CD
- [ ] Dockerized full-stack deployment
