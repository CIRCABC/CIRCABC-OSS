#!/bin/bash

# Clean existing Maven artifacts
mvn clean

# Start Maven build in background
echo "Starting Maven build..."
mvn clean package -Dbackend-target.env=tomcat-docker -Dfrontend-target.env=docker -Dserver.node=N2 &
MAVEN_PID=$!

# Start npm build in background
echo "Starting npm build..."
cd circabc-frontend
npm i --force
npm run docker &
NPM_PID=$!

# Wait for Maven build to complete
echo "Waiting for Maven build to complete..."
wait $MAVEN_PID
if [ $? -ne 0 ]; then
  echo "Maven build failed"
  exit 1
fi
echo "Maven build completed successfully"

# Wait for npm build to complete
echo "Waiting for npm build to complete..."
wait $NPM_PID
if [ $? -ne 0 ]; then
  echo "npm build failed"
  exit 1
fi
echo "npm build completed successfully"

# Return to root directory
cd ..

# Create directories if they don't exist
mkdir -p circabc-docker/tomcat/dist
mkdir -p circabc-docker/solr/dist
mkdir -p circabc-docker/angular/dist

# Copy build artifacts
cp -f circabc-backend/target/circabc.war circabc-docker/tomcat/dist/
cp -f solr/alfresco-solr-4.2.f.zip circabc-docker/solr/dist/
cp -rf circabc-frontend/dist/circabc circabc-docker/angular/dist/

# Run Docker compose
cd circabc-docker
# docker compose -f docker-compose-tomcat.yml up --build
docker compose -f docker-compose-tomcat-solr.yml up --build