# Multi-stage build for XAI-Forge Backend
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# Copy parent POM first
COPY pom.xml .
# Copy backend directory
COPY backend ./backend
# Create empty frontend to satisfy parent POM
RUN mkdir -p frontend && echo '<?xml version="1.0"?><project><modelVersion>4.0.0</modelVersion><groupId>com.example</groupId><artifactId>frontend</artifactId><version>1.0.0</version></project>' > frontend/pom.xml
# Build only backend module, skip test compilation, ensure Spring Boot repackages JAR
RUN mvn clean package spring-boot:repackage -DskipTests -Dmaven.test.skip=true -Dmaven.main.skip=false -pl backend -am

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/backend/target/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
