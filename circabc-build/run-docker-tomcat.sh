rm -f ./circabc-backend/target/circabc.war
rm -rf ./circabc-frontend/dist/circabc
mvn clean
mvn clean package -Dbackend-target.env=tomcat-docker  -Dfrontend-target.env=docker -Dserver.node=N1
rm -rf ./circabc-docker/tomcat/dist
rm -rf ./circabc-docker/angular/dist
mkdir -p circabc-docker/tomcat/dist     
mkdir -p circabc-docker/angular/dist
cp -f  ./circabc-backend/target/circabc.war ./circabc-docker/tomcat/dist
cp -rf ./circabc-frontend/dist/circabc ./circabc-docker/angular/dist
cd circabc-docker
docker-compose -f docker-compose-tomcat.yml build
docker-compose -f docker-compose-tomcat.yml up


