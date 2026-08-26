#!/bin/bash

mvn clean
mvn clean package -Dbackend-target.env=tomcat-docker  -Dfrontend-target.env=docker -Dserver.node=N2
cd circabc-frontend
npm i --force
npm run docker
cd ..
mkdir -p circabc-docker/tomcat/dist
mkdir -p circabc-docker/solr/dist
mkdir -p circabc-docker/angular/dist
cp -f  circabc-backend/target/circabc.war circabc-docker/tomcat/dist/ROOT.war
cp -f  solr/alfresco-solr-4.2.f.zip circabc-docker/solr/dist/
cp -rf circabc-frontend/dist/circabc circabc-docker/angular/dist/
cd circabc-docker
# docker compose -f docker-compose-tomcat.yml up --build
docker compose -f docker-compose-tomcat-solr.yml up --build