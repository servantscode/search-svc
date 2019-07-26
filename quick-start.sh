#!/bin/bash

SERVICE=$1
echo "Configuring service $SERVICE"

files="Dockerfile kube.yml deploy.env settings.gradle src/main/webapp/WEB-INF/web.xml src/main/java/org/servantscode/hello/rest/ServiceIdentificationFilter.java src/main/java/org/servantscode/hello/rest/HelloSvc.java src/main/java/org/servantscode/hello/db/DBUpgrade.java"

for file in $files; do 
  echo "Updating $file"
  sed -i "s/hello/$SERVICE/g" $file
done

mv src/main/java/org/servantscode/hello src/main/java/org/servantscode/$SERVICE

SERVICE="$(tr '[:lower:]' '[:upper:]' <<< ${SERVICE:0:1})${SERVICE:1}"
DATE=`date +%D`
USER=`whoami`
printf "Service: $SERVICE\nCreator: $USER\nCreated: $DATE" > README.md

git remote set-url origin git@github.com:servantscode/$SERVICE-svc.git
git push
