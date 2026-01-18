# syntax=docker/dockerfile:1.7

# ---------- Build Stage ----------
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace

# Gradle 캐시 최적화: 설정/래퍼를 먼저 복사
COPY gradlew .
COPY gradle gradle
COPY settings.gradle* build.gradle* gradle.properties* ./

# 의존성 캐시 (소스 없이도 가능한 단계)
RUN chmod +x gradlew \
  && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

# 소스 복사
COPY src src

# 빌드 (테스트는 CI에서 수행하므로 컨테이너 빌드에서는 제외 권장)
RUN ./gradlew --no-daemon clean bootJar -x test

# ---------- Run Stage ----------
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

# 보안/권장: non-root 유저로 실행
RUN useradd -ms /bin/bash appuser
USER appuser

# bootJar 복사
COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 8080

# 기본 JVM 옵션(필요 시 docker-compose/.env에서 JAVA_TOOL_OPTIONS로 덮어쓰기 가능)
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
