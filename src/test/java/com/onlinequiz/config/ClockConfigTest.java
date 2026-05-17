package com.onlinequiz.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class ClockConfigTest {

    @Test
    void clockBeanUsesUtcZone() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ClockConfig.class)) {
            Clock clock = context.getBean(Clock.class);

            assertThat(clock).isNotNull();
            assertThat(clock.getZone()).isEqualTo(ZoneOffset.UTC);
        }
    }
}
