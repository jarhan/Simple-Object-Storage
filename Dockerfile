FROM openjdk:8
WORKDIR /app
ADD target/simple-object-storage.jar simple-object-storage.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "simple-object-storage.jar"]