#!/bin/bash
set -e  # Exit on error
cd ..
# Start docker
cd circabc-docker
docker-compose -f docker-compose-tomcat.yml down 
docker-compose -f docker-compose-tomcat.yml up --build
