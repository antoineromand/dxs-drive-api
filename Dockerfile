FROM openjdk:17-jdk-slim-buster
WORKDIR /app
ARG JAR_VERSION
COPY build/libs/DriveProject-${JAR_VERSION}.jar /app/drive.jar
EXPOSE 3001
CMD ["java", "-jar", "/app/drive.jar"]
