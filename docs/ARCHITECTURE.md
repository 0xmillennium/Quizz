# Architecture

Quizz is a Spring Boot MVC monolith organized by feature package under `com.quizz`.

## Package Responsibilities

- `com.quizz.auth`: login and registration MVC flow, registration validation, and delegation to user account services.
- `com.quizz.security`: Spring Security configuration, authentication success handling, `UserDetails` adapter, user-details service, and current-user lookup.
- `com.quizz.user`: `User` aggregate, role enum, user repository, account creation, user lookup, and user DTO mapping.
- `com.quizz.category`: category aggregate, admin CRUD flow, validation, service layer, mapper, and repository.
- `com.quizz.question`: question aggregate and answer options, admin question flow, validation, service layer, mapper, and repository.
- `com.quizz.quiz`: quiz aggregate and ordered quiz questions, public quiz browsing, admin quiz flow, validation, service layer, mapper, and repository.
- `com.quizz.attempt`: attempt snapshots, attempt lifecycle, scoring, result/history views, chart data, mapper, and repository.
- `com.quizz.leaderboard`: read-only leaderboard MVC flow, filter DTOs, service, and JDBC query repository.
- `com.quizz.admin`: admin dashboard and result reporting read models, services, controllers, DTOs, and JDBC query repositories.
- `com.quizz.common`: shared base entity, validation response records, flash message model, and application exceptions.
- `com.quizz.config`: MVC, auditing, and clock configuration.

## Aggregate Roots And Owned Children

Aggregate roots:

- `User`
- `Category`
- `Question`
- `Quiz`
- `QuizAttempt`

Owned child entities:

- `AnswerOption` belongs to `Question`.
- `QuizQuestion` belongs to `Quiz`.
- `AttemptQuestion` and `AttemptAnswerOption` belong to `QuizAttempt`.

`QuizAttempt` owns immutable attempt snapshots for the submitted quiz state. The snapshot records original question and answer option IDs as metadata while using scalar selected option IDs for answers.

## Read Models

- Leaderboard reads are in `com.quizz.leaderboard.repository.LeaderboardQueryRepository`.
- Admin reporting reads are in `com.quizz.admin.repository.AdminDashboardQueryRepository` and `AdminResultQueryRepository`.

These repositories use Spring JDBC for reporting queries and do not mutate domain state.

## Dependency Rules

- Controllers depend on services, mappers, validators, current-user abstractions, and MVC types, not repositories.
- Feature command/query services own repository interaction for their aggregate.
- `auth` and `security` depend on user service abstractions, not user repositories directly.
- `quiz` loads categories and questions through their services, not through their repositories.
- `attempt` loads users and quizzes through their services, and persists attempts through `QuizAttemptRepository`.
- `leaderboard` and admin reporting read from snapshots through JDBC repositories and do not call attempt command services.
- Domain entities do not depend on DTOs, controllers, services, repositories, or Spring Security.

These rules are covered by the architecture tests under `src/test/java/com/quizz/architecture`.

## Security Model

Spring Security is configured in `SecurityConfig`.

- Public routes include `/`, `/login`, `/register`, static assets, and `/error/**`.
- `/admin` and `/admin/**` require `ROLE_ADMIN`.
- Quiz, attempt, and leaderboard routes require authentication.
- CSRF remains enabled.
- Logout is a POST to `/logout`.
- `User` does not implement `UserDetails`; `CustomUserDetails` adapts the domain user for Spring Security.
- Direct `SecurityContextHolder` access is isolated behind `SecurityCurrentUserProvider`.

## Snapshot Model

Attempts copy quiz title, category identity/name, question text, answer option text, display order, and correctness into attempt tables. This preserves the result history even if admins later edit, archive, or restore quiz content.

Attempts keep:

- `originalQuestionId` on `AttemptQuestion`.
- `originalAnswerOptionId` on `AttemptAnswerOption`.
- `selectedOptionId` as a scalar on `AttemptQuestion`.

The play page uses attempt question and option snapshot IDs and text. It does not expose answer correctness.

## Scoring Model

Scoring is performed from attempt snapshots in `com.quizz.attempt.scoring`. The score records total, correct, wrong, unanswered, score percentage, and `scoring_version`. Database constraints keep attempt counts consistent.

## Leaderboard Read Query Model

The leaderboard is computed from completed attempt snapshots. There is no leaderboard table. Rankings are read-time query results ordered by score percentage, correct count, and submission time.

This keeps leaderboard data consistent with stored attempts and avoids another mutable projection table.

## Admin Reporting Read Query Model

Admin dashboard and result pages query attempt snapshots through JDBC read repositories. Reports show user full name, quiz/category snapshots, status, score counts, and stored answer snapshots. They do not expose user email and do not mutate attempts.

## Why No ManyToMany For Quiz And Question

`QuizQuestion` is an owned child of `Quiz` instead of a direct many-to-many mapping because the relationship has domain data: display order and immutability rules around published quizzes. A join entity makes the ordering explicit and keeps quiz composition behavior inside the quiz aggregate.

## Why No Leaderboard Table

The leaderboard is a read model over completed attempts. A table would introduce synchronization and invalidation concerns without adding behavior. JDBC queries over indexed attempt snapshots are enough for the current application scope.

## Why Attempts Store Snapshots

Quiz content can change after a user submits an attempt. Storing snapshots makes history, result charts, admin reporting, and leaderboard calculations durable and explainable. It also prevents published quiz immutability rules from being the only protection for historical correctness.

## Route Inventory

Public:

- `GET /`
- `GET /login`
- `POST /login`
- `GET /register`
- `POST /register`
- `POST /logout`

Authenticated user:

- `GET /quizzes`
- `GET /quizzes/{id}`
- `POST /attempts/start`
- `GET /attempts/{attemptId}`
- `POST /attempts/{attemptId}/submit`
- `GET /attempts/{attemptId}/result`
- `GET /attempts/history`
- `GET /attempts/{attemptId}/chart-data`
- `GET /leaderboard`

Admin:

- `GET /admin`
- `GET /admin/dashboard`
- `GET /admin/categories`
- `GET /admin/categories/new`
- `POST /admin/categories`
- `GET /admin/categories/{id}/edit`
- `POST /admin/categories/{id}`
- `POST /admin/categories/{id}/activate`
- `POST /admin/categories/{id}/deactivate`
- `GET /admin/questions`
- `GET /admin/questions/new`
- `POST /admin/questions`
- `GET /admin/questions/{id}/edit`
- `POST /admin/questions/{id}`
- `POST /admin/questions/{id}/delete`
- `POST /admin/questions/{id}/restore`
- `GET /admin/quizzes`
- `GET /admin/quizzes/new`
- `POST /admin/quizzes`
- `GET /admin/quizzes/{id}`
- `GET /admin/quizzes/{id}/edit`
- `POST /admin/quizzes/{id}`
- `POST /admin/quizzes/{id}/publish`
- `POST /admin/quizzes/{id}/archive`
- `GET /admin/results`
- `GET /admin/results/{attemptId}`
