FROM openjdk:11
COPY build/libs/studysquad-0.0.1-SNAPSHOT.jar app.jar
CMD ["java", "-jar", "app.jar"]