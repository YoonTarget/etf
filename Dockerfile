# 1. Base Image: 무엇을 기반으로 실행할 것인가?
# Java 17을 실행할 수 있는 가장 가벼운 리눅스(Alpine) 버전을 사용합니다.
# JDK(개발도구)가 아닌 JRE(실행환경)만 사용하여 용량을 줄입니다.
FROM eclipse-temurin:17-jre-alpine

# 2. 작업 디렉토리 설정
# 컨테이너 내부에서 파일들이 위치할 폴더를 만듭니다.
WORKDIR /app

# 3. 빌드된 Jar 파일 복사
# 로컬에서 빌드한 jar 파일을 컨테이너 내부의 app.jar로 복사합니다.
# 주의: 이 Dockerfile을 실행하기 전에 반드시 './gradlew bootJar'로 빌드를 먼저 해야 합니다.
COPY build/libs/*.jar app.jar

# 4. 환경 변수 설정 (기본값)
# 실행 시점에 덮어쓸 수 있지만, 기본적으로 프로덕션(prod) 프로필로 실행되도록 합니다.
ENV SPRING_PROFILES_ACTIVE=prod

# 5. 실행 명령어
# 컨테이너가 시작될 때 실행할 명령입니다.
# java -jar app.jar 명령어를 실행합니다.
ENTRYPOINT ["java", "-jar", "app.jar"]
