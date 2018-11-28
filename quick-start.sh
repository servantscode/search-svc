#!/bin/bash

SERVICE=$1
echo "Configuring service $SERVICE"

files="Dockerfile deploy.env docker-compose.yml settings.gradle src/main/resources/log4j2.json"

for file in $files; do 
  echo "Updating $file"
  sed -i "s/hello/$SERVICE/g" $file
done
