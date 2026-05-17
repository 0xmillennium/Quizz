CREATE INDEX idx_questions_category_status
ON questions(category_id, status);

CREATE INDEX idx_quizzes_status
ON quizzes(status);

CREATE INDEX idx_quizzes_category_status
ON quizzes(category_id, status);

CREATE INDEX idx_quiz_questions_quiz
ON quiz_questions(quiz_id);

CREATE INDEX idx_quiz_questions_question
ON quiz_questions(question_id);

CREATE INDEX idx_quiz_attempts_user_started
ON quiz_attempts(user_id, started_at DESC);

CREATE INDEX idx_quiz_attempts_quiz_status
ON quiz_attempts(quiz_id, status);

CREATE INDEX idx_quiz_attempts_status_expires
ON quiz_attempts(status, expires_at);

CREATE INDEX idx_attempt_questions_attempt
ON attempt_questions(attempt_id);

CREATE INDEX idx_attempt_answer_options_question
ON attempt_answer_options(attempt_question_id);

CREATE INDEX idx_quiz_attempts_leaderboard_overall
ON quiz_attempts(status, user_id, score_percentage DESC, correct_count DESC, submitted_at ASC);

CREATE INDEX idx_quiz_attempts_leaderboard_quiz
ON quiz_attempts(quiz_id, status, user_id, score_percentage DESC, correct_count DESC, submitted_at ASC);

CREATE INDEX idx_quiz_attempts_leaderboard_category
ON quiz_attempts(category_id_snapshot, status, user_id, score_percentage DESC, correct_count DESC, submitted_at ASC);

CREATE INDEX idx_quiz_attempts_admin_started
ON quiz_attempts(started_at DESC);

CREATE INDEX idx_quiz_attempts_admin_status_started
ON quiz_attempts(status, started_at DESC);

CREATE INDEX idx_quiz_attempts_admin_category_started
ON quiz_attempts(category_id_snapshot, started_at DESC);
