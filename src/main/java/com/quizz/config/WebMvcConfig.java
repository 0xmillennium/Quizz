package com.quizz.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC view-controller configuration.
 *
 * <p>The root route redirects to the authenticated quiz list, leaving route
 * authorization to {@link com.quizz.security.config.SecurityConfig}.</p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/quizzes");
    }
}
