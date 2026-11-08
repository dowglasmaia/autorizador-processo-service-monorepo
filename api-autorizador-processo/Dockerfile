# syntax=docker/dockerfile:1
# Imagem da API (SPEC-10). Build com JDK 25 + Maven Wrapper; runtime só com JRE 25.

# --- Estágio de build ---------------------------------------------------------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# Camada de dependências separada do código para aproveitar o cache.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -DskipTests package \
    && cp target/api-autorizador-processo-*.jar /workspace/app.jar

# --- Estágio de runtime -------------------------------------------------------
FROM eclipse-temurin:25-jre
# curl é usado só pelo healthcheck do compose (a imagem base não o inclui).
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app \
    && useradd --system --gid app --no-create-home --shell /usr/sbin/nologin app

WORKDIR /app
COPY --from=build --chown=app:app /workspace/app.jar /app/app.jar

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
