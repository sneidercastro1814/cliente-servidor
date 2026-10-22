# =============================================================================
# Imagen del backend (API REST). Build multietapa: la imagen final solo lleva
# el JRE y el jar, no Maven ni el codigo fuente.
# =============================================================================

# ---------- Etapa 1: compilacion ----------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# El pom va primero y solo: asi la capa de dependencias se reutiliza mientras
# no cambien las dependencias, y los rebuilds de codigo son rapidos.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Etapa 2: ejecucion ----------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Usuario sin privilegios: el proceso no corre como root.
RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /app/target/*.jar app.jar
RUN chown spring:spring /app/app.jar

USER spring

EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
