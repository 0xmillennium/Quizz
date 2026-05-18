package com.quizz.attempt.random;

import java.util.List;

public interface AttemptRandomizer {

    <T> List<T> shuffledCopy(List<T> source);
}
