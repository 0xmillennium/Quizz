FROM maven:3.9.11-eclipse-temurin-25 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:26-jre-alpine-3.23 AS runtime

WORKDIR /app

RUN apk add --no-cache curl \
    && addgroup -S -g 10001 quizz \
    && adduser -S -D -H -u 10001 -G quizz quizz

COPY --from=build /workspace/target/*.jar /app/quizz.jar
RUN chown quizz:quizz /app/quizz.jar

USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/quizz.jar"]
