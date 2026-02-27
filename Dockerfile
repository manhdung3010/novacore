FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml trước để cache dependencies Maven
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Copy source code và build ứng dụng
COPY src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# Copy file JAR đã build từ stage trước
COPY --from=build /app/target/novacore-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

