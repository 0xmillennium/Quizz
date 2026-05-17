package com.quizz.attempt.scoring;

import com.quizz.attempt.entity.AttemptQuestion;
import java.util.List;

public interface ScoreCalculator {

    ScoreResult calculate(List<AttemptQuestion> questions);
}
