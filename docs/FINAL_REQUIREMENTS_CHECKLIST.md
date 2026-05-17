# Final Requirements Checklist

| Requirement | Implementation |
| --- | --- |
| User registration/login | `com.quizz.auth`, `com.quizz.user`, `com.quizz.security`; routes `GET/POST /register`, `GET/POST /login`. |
| User solves quiz | `com.quizz.quiz` and `com.quizz.attempt`; routes `GET /quizzes`, `GET /quizzes/{id}`, `POST /attempts/start`, `GET /attempts/{attemptId}`, `POST /attempts/{attemptId}/submit`. |
| User sees score | `com.quizz.attempt` result mapping and template; route `GET /attempts/{attemptId}/result`. |
| User sees result history | `QuizAttemptController` and `QuizAttemptQueryService`; route `GET /attempts/history`. |
| Admin adds/updates/deactivates categories | `com.quizz.category`; routes under `/admin/categories`. |
| Admin adds/updates/archives/restores questions | `com.quizz.question`; routes under `/admin/questions`, with delete mapped to archive and restore mapped to restore. |
| Admin creates/publishes/archives category-based quizzes | `com.quizz.quiz`; routes under `/admin/quizzes`. |
| Admin sees user results | `com.quizz.admin`; routes `GET /admin/results` and `GET /admin/results/{attemptId}`. |
| Countdown timer | `attempt/play.html` and `static/js/quiz-timer.js`; server expiry remains authoritative in attempt services. |
| Result chart | `attempt/result.html`, `static/js/result-chart.js`, and `GET /attempts/{attemptId}/chart-data`; chart uses stored result counts. |
| Leaderboard | `com.quizz.leaderboard`; route `GET /leaderboard`; rankings are read from completed attempt snapshots. |
| Framework beyond Servlet/JSP | Spring Boot MVC with Thymeleaf templates. |
| Database connection | PostgreSQL configured in `application-dev.yml`; Spring Data JPA and JDBC repositories. |
| Dynamic pages from DB | Quiz, category, question, attempt, leaderboard, and admin pages are backed by repositories/services. |
| User-submitted data persisted | Registration creates users; attempts persist answers, status, scores, and snapshots. |

## Final Hardening Checks

- Package boundaries are covered by architecture tests.
- Security route matrix is covered by MockMvc tests.
- CSRF is enabled.
- `ddl-auto=validate` and `open-in-view=false` remain configured.
- There is no production seed data or admin bootstrap code.
- Correct answers are present in admin/result views only, not on the play page.
