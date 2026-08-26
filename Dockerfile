FROM eclipse-temurin:17-jre-jammy

ARG JAR_FILE

RUN groupadd --system exchange \
    && useradd --system --gid exchange --home-dir /app --shell /usr/sbin/nologin exchange \
    && mkdir -p /app/uploads \
    && chown -R exchange:exchange /app

WORKDIR /app
COPY --chown=exchange:exchange ${JAR_FILE} app.jar

USER exchange

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
