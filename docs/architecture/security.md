# Security Architecture

Quizz uses Spring Security with form login, CSRF enabled, role-based route groups, and a small current-user abstraction.

## Roles

| Role | Access |
| --- | --- |
| Anonymous | Public home redirect, login, registration, static assets, health, error pages |
| `USER` | Published quiz browsing, attempts, history, results, leaderboard |
| `ADMIN` | Admin dashboard, category/question/quiz management, admin result reporting |

## Route Groups

- `/admin` and `/admin/**` require `ROLE_ADMIN`.
- `/quizzes`, `/attempts`, and `/leaderboard` require authentication.
- `/actuator/health` is public.
- Static assets and error pages are public.

## CSRF

CSRF protection uses Spring Security defaults. MVC forms include CSRF tokens, and demo fixture scripts extract tokens from rendered forms before POSTing.

## Password Hashing

Application passwords are stored as bcrypt hashes. `PasswordHashCli` provides `hash-password` mode for admin bootstrap hash generation without starting the Spring context.

## UserDetails Bridge

The domain `User` entity does not implement Spring Security interfaces. `CustomUserDetailsService` resolves accounts through `UserQueryService` and wraps them in `CustomUserDetails`.

## Current User Boundary

`SecurityCurrentUserProvider` is the application boundary for reading `SecurityContextHolder`. Controllers and services use `CurrentUserProvider` or explicit user ids instead of static framework context reads.

## Admin Bootstrap

No default admin is created by application startup. Admin bootstrap is an explicit interactive operation that prompts for a password and upserts the configured admin account.

## Privacy Boundaries

- Password hashes are not exposed through web DTOs.
- Leaderboard and admin reporting read models expose user display names, not email addresses.
- Active play-page DTOs do not expose correct answers or original live answer option ids.
