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
 * Change git destination 
  * git remote set-url origin git@github.com:servantscode/{new-service}.git
 * Update README.md
 * Push template files to repo
  * git push

* Change project name in:
  * settings.gradle
  * deploy.env
  * Dockerfile
  * docker-compose.yml
  * src/main/resources/log4j2.json

* Replace HelloSvc.java with your work
