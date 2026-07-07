# 1. Build stage: JDK 17
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Copia solo file necessari
COPY pom.xml .
COPY src ./src

# Build con Maven, skip test
RUN mvn clean package -DskipTests

# 2. Run stage: JRE 17 (più leggero)
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copia il JAR compilato dal build stage
COPY --from=build /app/target/*.jar player-service.jar

# Porta esposta (Render usa di solito 8080 o PORT)
EXPOSE 7072

# Comando di esecuzione
ENTRYPOINT ["java", "-jar", "player-service.jar"]