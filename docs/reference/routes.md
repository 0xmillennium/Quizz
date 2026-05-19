# Routes

CSRF is enabled. POST routes require a CSRF token unless Spring Security itself handles the authentication processing endpoint.

## Public

| Route pattern | Method | Role | CSRF | Controller | Purpose |
| --- | --- | --- | --- | --- | --- |
| `/` | GET | Anonymous | No | `WebMvcConfig` | Redirect to `/quizzes`. |
| `/login` | GET | Anonymous | No | `LoginController` | Render login page. |
| `/login` | POST | Anonymous | Yes | Spring Security | Process form login. |
| `/register` | GET | Anonymous | No | `RegistrationController` | Render registration form. |
| `/register` | POST | Anonymous | Yes | `RegistrationController` | Register a user account. |
| `/css/**`, `/js/**`, `/images/**`, `/favicon.ico` | GET | Anonymous | No | Static resources | Serve static assets. |
| `/error/**` | GET | Anonymous | No | Error handling | Render error views. |

## Authenticated User

| Route pattern | Method | Role | CSRF | Controller | Purpose |
| --- | --- | --- | --- | --- | --- |
| `/quizzes` | GET | USER or ADMIN | No | `QuizController` | List published quizzes. |
| `/quizzes/{id}` | GET | USER or ADMIN | No | `QuizController` | Show quiz detail and attempt state. |
| `/attempts/start` | POST | USER or ADMIN | Yes | `QuizAttemptController` | Start or resume a quiz attempt. |
| `/attempts/{attemptId}/restart` | POST | USER or ADMIN | Yes | `QuizAttemptController` | Restart an active attempt. |
| `/attempts/{attemptId}` | GET | USER or ADMIN | No | `QuizAttemptController` | Render active play page. |
| `/attempts/{attemptId}/submit` | POST | USER or ADMIN | Yes | `QuizAttemptController` | Submit attempt. |
| `/attempts/{attemptId}/questions/{attemptQuestionId}/answer` | POST | USER or ADMIN | Yes | `QuizAttemptController` | Autosave one answer. |
| `/attempts/{attemptId}/auto-submit` | POST | USER or ADMIN | Yes | `QuizAttemptController` | Auto-submit overdue attempt. |
| `/attempts/{attemptId}/result` | GET | USER or ADMIN | No | `QuizAttemptController` | Render completed attempt result. |
| `/attempts/history` | GET | USER or ADMIN | No | `QuizAttemptController` | Render user attempt history. |
| `/attempts/{attemptId}/chart-data` | GET | USER or ADMIN | No | `QuizAttemptController` | Return result chart data. |
| `/leaderboard` | GET | USER or ADMIN | No | `LeaderboardController` | Render public leaderboard. |

## Admin

| Route pattern | Method | Role | CSRF | Controller | Purpose |
| --- | --- | --- | --- | --- | --- |
| `/admin`, `/admin/dashboard` | GET | ADMIN | No | `AdminDashboardController` | Render admin dashboard. |
| `/admin/categories` | GET | ADMIN | No | `CategoryAdminController` | List categories. |
| `/admin/categories/new` | GET | ADMIN | No | `CategoryAdminController` | Render category creation form. |
| `/admin/categories` | POST | ADMIN | Yes | `CategoryAdminController` | Create category. |
| `/admin/categories/{id}/edit` | GET | ADMIN | No | `CategoryAdminController` | Render category edit form. |
| `/admin/categories/{id}` | POST | ADMIN | Yes | `CategoryAdminController` | Update category. |
| `/admin/categories/{id}/activate` | POST | ADMIN | Yes | `CategoryAdminController` | Activate category. |
| `/admin/categories/{id}/deactivate` | POST | ADMIN | Yes | `CategoryAdminController` | Deactivate category. |
| `/admin/questions` | GET | ADMIN | No | `QuestionAdminController` | List questions. |
| `/admin/questions/new` | GET | ADMIN | No | `QuestionAdminController` | Render question creation form. |
| `/admin/questions` | POST | ADMIN | Yes | `QuestionAdminController` | Create question. |
| `/admin/questions/{id}/edit` | GET | ADMIN | No | `QuestionAdminController` | Render question edit form. |
| `/admin/questions/{id}` | POST | ADMIN | Yes | `QuestionAdminController` | Update question. |
| `/admin/questions/{id}/delete` | POST | ADMIN | Yes | `QuestionAdminController` | Archive question. |
| `/admin/questions/{id}/restore` | POST | ADMIN | Yes | `QuestionAdminController` | Restore question. |
| `/admin/quizzes` | GET | ADMIN | No | `QuizAdminController` | List quizzes. |
| `/admin/quizzes/new` | GET | ADMIN | No | `QuizAdminController` | Render quiz creation form. |
| `/admin/quizzes` | POST | ADMIN | Yes | `QuizAdminController` | Create quiz draft. |
| `/admin/quizzes/{id}` | GET | ADMIN | No | `QuizAdminController` | Show quiz detail. |
| `/admin/quizzes/{id}/edit` | GET | ADMIN | No | `QuizAdminController` | Render quiz edit form. |
| `/admin/quizzes/{id}` | POST | ADMIN | Yes | `QuizAdminController` | Update quiz draft. |
| `/admin/quizzes/{id}/publish` | POST | ADMIN | Yes | `QuizAdminController` | Publish quiz. |
| `/admin/quizzes/{id}/archive` | POST | ADMIN | Yes | `QuizAdminController` | Archive quiz. |
| `/admin/results` | GET | ADMIN | No | `AdminResultController` | List attempt reports. |
| `/admin/results/{attemptId}` | GET | ADMIN | No | `AdminResultController` | Show attempt report detail. |

## Actuator

| Route pattern | Method | Role | CSRF | Controller | Purpose |
| --- | --- | --- | --- | --- | --- |
| `/actuator/health` | GET | Anonymous | No | Spring Boot Actuator | Health endpoint for Docker and local checks. |
