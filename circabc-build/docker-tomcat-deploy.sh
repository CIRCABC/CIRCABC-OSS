#!/bin/bash
set -e  # Exit on error

cd ..

rm -rf circabc-docker/tomcat/dist
mkdir -p circabc-docker/tomcat/dist
cp -f circabc-backend/target/circabc.war circabc-docker/tomcat/dist/ROOT.war

rm -rf circabc-docker/angular/dist
mkdir -p circabc-docker/angular/dist/circabc
cp -rf circabc-frontend/dist/circabc/* circabc-docker/angular/dist/circabc/


