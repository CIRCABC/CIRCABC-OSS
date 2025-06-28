#!/bin/bash
set -e  # Exit on error

# Go to frontend directory and run docker script
cd ../circabc-frontend
npm run docker


cd ../circabc-backend
# Build the backend with Maven
mvn clean package \
  -Dbackend-target.env=tomcat-docker \
  -Dfrontend-target.env=docker \
  -Dserver.node=N1 \
  -Dskip.installnodenpm=true \
  -Dskip.npm=true


