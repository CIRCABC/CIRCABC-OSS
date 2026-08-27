#!/bin/bash

# Stop all containers
docker stop `docker ps -qa`

# Remove all containers
docker rm `docker ps -qa`

# Remove tomcat images
docker image remove angular-circabc:latest
docker image remove tomcat-circabc:latest

docker system prune  --volumes
