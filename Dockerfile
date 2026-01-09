# ---------- Build Stage ----------
FROM gradle:8.13-jdk17-alpine AS build
WORKDIR /home/gradle/project

COPY build.gradle settings.gradle ./
COPY gradle gradle/

COPY src src

# 3) 빌드 (테스트까지 포함하려면 build, 제외하려면 -x test)
RUN gradle clean build

# ---------- Run Stage ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /home/gradle/project/build/libs/*.jar api.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "api.jar"]