#Dockerfile

FROM servantcode/tomcat-elk-logging

LABEL maintainer="greg@servantscode.org"

COPY ./build/libs/search-svc-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war
