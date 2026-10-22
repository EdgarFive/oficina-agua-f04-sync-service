# ETAPA 1: Compilar el microservicio
FROM eclipse-temurin:17-jdk AS build

WORKDIR /workspace

# Copiar Maven Wrapper y configuración
COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN chmod +x mvnw

# Copiar contrato OpenAPI y código Java
COPY openapi ./openapi
COPY src ./src

# Generar OpenAPI y compilar la aplicación
RUN ./mvnw -B -DskipTests clean package


# ETAPA 2: Ejecutar el microservicio
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copiar la aplicación compilada
COPY --from=build /workspace/target/sync-service-0.0.1-SNAPSHOT.jar app.jar

# Ejecutar con un usuario sin privilegios
USER 10001

# Puerto de Spring Boot
EXPOSE 8080

# Iniciar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
