FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY build/libs/DriveProject-0.0.1-SNAPSHOT.jar /app/DriveProject-0.0.1-SNAPSHOT.jar
EXPOSE 3001
CMD ["java", "-jar", "/app/DriveProject-0.0.1-SNAPSHOT.jar"]

