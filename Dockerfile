FROM maven:3.9.12-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml ./
RUN mvn --batch-mode dependency:go-offline
COPY src ./src
RUN mvn --batch-mode package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /workspace/target/similar-products-service-1.0.0.jar app.jar
USER app
EXPOSE 5000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
