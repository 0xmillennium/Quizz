# Database Schema Reference

Flyway migrations define the schema. This page summarizes important tables and constraints without duplicating migration files.

| Table | Purpose | Important columns | Important constraints/indexes | Notes |
| --- | --- | --- | --- | --- |
| `users` | Application accounts | `full_name`, `email`, `password_hash`, `role`, `enabled` | `ck_users_role`, `ux_users_email_ci` on `lower(email)` | Password hash is used only by authentication boundary. |
| `categories` | Category master data | `name`, `description`, `active` | `ux_categories_name_ci` on `lower(name)` | Active/inactive lifecycle instead of hard delete. |
| `questions` | Question-bank aggregate root | `category_id`, `text`, `status` | FK to `categories`, `ck_questions_status`, `idx_questions_category_status` | Status values are `ACTIVE` and `ARCHIVED`. |
| `answer_options` | Authored answer options | `question_id`, `text`, `correct`, `display_order` | FK to `questions` with cascade delete, unique question/order, partial unique index for one correct option | Owned by `Question`. |
| `quizzes` | Quiz definitions | `category_id`, `title`, `description`, `duration_minutes`, `question_count`, `attempt_limit`, `retake_cooldown_minutes`, `status` | FK to `categories`, duration/count/status checks, status/category indexes | Status values are `DRAFT`, `PUBLISHED`, and `ARCHIVED`. |
| `quiz_questions` | Quiz pool membership | `quiz_id`, `question_id`, `display_order` | FK to `quizzes` with cascade delete, FK to `questions`, unique quiz/question, unique quiz/order | Explicit join entity; no `ManyToMany`. |
| `quiz_attempts` | Attempt lifecycle and score snapshot | `quiz_id`, `user_id`, quiz/category snapshots, timing, status, completion reason, score counters | Lifecycle check, count check, score bounds, one active attempt partial unique index | `TIME_EXPIRED` attempts use expiry time as submitted time. |
| `attempt_questions` | Question snapshot per attempt | `attempt_id`, `original_question_id`, `question_text`, `display_order`, `selected_option_id`, `answer_revision`, `answered_at`, `correct` | FK to `quiz_attempts` with cascade delete, unique attempt/order | `selected_option_id` is a scalar attempt answer option id. |
| `attempt_answer_options` | Answer option snapshot per attempt question | `attempt_question_id`, `original_answer_option_id`, `option_text`, `correct`, `display_order` | FK to `attempt_questions` with cascade delete, unique question/order, partial unique index for one correct snapshot option | Correctness is used for scoring, results, and admin reporting. |
| `quiz_attempt_allowances` | Attempt rights and cooldown | `user_id`, `quiz_id`, `remaining_attempts`, `cooldown_until`, `last_consumed_at`, `version` | Unique user/quiz, cooldown check, cooldown index | Command flows lock the row when updating rights. |
