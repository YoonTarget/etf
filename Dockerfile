# 1. 빌드 단계 (Builder Stage)
FROM gradle:8.5-jdk17 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# 소스 코드 복사
COPY . .

# [해결책] ./gradlew 파일 대신, 이미지에 설치된 'gradle' 명령어를 직접 사용합니다.
# 이렇게 하면 파일 권한(126 에러)이나 줄바꿈 문자(CRLF) 문제를 원천 차단할 수 있습니다.
RUN gradle clean build -x test

# 2. 실행 단계 (Runtime Stage)
FROM eclipse-temurin:17-jre-jammy

# 작업 디렉토리 설정
WORKDIR /app

# 빌드 단계에서 생성된 JAR 파일을 복사해옵니다.
COPY --from=builder /app/build/libs/*.jar app.jar

# 컨테이너 실행 시 사용할 포트 노출
EXPOSE 8080

# 애플리케이션 실행 명령어
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]