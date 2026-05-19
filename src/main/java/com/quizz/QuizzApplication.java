package com.quizz;

import com.quizz.tooling.PasswordHashCli;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point and production-jar tooling dispatcher.
 *
 * <p>
 * When invoked as {@code hash-password}, startup delegates to
 * {@link PasswordHashCli} and returns before the Spring context is created.
 * All other invocations start the MVC application normally.
 * </p>
 */
@SpringBootApplication
public class QuizzApplication {

    public static void main(String[] args) {
        if (args.length == 1 && "hash-password".equals(args[0])) {
            int exitCode = PasswordHashCli.run(System.in, System.out, System.err);
            if (exitCode != 0) {
                System.exit(exitCode);
            }
            return;
        }

        SpringApplication.run(QuizzApplication.class, args);
    }
}
