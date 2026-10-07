# Runtime image for the jpassvault sync server.
# Build after `./gradlew clean bootJar`:
#   docker build -t jpassvaultserver .
# The service requires JPASSVAULT_SECRET and runs as a non-root user. TLS is
# expected to terminate at the edge; set
# app-properties.tls-terminated-at-proxy=true behind a trusted TLS proxy.
FROM eclipse-temurin:25-jre

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --home-dir /app --shell /usr/sbin/nologin app

WORKDIR /app

COPY --chown=app:app build/libs/jpassvaultserver-*.jar /app/app.jar

RUN install -d -m 0700 -o app -g app /app/data && chown -R app:app /app

USER app

ENV SERVER_PORT=9393
ENV SPRING_DATASOURCE_URL=jdbc:h2:file:/app/data/maindb

EXPOSE 9393

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -sf -H "token: ${JPASSVAULT_SECRET}" http://127.0.0.1:9393/check || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
