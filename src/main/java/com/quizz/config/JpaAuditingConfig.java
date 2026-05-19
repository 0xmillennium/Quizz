package com.quizz.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing for shared entity timestamps.
 *
 * <p>{@link com.quizz.common.entity.BaseEntity} relies on auditing to populate
 * creation and update timestamps consistently across aggregates.</p>
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
