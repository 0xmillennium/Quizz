package com.quizz.tooling;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class PasswordHashCliTest {

    @Test
    void generatesBcryptHashThatMatchesRawPassword() {
        CliResult result = runCli("StrongPassword123!");

        assertThat(result.exitCode()).isZero();
        assertThat(new BCryptPasswordEncoder().matches("StrongPassword123!", result.stdout().trim())).isTrue();
    }

    @Test
    void doesNotPrintPlainPasswordToStdout() {
        String rawPassword = "StrongPassword123!";

        CliResult result = runCli(rawPassword);

        assertThat(result.stdout()).doesNotContain(rawPassword);
    }

    @Test
    void rejectsBlankPassword() {
        CliResult result = runCli("   ");

        assertThat(result.exitCode()).isNotZero();
    }

    @Test
    void rejectsPasswordShorterThanTwelveCharacters() {
        CliResult result = runCli("Short123!");

        assertThat(result.exitCode()).isNotZero();
    }

    @Test
    void writesValidationErrorToStderrOnInvalidInput() {
        CliResult result = runCli("");

        assertThat(result.stderr()).contains("Password is required");
        assertThat(result.stdout()).isEmpty();
    }

    @Test
    void returnsNonZeroExitCodeOnInvalidInput() {
        CliResult result = runCli("tiny");

        assertThat(result.exitCode()).isNotZero();
    }

    @Test
    void returnsZeroExitCodeOnValidInput() {
        CliResult result = runCli("StrongPassword123!");

        assertThat(result.exitCode()).isZero();
    }

    private CliResult runCli(String input) {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        int exitCode = PasswordHashCli.run(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(stdout, true, StandardCharsets.UTF_8),
                new PrintStream(stderr, true, StandardCharsets.UTF_8)
        );

        return new CliResult(
                exitCode,
                stdout.toString(StandardCharsets.UTF_8),
                stderr.toString(StandardCharsets.UTF_8)
        );
    }

    private record CliResult(int exitCode, String stdout, String stderr) {
    }
}
