package com.quizz.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class LayeringRulesTest {

    private static final Path MAIN_SOURCES = Path.of("src/main/java");

    @Test
    void controllersDoNotInjectPersistenceOrSecurityContextDependencies() throws IOException {
        List<SourceFile> offenders = javaSources().stream()
                .filter(file -> file.relativePath().endsWith("Controller.java"))
                .filter(file -> file.text().contains(".repository.")
                        || file.text().contains("EntityManager")
                        || file.text().contains("SecurityContextHolder")
                        || file.text().contains("@Transactional"))
                .toList();

        assertThat(offenders)
                .extracting(SourceFile::relativePath)
                .as("controllers must stay behind services and security config")
                .isEmpty();
    }

    @Test
    void servicesDoNotDependOnControllersViewsOrSecurityContextHolder() throws IOException {
        List<SourceFile> offenders = javaSourcesUnder("com/quizz").stream()
                .filter(file -> file.relativePath().contains("/service/"))
                .filter(file -> file.text().contains(".controller.")
                        || file.text().contains("org.springframework.ui.")
                        || file.text().contains("org.springframework.web.servlet")
                        || file.text().contains("ModelAndView")
                        || file.text().contains("RedirectAttributes")
                        || file.text().contains("BindingResult")
                        || file.text().contains("SecurityContextHolder"))
                .toList();

        assertThat(offenders)
                .extracting(SourceFile::relativePath)
                .as("services must not depend on MVC view/controller/security-context APIs")
                .isEmpty();
    }

    @Test
    void scoringServicesDoNotDependOnRepositories() throws IOException {
        List<SourceFile> offenders = javaSourcesUnder("com/quizz/attempt").stream()
                .filter(file -> file.relativePath().contains("/scoring/")
                        || file.relativePath().endsWith("ScoringService.java")
                        || file.relativePath().endsWith("DefaultScoringService.java"))
                .filter(file -> file.text().contains(".repository."))
                .toList();

        assertThat(offenders)
                .extracting(SourceFile::relativePath)
                .as("scoring must work from attempt snapshots, not repositories")
                .isEmpty();
    }

    @Test
    void reportingServicesAreReadOnlyAndDoNotMutateDomainState() throws IOException {
        List<SourceFile> offenders = javaSources().stream()
                .filter(file -> file.relativePath().startsWith("com/quizz/leaderboard/service/")
                        || file.relativePath().startsWith("com/quizz/admin/service/"))
                .filter(file -> file.text().contains(".save(")
                        || file.text().contains(".delete(")
                        || file.text().contains(".flush(")
                        || file.text().contains("@Transactional\n")
                        || file.text().contains("@Transactional(") && !file.text().contains("readOnly = true"))
                .toList();

        assertThat(offenders)
                .extracting(SourceFile::relativePath)
                .as("leaderboard/admin reporting services must remain read-only")
                .isEmpty();
    }

    @Test
    void entitiesDoNotDependOnDtosServicesControllersRepositoriesOrSecurity() throws IOException {
        List<SourceFile> offenders = javaSources().stream()
                .filter(file -> file.relativePath().contains("/entity/"))
                .filter(file -> file.text().contains(".dto.")
                        || file.text().contains(".service.")
                        || file.text().contains(".controller.")
                        || file.text().contains(".repository.")
                        || file.text().contains("org.springframework.security")
                        || file.text().contains("SecurityContextHolder"))
                .toList();

        assertThat(offenders)
                .extracting(SourceFile::relativePath)
                .as("entities must remain persistence/domain objects")
                .isEmpty();
    }

    @Test
    void jpaRepositoriesStayInOwningRepositoryPackages() throws IOException {
        List<SourceFile> offenders = javaSources().stream()
                .filter(file -> file.text().contains("extends JpaRepository"))
                .filter(file -> !file.relativePath().matches("com/quizz/[^/]+/repository/[^/]+Repository\\.java"))
                .toList();

        assertThat(offenders)
                .extracting(SourceFile::relativePath)
                .as("feature JpaRepository interfaces must stay inside their owning repository package")
                .isEmpty();
    }

    @Test
    void jdbcQueryRepositoriesOnlyExistInLeaderboardAndAdminRepositoryPackages() throws IOException {
        List<SourceFile> offenders = javaSources().stream()
                .filter(file -> file.text().contains("JdbcTemplate")
                        || file.text().contains("NamedParameterJdbcTemplate"))
                .filter(file -> !file.relativePath().startsWith("com/quizz/leaderboard/repository/")
                        && !file.relativePath().startsWith("com/quizz/admin/repository/"))
                .toList();

        assertThat(offenders)
                .extracting(SourceFile::relativePath)
                .as("JDBC read repositories are limited to leaderboard and admin reporting")
                .isEmpty();
    }

    @Test
    void utilityDumpingGroundClassesDoNotExist() throws IOException {
        List<String> forbiddenClassNames = List.of(
                "Common" + "Utils.java",
                "App" + "Constants.java",
                "Question" + "Utils.java",
                "Quiz" + "Utils.java",
                "Attempt" + "Utils.java",
                "Leaderboard" + "Utils.java",
                "Admin" + "Utils.java"
        );

        List<SourceFile> offenders = javaSources().stream()
                .filter(file -> forbiddenClassNames.stream().anyMatch(file.relativePath()::endsWith))
                .toList();

        assertThat(offenders)
                .extracting(SourceFile::relativePath)
                .as("utility dumping-ground classes")
                .isEmpty();
    }

    private static List<SourceFile> javaSourcesUnder(String relativeDirectory) throws IOException {
        Path root = MAIN_SOURCES.resolve(relativeDirectory);
        if (!Files.exists(root)) {
            return List.of();
        }
        return javaSources(root);
    }

    private static List<SourceFile> javaSources() throws IOException {
        return javaSources(MAIN_SOURCES);
    }

    private static List<SourceFile> javaSources(Path root) throws IOException {
        try (var stream = Files.walk(root)) {
            return stream
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(LayeringRulesTest::readSource)
                    .toList();
        }
    }

    private static SourceFile readSource(Path path) {
        try {
            return new SourceFile(MAIN_SOURCES.relativize(path).toString(), Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read " + path, exception);
        }
    }

    private record SourceFile(String relativePath, String text) {
    }
}
