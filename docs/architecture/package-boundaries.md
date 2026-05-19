# Package Boundaries

Feature packages own their domain concepts and expose service contracts to controllers. Controllers should not inject repositories directly.

| Package | Responsibility | May depend on | Must not do | Primary entry points |
| --- | --- | --- | --- | --- |
| `common` | Shared base entities, exceptions, validation responses, flash messages | None or framework basics | Contain feature-specific rules | `BaseEntity`, exception types, `FlashMessage` |
| `config` | Application-wide Spring configuration | Framework configuration APIs | Hide feature behavior | `ClockConfig`, `JpaAuditingConfig`, `WebMvcConfig` |
| `security` | Spring Security integration and current-user abstraction | `user`, Spring Security | Put `SecurityContextHolder` reads outside the provider boundary | `SecurityConfig`, `CurrentUserProvider`, `CustomUserDetailsService` |
| `auth` | Registration and login page MVC flows | `user`, `security`, `common` | Handle `POST /login` authentication logic | `RegistrationController`, `RegistrationService`, `LoginController` |
| `user` | User account identity, role, enablement, password hash storage | `common` | Expose password hashes in DTOs or implement `UserDetails` on the entity | `UserAccountService`, `UserQueryService`, `UserRepository` |
| `category` | Category master data and active/inactive lifecycle | `common` | Hard-delete referenced category data or own question collections | `CategoryCommandService`, `CategoryQueryService` |
| `question` | Question bank, answer options, archive/restore lifecycle | `category`, `common` | Expose correctness to active play DTOs | `QuestionCommandService`, `QuestionQueryService` |
| `quiz` | Quiz definitions, published pools, attempt state view | `category`, `question`, `attempt` through contracts | Use `ManyToMany` or mutate published quizzes through draft update | `QuizCommandService`, `QuizQueryService`, `QuizAttemptStateProvider` |
| `attempt` | Attempt lifecycle, snapshots, scoring, autosave, rights, cooldown | `quiz`, `user`, `common` | Leak correctness on play pages or mutate from query services | `QuizAttemptCommandService`, `QuizAttemptQueryService`, `ScoringService` |
| `leaderboard` | Public read-only ranking model | `category`, `quiz`, JDBC | Create leaderboard persistence state or expose email addresses | `LeaderboardService`, `LeaderboardQueryRepository` |
| `admin` | Admin dashboard and result reporting read models | JDBC, feature query services | Recalculate scores or mutate reporting data | `AdminDashboardService`, `AdminResultService` |
| `tooling` | Production-jar CLI helpers | Security encoder APIs | Start full application context for hash mode | `PasswordHashCli` |

## Boundary Rules

- Command services mutate state and enforce invariants.
- Query services read state and shape DTOs/read models.
- Repositories remain behind service contracts.
- JDBC read models are used when a report needs explicit SQL shape.
- Security framework details stay in the security package.
