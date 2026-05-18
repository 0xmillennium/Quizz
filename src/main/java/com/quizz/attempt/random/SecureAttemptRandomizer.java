package com.quizz.attempt.random;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SecureAttemptRandomizer implements AttemptRandomizer {

    private final SecureRandom random = new SecureRandom();

    @Override
    public <T> List<T> shuffledCopy(List<T> source) {
        List<T> copy = new ArrayList<>(source);
        Collections.shuffle(copy, random);
        return copy;
    }
}
