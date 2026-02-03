# 1. 빌드 단계 (Builder Stage)
# Gradle이 설치된 이미지를 사용하여 소스 코드를 빌드합니다.
FROM gradle:8.5-jdk17 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# 소스 코드 복사
COPY . .

# Gradle 빌드 실행 (테스트 제외하여 빌드 속도 향상)
RUN ./gradlew clean build -x test

# 2. 실행 단계 (Runtime Stage)
# 더 안정적인 Eclipse Temurin JDK 이미지를 사용합니다.
FROM eclipse-temurin:17-jre-jammy

# 작업 디렉토리 설정
WORKDIR /app

# 빌드 단계에서 생성된 JAR 파일을 복사해옵니다.
COPY --from=builder /app/build/libs/*.jar app.jar

# 컨테이너 실행 시 사용할 포트 노출 (문서화 용도)
EXPOSE 8080

# 애플리케이션 실행 명령어
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]