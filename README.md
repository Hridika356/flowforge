# FlowForge

A project and task manager I built to learn how a full-stack app fits together end to end: a React frontend, a Spring Boot API, a PostgreSQL database, real authentication, and a production deployment.

You sign up, create projects, add tasks, and move them across a Kanban board. Each project shows how far along it is based on how many of its tasks are done.

**Live demo:** https://flowforge-89tc.vercel.app
*(It runs on free hosting, so if nobody has used it in a while, the first load can take about a minute while the server wakes up.)*

![Project board](docs/board.png)

## What it does

- **Accounts.** Register and log in. Passwords are hashed with BCrypt, and every request after login is authenticated with a JWT.
- **Projects.** Create, edit, and delete projects, give them a status, and search or filter the list.
- **Tasks.** Each task has a priority, an optional due date, and can be assigned to you.
- **Kanban board.** Drag tasks between To do, In progress, Review, and Done, or use the arrow buttons.
- **Progress.** Each project shows the percentage of its tasks that are done.
- **Dashboard.** Project and task counts, your most recent projects, and anything due in the next week (including overdue tasks).
- **Privacy between users.** You can only see and change your own projects and tasks.

![Dashboard](docs/dashboard.png)

## Tech stack

| Part | What I used |
|---|---|
| Frontend | React 19, Vite, React Router, plain CSS |
| Backend | Java 21, Spring Boot 3.5 (Web, Data JPA, Validation, Security), JJWT |
| Database | PostgreSQL 16, with Flyway for migrations |
| Tests | JUnit 5 and MockMvc on the backend, Vitest on the frontend |
| CI | GitHub Actions runs both test suites on every push |
| Hosting | Vercel (frontend), Render (backend, via Docker), Neon (database) |

## How it's put together

```
React app (Vite)
   │  fetch + "Authorization: Bearer <token>"
   ▼
Spring Boot API
   Controller  →  Service  →  Repository  →  PostgreSQL
```

Controllers only handle HTTP. The business rules live in the service layer, and Spring Data JPA repositories talk to the database. The API returns DTOs instead of JPA entities, so the database model never leaks into the JSON.

```
backend/src/main/java/com/flowforge/backend/
├── controller/   HTTP endpoints
├── service/      business logic and access checks
├── repository/   Spring Data JPA interfaces
├── model/        JPA entities and enums
├── dto/          request and response objects
├── security/     JWT creation, validation, and the auth filter
├── config/       security, CORS, and app settings
└── exception/    error types and the global error handler

frontend/src/
├── services/     one fetch wrapper plus auth, project, and task API calls
├── context/      auth state (token, current user, login and logout)
├── hooks/        a small hook for loading data
├── components/   layout, board, cards, forms, modal, badges
├── pages/        login, register, dashboard, projects, project details, profile
└── utils/        labels and date helpers
```

## Decisions I made along the way

- **All permission checks are in one class.** `ProjectAccessService` decides who can see or change a project. Right now that's just the owner. When I add shared projects, that's the only place that needs to change.
- **Someone else's project returns 404, not 403.** A 403 would tell an attacker that a project with that ID exists. A 404 doesn't give anything away.
- **Flyway owns the database schema.** The tables are created by `V1__init.sql`, and Hibernate only validates that the schema matches the code. Any schema change goes in a new migration file, so every environment ends up identical.
- **Task counts come from one grouped query.** The projects page gets the counts for all projects at once instead of loading every task.
- **Logging out happens in the browser.** JWTs are stateless, so logout just discards the token. Tokens expire after 24 hours by default.

## Running it locally

You'll need Java 21, Node 20 or newer, and Docker (or a local PostgreSQL install).

**1. Start the database**

```bash
docker compose up -d
```

**2. Start the backend**

```bash
cd backend
./mvnw spring-boot:run
```

Flyway creates the tables on the first run. To check it's up, open http://localhost:8080/api/hello. It should say "FlowForge backend is running!"

To run the backend tests (they use an in-memory database, so Docker isn't needed):

```bash
./mvnw test
```

**3. Start the frontend**

```bash
cd frontend
npm install
npm run dev
```

Then open http://localhost:5173 and create an account.

## Configuration

The backend reads its settings from environment variables. The defaults are for local development only. `backend/.env.example` lists them all.

| Variable | Local default | What it's for |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/flowforge` | Database connection |
| `DB_USERNAME` / `DB_PASSWORD` | `flowforge` / `flowforge` | Database login |
| `JWT_SECRET` | a dev-only placeholder | Key used to sign tokens (at least 32 characters) |
| `JWT_EXPIRATION_MINUTES` | `1440` | How long a login lasts |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Which frontend URLs may call the API |
| `PORT` | `8080` | Server port |

In production (`SPRING_PROFILES_ACTIVE=prod`) there are no defaults, so the app won't start unless every value is set.

The frontend needs one variable, `VITE_API_URL`, which is the backend's base URL.

## API

Everything except registering, logging in, and the health check needs an `Authorization: Bearer <token>` header.

| Method | Path | What it does |
|---|---|---|
| GET | `/api/hello` | Health check |
| POST | `/api/auth/register` | Create an account and get a token |
| POST | `/api/auth/login` | Log in and get a token |
| GET | `/api/users/me` | The current user |
| GET | `/api/dashboard` | Counts, recent projects, tasks due soon |
| GET | `/api/projects?status=&q=` | Your projects, optionally filtered or searched |
| POST | `/api/projects` | Create a project |
| GET | `/api/projects/{id}` | One project, with task counts and progress |
| PUT | `/api/projects/{id}` | Update a project |
| DELETE | `/api/projects/{id}` | Delete a project and its tasks |
| GET | `/api/projects/{id}/tasks` | A project's tasks (filter by `status` or `priority`, sort by `dueDate` or `priority`) |
| POST | `/api/projects/{id}/tasks` | Add a task |
| GET | `/api/tasks/{id}` | One task |
| PUT | `/api/tasks/{id}` | Update a task |
| PATCH | `/api/tasks/{id}/status` | Move a task to another column |
| DELETE | `/api/tasks/{id}` | Delete a task |

Errors always come back in the same shape:

```json
{ "status": 400, "message": "Project name cannot be empty", "timestamp": "2026-09-29T16:00:00",
  "fieldErrors": { "name": "Project name cannot be empty" } }
```

## Deployment

- **Frontend on Vercel.** The root directory is `frontend`, and `VITE_API_URL` points at the backend. `vercel.json` sends every path to `index.html` so page refreshes work.
- **Backend on Render.** It's built from `backend/Dockerfile`, with the `prod` profile and the variables above set in Render's dashboard.
- **Database on Neon.** A managed PostgreSQL instance. Flyway runs the migrations when the backend starts.

Each push to `main` redeploys both the frontend and the backend.

## Known limitations

- The login token is stored in `localStorage`. That keeps things simple, but a cross-site scripting bug could expose it. An httpOnly cookie with CSRF protection would be safer.
- There's no rate limiting on login yet.
- The free backend sleeps when it isn't being used, so the first request after a while is slow.

## What I'd like to add next

1. Shared projects, with members and roles (owner, admin, member, viewer)
2. Custom workflows, so each project can define its own columns
3. Comments on tasks and an activity history
4. Pagination for long lists
5. Notifications for new assignments and upcoming due dates

## License

MIT
