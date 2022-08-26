FROM openjdk:13
WORKDIR /
ADD target/SpringMicroservice-0.0.1-SNAPSHOT.jar //
EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "-Dspring.profiles.active=mysql", "/SpringMicroservice-0.0.1-SNAPSHOT.jar"]