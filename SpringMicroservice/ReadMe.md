# Extending, Securing and Dockerizing Spring Boot Microservices

Final Product requires External MySql Database. Install Docker For Mac/Windows/Linux

### Docker Commands

#### Start MySql Container (downloads image if not found)

`docker run --detach --name ec-mysql -p 6604:3306 -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=explorecalifornia -e MYSQL_USER=sans_user -e MYSQL_PASSWORD=sans_pass -d mysql`

**view all images**

`docker images`

**view all containers (running or not)**

`docker ps -a`

**Interact with Database (link to ec-mysql container) with mysql client**

`docker run -it --link ec-mysql:mysql --rm mysql sh -c 'exec mysql -h"$MYSQL_PORT_3306_TCP_ADDR" -P"$MYSQL_PORT_3306_TCP_PORT" -uroot -p"$MYSQL_ENV_MYSQL_ROOT_PASSWORD"'`

**Stop ec-mysql container**

`docker stop ec-mysql`

**(ReStart) ec-mysql container**

`docker start ec-mysql`

**Remove ec-mysql container (must stop it first)**

`docker rm ec-mysql`

**Remove image (must stop and remove container first)**

`docker rmi mysql:latest`

#### Startup with Profile settings
##### Default profile, H2 database
``
mvn spring-boot:run
``

or

``
java  -jar target/SpringMicroservice-0.0.1-SNAPSHOT.jar
``
##### mysql profile, MySql database (requires running container ec-mysql)
``
mvn spring-boot:run -Dspring.profiles.active=mysql
``

or

``
java  -Dspring.profiles.active=mysql -jar target/SpringMicroservice-3.0.0-SNAPSHOT.jar
``
#### Dockerize Explore California
##### Build jar
``
mvn package -DskipTests
``
##### Build Docker image
``
docker build -t explorecalifonia .
``
##### Run Docker container
``
docker run --name ec-app -p8080:8080 -d explorecalifonia
``

##### Run Docker container linked with mysql
``
docker run  --name ec-app -p 8080:8080  --link ec-mysql:mysql -d explorecalifornia
``
##### enter Docker container
``
docker exec -t -i ec-app /bin/bash
``