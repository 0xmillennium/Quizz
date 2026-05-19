package com.quizz.tooling;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * CLI helper for generating bcrypt password hashes from the production jar.
 *
 * <p>
 * The {@code hash-password} mode reads the plaintext password from standard
 * input, writes only the generated hash to standard output, and writes
 * validation or I/O failures to standard error. It is invoked before the Spring
 * application context starts so operators can generate admin bootstrap hashes
 * without connecting to application infrastructure.
 * </p>
 */
public final class PasswordHashCli {

    private static final int BCRYPT_STRENGTH = 12;
    private static final int MIN_PASSWORD_LENGTH = 12;

    private PasswordHashCli() {
    }

    public static int run(InputStream in, PrintStream out, PrintStream err) {
        String password;

        try {
            password = readPassword(in);
        } catch (IOException ex) {
            err.println("Failed to read password from stdin.");
            return 1;
        }

        if (password.isEmpty()) {
            err.println("Password is required on stdin.");
            return 1;
        }

        if (password.isBlank()) {
            err.println("Password must not be blank.");
            return 1;
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            err.println("Password must be at least 12 characters long.");
            return 1;
        }

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(BCRYPT_STRENGTH);
        out.println(passwordEncoder.encode(password));
        return 0;
    }

    private static String readPassword(InputStream in) throws IOException {
        String input = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        int end = input.length();

        while (end > 0) {
            char current = input.charAt(end - 1);
            if (current != '\n' && current != '\r') {
                break;
            }
            end--;
        }

        return input.substring(0, end);
    }
}
