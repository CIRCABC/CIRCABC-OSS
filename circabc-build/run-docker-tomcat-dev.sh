mvn clean
mvn clean package -Dbackend-target.env=tomcat-docker  -Dfrontend-target.env=docker -Dserver.node=N1
#cp -f  ./circabc-backend/target/circabc.war ./circabc-docker/tomcat/dist
jar xvf  ./circabc-backend/target/circabc.war  -C  ../circabc-docker/tomcat-dev/dist 
cp -rf ./circabc-frontend/dist/circabc ./circabc-docker/angular-dev/dist
cd circabc-docker
clean.sh
docker-compose -f docker-compose-tomcat.yml up


