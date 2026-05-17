package com.quizz.attempt.service;

import com.quizz.attempt.entity.QuizAttempt;
import com.quizz.attempt.scoring.ScoreResult;

public interface ScoringService {

    ScoreResult score(QuizAttempt attempt);
}
