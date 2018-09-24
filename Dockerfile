FROM openjdk:8
WORKDIR /app
ADD target/simple-object-storage.jar simple-object-storage.jar
ADD src/main/resources/application.properties application.properties
EXPOSE 8080
#ARG JAR_FILE
#COPY ${JAR_FILE} simpleobjectstorage.jar
RUN mkdir data
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-jar", "simple-object-storage.jar"]