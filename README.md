# service-template

Basic template for a new javabased service in the SC infrastructure. Includes:
* HelloService
* Gradle project
* Docker configuration
** Filebeats logging watcher
** Tomcat
* Docker compose 
** Postgres
** HelloService


After cloning you can start with:
* gradle build
* make build
* dc up

Verify life at http://localhost/rest

First steps:
* Change project name in:
** settings.gradle
** deploy.env
** Dockerfile
** docker-compose.yml
** src/main/resources/log4j2.json

* Replace HelloSvc.java with your work
