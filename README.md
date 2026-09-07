# NexaFlow — Employee & Organization Management System

A full-stack RBAC-based employee management system with three roles: **OWNER**, **MANAGER**, **EMPLOYEE**.

## Stack
- **Backend:** Java 17, Spring Boot 3.3, Spring Data JPA, Spring Security, JWT (jjwt), BCrypt
- **Database:** PostgreSQL
- **Frontend:** HTML, CSS, JavaScript, Bootstrap 5 (no frameworks)

## Project structure
```
nexaflow/
├── backend/      Spring Boot project (open in IntelliJ)
├── frontend/     Static HTML/CSS/JS site
└── db/           schema.sql (reference only — Hibernate creates tables automatically)
```

---

## 1. Database setup

Unlike MySQL, PostgreSQL cannot auto-create a database from a connection string,
so you need to create it once, manually:

```bash
createdb nexaflow_db
```
or from the `psql` shell:
```sql
CREATE DATABASE nexaflow_db;
```

Once the (empty) database exists, you do NOT need to manually run `db/schema.sql` —
the backend is configured with `spring.jpa.hibernate.ddl-auto=update`, so Hibernate
creates all the tables automatically on first run.

## 2. Backend setup

1. Open the `backend/` folder in IntelliJ IDEA as a Maven project.
2. Open `backend/src/main/resources/application.properties` and update:
   ```properties
   spring.datasource.username=postgres
   spring.datasource.password=your_postgres_password
   ```
   (adjust `spring.datasource.url` too if your Postgres isn't on the default port 5432)
3. Run `NexaFlowApplication.java`.
4. On first run, the app auto-creates:
   - A default **OWNER** account (see console output):
     ```
     Email: owner@nexaflow.com
     Password: Owner@123
     ```
   - Sample demo data (Manager, 2 Employees, a Department, and a Task already assigned)
     so you can test the full app immediately without manually creating accounts:
     ```
     Manager:   manager@nexaflow.com   / Manager@123
     Employee1: employee1@nexaflow.com / Employee@123  (has 1 task assigned)
     Employee2: employee2@nexaflow.com / Employee@123
     ```
   You can change the Owner defaults in `application.properties` under `nexaflow.default-owner.*`
   **before** the first run (they only apply if no Owner exists yet). To skip demo data
   seeding (e.g., once you're past testing), set `nexaflow.seed-demo-data=false`.
5. Confirm it's running: open `http://localhost:8080` (you'll see a 404 page, which is
   expected — that just means the server is up; there's no root endpoint).

## 3. Frontend setup

The frontend is plain static HTML/CSS/JS — no build step needed.

- Easiest: open `frontend/index.html` directly in your browser, OR
- Recommended: serve it with a simple local server (avoids some browser CORS quirks):
  ```bash
  cd frontend
  python -m http.server 5500
  ```
  Then visit `http://localhost:5500`.

Log in with the default Owner account, then:
1. Create Departments (optional) via Postman/API (`POST /api/departments`) — no dedicated UI page yet, this was outside the original phase scope.
2. Create Managers (Owner → Managers page).
3. Create Employees (Owner or Manager → Employees page), optionally assigning to a manager.
4. Explore Tasks, Attendance, Leave from each role's dashboard.

## 4. Testing with Postman

Base URL: `http://localhost:8080`

1. `POST /api/auth/login` with `{ "email": "...", "password": "..." }` → returns a JWT `token`.
2. For every other request, add header: `Authorization: Bearer <token>`.

Key endpoints:
| Method | Path | Who |
|---|---|---|
| POST | /api/auth/login | Everyone |
| POST | /api/users/managers | Owner |
| GET  | /api/users/managers | Owner |
| POST | /api/users/employees | Owner, Manager |
| GET  | /api/users/employees | Owner, Manager |
| PUT  | /api/users/{id} | Self / Owner / own Manager |
| PUT  | /api/users/{id}/deactivate | Owner |
| PUT  | /api/users/{employeeId}/assign-manager/{managerId} | Owner |
| POST | /api/departments | Owner |
| GET  | /api/departments | Everyone (authenticated) |
| POST | /api/tasks/assign/{employeeId} | Owner, Manager |
| GET  | /api/tasks | Everyone (scoped by role) |
| PUT  | /api/tasks/{id}/status | Assigned employee / creator / Owner |
| POST | /api/attendance/check-in | Employee |
| POST | /api/attendance/check-out | Employee |
| GET  | /api/attendance/me | Employee |
| GET  | /api/attendance/user/{userId} | Owner, Manager (own team) |
| GET  | /api/attendance/all | Owner |
| POST | /api/leave | Employee |
| GET  | /api/leave/me | Employee |
| GET  | /api/leave/pending | Owner, Manager |
| PUT  | /api/leave/{id}/review | Owner, Manager (own team) |

## 5. Notes on design decisions
- **Single `users` table** for all 3 roles, distinguished by a `role` enum column — avoids duplicate schema for shared fields.
- **Soft delete**: employees are `deactivate`d (is_active = false), never hard-deleted, to preserve history.
- **JWT auth**: stateless, no server-side sessions — the token carries `email`, `role`, and `userId` as claims.
- **Ownership enforcement lives in the Service layer**, not just `@PreAuthorize` — so even if a Manager somehow reaches an endpoint, `assertCanAccessUser()` will still block cross-team access.
- **Audit log**: key actions (login, create/update/deactivate user, task/leave actions) are recorded in the `audit_logs` table.

## 6. Known gaps / things not yet built out
- Department management has no dedicated frontend page (API only) — quick to add if needed.
- "Change password" flow isn't wired up yet (profile page only updates name).
- No JUnit tests included yet.
- No deployment configuration (this is a local-first setup).

These map to phases in your original learning plan (Phase 14 validation refinements,
Phase 16 testing, Phase 18 deployment) — happy to build any of these out next.
