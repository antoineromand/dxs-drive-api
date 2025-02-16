FROM openjdk:17-jdk-slim-buster
WORKDIR /app
ARG VERSION
COPY build/libs/DriveProject-${VERSION}.jar /app/drive.jar
EXPOSE 3001
CMD ["java", "-jar", "/app/drive.jar"]

