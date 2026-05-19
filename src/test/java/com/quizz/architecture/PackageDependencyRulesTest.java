package com.quizz.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class PackageDependencyRulesTest {

    private static final Path MAIN_SOURCES = Path.of("src/main/java");

    @Test
    void commonPackageDoesNotDependOnFeaturePackages() throws IOException {
        assertPackageDoesNotReference("common",
                "auth", "security", "user", "category", "question", "quiz", "attempt", "leaderboard", "admin");
    }

    @Test
    void userPackageDoesNotDependOnOtherFeaturePackages() throws IOException {
        assertPackageDoesNotReference("user",
                "auth", "security", "category", "question", "quiz", "attempt", "leaderboard", "admin");
    }

    @Test
    void authPackageDoesNotDependOnRepositories() throws IOException {
        assertPackageDoesNotReference("auth", ".repository.");
    }

    @Test
    void securityPackageDoesNotDependOnRepositories() throws IOException {
        assertPackageDoesNotReference("security", ".repository.");
    }

    @Test
    void categoryPackageDoesNotDependOnDownstreamPackages() throws IOException {
        assertPackageDoesNotReference("category", "question", "quiz", "attempt", "leaderboard", "admin");
    }

    @Test
    void questionPackageDoesNotDependOnDownstreamPackages() throws IOException {
        assertPackageDoesNotReference("question", "quiz", "attempt", "leaderboard", "admin");
    }

    @Test
    void quizPackageDoesNotDependOnAttemptLeaderboardOrAdmin() throws IOException {
        assertPackageDoesNotReference("quiz", "attempt", "leaderboard", "admin");
    }

    @Test
    void attemptPackageDoesNotDependOnLeaderboardOrAdmin() throws IOException {
        assertPackageDoesNotReference("attempt", "leaderboard", "admin");
    }

    @Test
    void leaderboardPackageDoesNotDependOnAttemptServiceRepositoryOrAdmin() throws IOException {
        assertPackageDoesNotReference("leaderboard", "attempt.service", "attempt.repository", "admin");
    }

    @Test
    void adminPackageDoesNotDependOnMutableAttemptOrLeaderboardOrDomainRepositories() throws IOException {
        assertPackageDoesNotReference("admin",
                "attempt.repository",
                "attempt.service",
                "leaderboard.service",
                "leaderboard.repository",
                "user.repository",
                "quiz.repository",
                "question.repository",
                "category.repository");
    }

    @Test
    void forbiddenDirectRepositoryDependenciesAreAbsent() throws IOException {
        assertPackageDoesNotReference("quiz",
                "com.quizz.question.repository.QuestionRepository",
                "com.quizz.category.repository.CategoryRepository");
        assertPackageDoesNotReference("attempt",
                "com.quizz.quiz.repository.QuizRepository",
                "com.quizz.user.repository.UserRepository",
                "com.quizz.question.repository.QuestionRepository");
        assertPackageDoesNotReference("leaderboard", "com.quizz.attempt.repository.QuizAttemptRepository");
        assertPackageDoesNotReference("admin",
                "com.quizz.attempt.repository.QuizAttemptRepository",
                "com.quizz.user.repository.UserRepository",
                "com.quizz.quiz.repository.QuizRepository",
                "com.quizz.question.repository.QuestionRepository",
                "com.quizz.category.repository.CategoryRepository");
    }

    @Test
    void oldPackageAndApplicationClassAreAbsent() throws IOException {
        List<SourceFile> files = javaSources();
        String oldPackageRoot = "com.online" + "quiz";
        String oldApplicationClass = "Online" + "QuizApplication";

        assertThat(files)
                .filteredOn(file -> file.text().contains(oldPackageRoot)
                        || file.text().contains(oldApplicationClass))
                .as("old package root or application class references")
                .isEmpty();
    }

    private static void assertPackageDoesNotReference(String packageName, String... forbiddenReferences)
            throws IOException {
        List<SourceFile> offenders = javaSourcesUnder(packageName).stream()
                .filter(file -> containsAnyForbiddenReference(file.text(), forbiddenReferences))
                .toList();

        assertThat(offenders)
                .extracting(SourceFile::relativePath)
                .as("forbidden references in com.quizz.%s", packageName)
                .isEmpty();
    }

    private static boolean containsAnyForbiddenReference(String source, String[] forbiddenReferences) {
        for (String forbiddenReference : forbiddenReferences) {
            if (forbiddenReference.startsWith("com.quizz.") || forbiddenReference.startsWith(".")) {
                if (source.contains(forbiddenReference)) {
                    return true;
                }
            } else if (source.contains("com.quizz." + forbiddenReference + ".")) {
                return true;
            }
        }
        return false;
    }

    private static List<SourceFile> javaSourcesUnder(String packageName) throws IOException {
        Path packagePath = MAIN_SOURCES.resolve("com/quizz").resolve(packageName.replace('.', '/'));
        if (!Files.exists(packagePath)) {
            return List.of();
        }
        return javaSources(packagePath);
    }

    private static List<SourceFile> javaSources() throws IOException {
        return javaSources(MAIN_SOURCES);
    }

    private static List<SourceFile> javaSources(Path root) throws IOException {
        try (var stream = Files.walk(root)) {
            return stream
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(PackageDependencyRulesTest::readSource)
                    .toList();
        }
    }

    private static SourceFile readSource(Path path) {
        try {
            return new SourceFile(MAIN_SOURCES.relativize(path).toString(),
                    Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read " + path, exception);
        }
    }

    private record SourceFile(String relativePath, String text) {
    }
}
