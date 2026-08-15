# Quiz Platform — Backend

REST API for the Quiz Management & Online Assessment Platform. Java 21 + Spring Boot 4 + Spring Security (JWT) + MySQL. Deploys on **Render**.

## Features

- **Auth**: register, login, JWT, forgot/reset password, role-based authorization (ADMIN/STUDENT), rate limiting on auth endpoints
- **Admin**: user management, quiz CRUD + publish/unpublish + scheduling + negative marking, question CRUD + CSV/Excel import, category CRUD, analytics, all attempts/results, notifications
- **Student**: attempt lifecycle (start → save answer → submit) with server-side scoring, timer enforcement, question/option randomization, max-attempt limits, certificates (PDF), leaderboard, notifications
- **Security**: BCrypt passwords, stateless JWT, role checks, input validation, server-validated answers/scores/timer (frontend never trusted), CORS whitelist

## Project layout

```
src/main/java/com/quizplatform/backend/
├── config/       # Security, CORS, rate limiting, data seeder
├── controller/   # REST controllers
├── dto/          # Request/response records
├── entity/       # JPA entities (match database/schema.sql)
├── enums/        # Role, QuizStatus, Difficulty, ...
├── exception/    # Global error handling
├── repository/   # Spring Data repositories
├── security/     # JWT service + filter
└── service/      # Business logic
database/schema.sql    # Full MySQL schema (fresh DB)
database/upgrade.sql   # Extra columns for existing databases
postman_collection.json # Ready-to-import Postman API collection
```

## Setup

### 1. Database (MySQL 8)

Run `database/schema.sql` on your MySQL instance (local or Aiven). If you already created tables, run `database/upgrade.sql` instead.

### 2. Configuration

Copy `.env.example` to `.env` and fill in values (DB, JWT, admin, SMTP). The app reads these from environment variables:

| Variable | Description |
|---|---|
| `DB_URL` | JDBC URL, e.g. `jdbc:mysql://HOST:3306/quiz_platform?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `DB_USERNAME` / `DB_PASSWORD` | MySQL credentials |
| `JWT_SECRET` | Long random string (32+ chars) |
| `JWT_EXPIRATION` | Token lifetime in ms (default 7 days) |
| `FRONTEND_ORIGIN` | Allowed CORS origin(s), comma-separated |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` / `ADMIN_NAME` | Default admin seeded on first start |
| `SMTP_HOST` / `SMTP_PORT` / `SMTP_USERNAME` / `SMTP_PASSWORD` | Email (forgot-password, notifications) |
| `CERTIFICATE_DIR` | Where certificate PDFs are stored |

### 3. Run

```bash
./mvnw clean package -DskipTests
java -jar target/quiz-backend-0.0.1-SNAPSHOT.jar
```

The app needs `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` env vars. If none are set it falls back to `localhost:3306` (which fails when no local MySQL is running).

**Running from Eclipse:** create `src/main/resources/application-local.properties` (copy the template in `.env.example`, it is gitignored) with your real connection, then set **Run Configurations → Arguments → Program arguments** to:

```
--spring.profiles.active=local
```

Or set the three DB variables in **Run Configurations → Environment**.

> Always run from the built jar (or `./mvnw clean spring-boot:run`).

### Running in Eclipse

This project intentionally uses **no Lombok** — entities and DTOs are plain Java, so any IDE (Eclipse, VS Code, IntelliJ) can compile and run it without extra annotation-processing setup. Always run via the built jar or `./mvnw clean spring-boot:run`.

## API overview

| Module | Endpoints |
|---|---|
| Auth | `POST /api/auth/register`, `/login`, `/forgot-password`, `/reset-password` |
| Users (admin) | `GET /api/users/students`, `PATCH /api/users/:id/status`, `GET /api/users/:id/profile`, `DELETE /api/users/:id` |
| Categories | `GET /api/categories` (public), `POST/PUT/DELETE /api/categories` (admin) |
| Quizzes | `GET /api/quizzes?q=&categoryId=&difficulty=&duration=&sort=` (public), CRUD + `PATCH /api/quizzes/:id/publish` (admin) |
| Questions (admin) | `GET/POST /api/quizzes/:quizId/questions`, `PUT/DELETE /api/questions/:id`, `POST /api/quizzes/:quizId/questions/import` (CSV/Excel) |
| Attempts (student) | `POST /api/quizzes/:quizId/start`, `POST /api/attempts/:id/answer`, `POST /api/attempts/:id/submit`, `GET /api/attempts`, `GET /api/attempts/:id` |
| Admin results | `GET /api/admin/attempts`, `GET /api/admin/attempts/:id` |
| Analytics (admin) | `GET /api/admin/analytics` |
| Leaderboard | `GET /api/leaderboard`, `/category/:id`, `/period?period=weekly\|monthly` |
| Certificates | `GET /api/certificates/:attemptId/download` |
| Notifications | `GET /api/notifications`, `GET /api/notifications/unread-count`, `PATCH /api/notifications/:id/read`, `PATCH /api/notifications/read-all` |

Import `postman_collection.json` into Postman to test the full flow (register → login → quiz → attempt → submit → result → leaderboard).

## Deploy on Render

1. Push this folder to a GitHub repo (as its own repo).
2. Render → **New → Web Service** → connect the repo.
3. Build command: `./mvnw clean package -DskipTests`
4. Start command: `java -Xmx256m -jar target/quiz-backend-0.0.1-SNAPSHOT.jar`
5. Add the env vars from `.env.example` (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `FRONTEND_ORIGIN`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`, optionally `SMTP_*`).

`render.yaml` in the repo encodes the same configuration for Render Blueprint deploys.

> Free tier: the `-Xmx256m` flag keeps memory within the free limit.