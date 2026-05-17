package com.quizz.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import com.quizz.security.principal.CustomUserDetails;
import com.quizz.user.entity.User;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

class SecurityBoundaryRulesTest {

    private static final Path MAIN_SOURCES = Path.of("src/main/java");
    private static final Path TEMPLATES = Path.of("src/main/resources/templates");
    private static final Path STATIC = Path.of("src/main/resources/static");
    private static final String ALLOWED_SECURITY_CONTEXT_HOLDER_FILE =
            "com/quizz/security/context/SecurityCurrentUserProvider.java";
    private static final Pattern STATIC_REFERENCE = Pattern.compile("th:(?:href|src)=\"@\\{/((?:css|js)/[^}\"()]+)");

    @Test
    void securityContextHolderIsOnlyUsedByCurrentUserProvider() throws IOException {
        List<SourceFile> offenders = javaSources().stream()
                .filter(file -> file.text().contains("SecurityContextHolder"))
                .filter(file -> !file.relativePath().equals(ALLOWED_SECURITY_CONTEXT_HOLDER_FILE))
                .toList();

        assertThat(offenders)
                .extracting(SourceFile::relativePath)
                .as("SecurityContextHolder must stay behind CurrentUserProvider")
                .isEmpty();
    }

    @Test
    void domainUserDoesNotImplementSpringSecurityUserDetails() {
        assertThat(UserDetails.class.isAssignableFrom(User.class)).isFalse();
    }

    @Test
    void customUserDetailsIsTheSpringSecurityAdapter() {
        assertThat(UserDetails.class.isAssignableFrom(CustomUserDetails.class)).isTrue();
    }

    @Test
    void csrfIsNotDisabledInSecurityConfiguration() throws IOException {
        SourceFile securityConfig = javaSources().stream()
                .filter(file -> file.relativePath().equals("com/quizz/security/config/SecurityConfig.java"))
                .findFirst()
                .orElseThrow();

        assertThat(securityConfig.text())
                .doesNotContain("csrf().disable")
                .doesNotContain("csrf(AbstractHttpConfigurer::disable")
                .doesNotContain("csrf(csrf -> csrf.disable")
                .doesNotContain(".csrf().disable()");
    }

    @Test
    void controllersDoNotPerformManualRoleChecks() throws IOException {
        List<SourceFile> offenders = javaSources().stream()
                .filter(file -> file.relativePath().endsWith("Controller.java"))
                .filter(file -> file.text().contains("SecurityContextHolder")
                        || file.text().contains("getAuthorities()")
                        || file.text().contains("hasRole(")
                        || file.text().contains("ROLE_"))
                .toList();

        assertThat(offenders)
                .extracting(SourceFile::relativePath)
                .as("controller role decisions belong in SecurityConfig")
                .isEmpty();
    }

    @Test
    void templateStaticReferencesResolveToExistingFiles() throws IOException {
        List<String> missingReferences = templateSources().stream()
                .flatMap(file -> STATIC_REFERENCE.matcher(file.text()).results()
                        .map(match -> file.relativePath() + " -> " + match.group(1)))
                .filter(reference -> {
                    String staticPath = reference.substring(reference.indexOf(" -> ") + 4);
                    return !Files.exists(STATIC.resolve(staticPath));
                })
                .toList();

        assertThat(missingReferences)
                .as("template CSS/JS references must point at existing static files")
                .isEmpty();
    }

    @Test
    void playTemplateDoesNotExposeCorrectAnswersOrSnapshotMetadata() throws IOException {
        String playTemplate = Files.readString(TEMPLATES.resolve("attempt/play.html"), StandardCharsets.UTF_8);

        assertThat(playTemplate)
                .doesNotContain("correct")
                .doesNotContain("correctOptionText")
                .doesNotContain("originalAnswerOptionId");
    }

    private static List<SourceFile> javaSources() throws IOException {
        try (var stream = Files.walk(MAIN_SOURCES)) {
            return stream
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(SecurityBoundaryRulesTest::readSource)
                    .toList();
        }
    }

    private static List<SourceFile> templateSources() throws IOException {
        try (var stream = Files.walk(TEMPLATES)) {
            return stream
                    .filter(path -> path.toString().endsWith(".html"))
                    .map(path -> readSource(TEMPLATES, path))
                    .toList();
        }
    }

    private static SourceFile readSource(Path path) {
        return readSource(MAIN_SOURCES, path);
    }

    private static SourceFile readSource(Path root, Path path) {
        try {
            return new SourceFile(root.relativize(path).toString(), Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read " + path, exception);
        }
    }

    private record SourceFile(String relativePath, String text) {
    }
}
