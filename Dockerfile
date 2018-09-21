FROM openjdk:8
ADD target/Simple-Object-StorageT.jar target/simple-object-storage.jar
EXPOSE 8085
ARG JAR_FILE
COPY ${JAR_FILE} simple-object-storage.jar
ENTRYPOINT ["java", "-jar", "target/simple-object-storage.jar"]