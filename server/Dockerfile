ARG MAVEN_IMAGE=maven:3.9.16-eclipse-temurin-21
ARG JAVA_IMAGE=eclipse-temurin:21-jre-noble

FROM ${MAVEN_IMAGE} AS build

WORKDIR /workspace

COPY . .

RUN mvn -B -ntp -pl yuncheng-boot -am clean package -DskipTests

FROM ${JAVA_IMAGE}

ARG APP_VERSION=dev
ARG VCS_REF=unknown

LABEL org.opencontainers.image.title="Yuncheng Quick Server" \
      org.opencontainers.image.description="Yuncheng Quick Spring Boot server" \
      org.opencontainers.image.version="${APP_VERSION}" \
      org.opencontainers.image.revision="${VCS_REF}" \
      org.opencontainers.image.source="https://github.com/liochaoo/yuncheng-quick"

RUN groupadd --gid 10001 app \
    && useradd --uid 10001 --gid 10001 --no-create-home --shell /usr/sbin/nologin app \
    && mkdir -p /app/tmp /app/data/files \
    && chown -R 10001:10001 /app

WORKDIR /app

COPY --from=build --chown=10001:10001 \
    /workspace/yuncheng-boot/target/yuncheng-boot.jar \
    /app/yuncheng-quick-server.jar

ENV SPRING_PROFILES_ACTIVE=prod \
    PLATFORM_FILE_LOCAL_PATH=/app/data/files/ \
    TZ=Asia/Shanghai

EXPOSE 8087

USER 10001:10001

ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Djava.io.tmpdir=/app/tmp", "-jar", "/app/yuncheng-quick-server.jar"]
