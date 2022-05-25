
rm  ./circabc-backend/target/circabc.war
rm -rf  ./circabc-frontend/dist/circabc/*
mvn clean
mvn clean package -Dbackend-target.env=tomcat-solr-docker  -Dfrontend-target.env=docker -Dserver.node=N1
cp -f  ./circabc-backend/target/circabc.war ./circabc-docker/tomcat/dist
cp -rf ./circabc-frontend/dist/circabc ./circabc-docker/angular/dist
cd circabc-docker
clean.sh
docker-compose -f docker-compose-tomcat-solr.yml up


