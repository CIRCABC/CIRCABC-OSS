cd ../circabc-frontend
npm install 
npm run docker
cd .. 
mvn clean
mvn clean package -Dbackend-target.env=tomcat-docker  -Dfrontend-target.env=docker -Dserver.node=N1 -D"skip.installnodenpm"=true -D"skip.npm"=true
mkdir ./circabc-docker/tomcat/dist
cp -f  ./circabc-backend/target/circabc.war ./circabc-docker/tomcat/dist
cp -rf ./circabc-frontend/dist/circabc ./circabc-docker/angular/dist
cd circabc-docker
./clean.sh
docker-compose -f docker-compose-tomcat.yml up
