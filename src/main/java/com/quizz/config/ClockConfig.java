package com.quizz.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Central time source for lifecycle decisions.
 *
 * <p>
 * Attempt expiry, cooldown, autosave, and scoring transitions depend on this
 * {@link Clock} bean so tests can replace time deterministically.
 * </p>
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
