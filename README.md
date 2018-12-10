# service-template

Basic template for a new javabased service in the SC infrastructure. Includes:
* HelloService
* Gradle project
* Docker configuration
  * Filebeats logging watcher
  * Tomcat
* Docker compose 
  * Postgres
  * HelloService


After cloning you can start with:
* gradle build
* make build
* dc up

Verify life at http://localhost/rest

First steps:
* Create and populate new service repo
  * Create new repo {new-service} on github (https://github.com/servantscode)
  * Rename your repo locally

* Change project name using quick-start.sh:
  * ./quick-start.sh {service-name}

This will update the service name in:
  * settings.gradle
  * deploy.env
  * Dockerfile
  * docker-compose.yml
  * src/main/resources/log4j2.json

It will also:
  * Update README.md
  * Change git destination 
    * git remote set-url origin git@github.com:servantscode/{new-service}.git
  * Push template files to repo
    * git push

* Replace HelloSvc.java with your work
