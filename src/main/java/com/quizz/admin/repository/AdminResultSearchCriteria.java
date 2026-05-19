package com.quizz.admin.repository;

import com.quizz.attempt.entity.AttemptStatus;
import java.time.Instant;

public record AdminResultSearchCriteria(
        Long userId,
        Long quizId,
        Long categoryId,
        AttemptStatus status,
        Instant startedFrom,
        Instant startedToExclusive,
        int limit,
        int offset) {
}
