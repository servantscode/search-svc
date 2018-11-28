#!/bin/bash

SERVICE=$1
echo "Configuring service $SERVICE"

files="Dockerfile deploy.env docker-compose.yml settings.gradle src/main/resources/log4j2.json"

for file in $files; do 
  echo "Updating $file"
  sed -i "s/hello/$SERVICE/g" $file
done

SERVICE="$(tr '[:lower:]' '[:upper:]' <<< ${SERVICE:0:1})${SERVICE:1}"
DATE=`date +%D`
USER=`whoami`
printf "$SERVICE service\nCreator: $USER\nCreated: $DATE" >README.md
