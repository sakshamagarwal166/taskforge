# TaskForge

Multi-tenant project management platform built with Spring Boot 3, React, and PostgreSQL.

## Tech Stack

| Layer         | Technology                                       |
|---------------|--------------------------------------------------|
| Language      | Java 17, TypeScript                              |
| Backend       | Spring Boot 3.5, Spring Security 6 (JWT)         |
| Frontend      | React 18, Vite, TailwindCSS                      |
| Persistence   | Spring Data JPA, Hibernate (multi-tenant filters)|
| Database      | PostgreSQL 16                                    |
| Migrations    | Flyway                                           |
| Infrastructure| Docker, Redis, GitHub Actions                    |

## Key Features

- **Multi-tenancy** — Row-level data isolation using Hibernate filters with a `tenant_id` discriminator. Every query is automatically scoped to the authenticated user's tenant.
- **JWT Authentication** — Stateless auth with access and refresh tokens. Login is scoped per tenant since emails are unique within a tenant, not globally.
- **Role-Based Access Control** — Four roles (OWNER, ADMIN, MEMBER, VIEWER) enforced at the API layer via Spring Security's `@PreAuthorize`.
- **Project Management** — Projects with auto-generated keys (e.g., "Bug Tracker" becomes `BT`), unique per tenant.
- **Kanban Boards** — Drag-and-drop task management with columns (To Do, In Progress, Done). Built with React and @hello-pangea/dnd.
- **Audit Logging** — Every create, update, and move operation is logged with before/after JSON snapshots, the acting user, and a timestamp.
- **API Documentation** — Interactive Swagger UI with all endpoints documented via OpenAPI 3.

## Screenshots

Screenshots coming soon.

## Architecture

```
                         ┌────────────────────────────────────────────┐
                         │              Spring Boot App               │
                         │                                            │
  Client Request         │  ┌──────────────┐    ┌──────────────────┐  │
  (Bearer JWT)    ────►  │  │  JWT Filter  │──►│  Tenant Resolver  │  │
                         │  └──────────────┘    └────────┬─────────┘  │
                         │                               │            │
                         │                      ┌────────▼─────────┐  │
                         │                      │   Controllers    │  │
                         │                      └────────┬─────────┘  │
                         │                               │            │
                         │  ┌──────────────┐    ┌────────▼─────────┐  │
                         │  │ Audit Aspect │◄──│    Services       │  │
                         │  └──────┬───────┘    └────────┬─────────┘  │
                         │         │                     │            │
                         │         │            ┌────────▼─────────┐  │
                         │         │            │  Repositories    │  │
                         │         │            └────────┬─────────┘  │
                         └─────────┼─────────────────────┼────────────┘
                                   │                     │
                                   ▼                     ▼
                             ┌───────────┐        ┌─────────────┐
                             │ audit_logs│        │ PostgreSQL  │
                             └───────────┘        └─────────────┘
```

The JWT filter authenticates the request and sets the security context. The tenant resolver reads the tenant ID from the JWT claims and sets it in a `ThreadLocal`. A Hibernate `@Filter` aspect enables row-level filtering before every repository call — no manual `WHERE tenant_id = ?` needed.

## Getting Started

### Prerequisites

- Java 17+
- Node.js 20+
- Docker & Docker Compose

### Running the Full Stack

```bash
# 1. Start PostgreSQL and Redis
docker compose up -d postgres redis

# 2. Start the backend (runs on port 8080)
cd taskforge-api
./mvnw spring-boot:run

# 3. Start the frontend (runs on port 5173)
cd taskforge-ui
npm install
npm run dev

# 4. Open the app
open http://localhost:5173
```

### Running with Docker (all services)

```bash
docker compose up --build
```

This starts PostgreSQL, Redis, the API, and the frontend. Open `http://localhost:3000`.

API docs are available at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).

### First Steps

1. **Register** a new tenant and owner account via the UI or `POST /api/auth/register`
2. **Login** to get access — the UI stores tokens automatically
3. **Create a project** from the Projects page, then manage tasks on the kanban board

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
- [x] React frontend with kanban board
- [x] GitHub Actions CI/CD
- [x] Dockerized full-stack deployment
