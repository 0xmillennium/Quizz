package com.quizz;

import com.quizz.tooling.PasswordHashCli;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
