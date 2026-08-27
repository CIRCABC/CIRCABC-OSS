cd circabc-frontend 
npm install 
npm run docker
cd .. 
mvn clean
mvn clean package -Dbackend-target.env=weblogic-docker  -Dfrontend-target.env=development -Dserver.node=N1  -Dweblogic.version=12.2.1 -D"skip.installnodenpm"=true -D"skip.npm"=true
cp -f  ./circabc-backend/target/circabc-backend-2.0-weblogic-docker.ear ./circabc-docker/weblogic/dist/circabc.ear
cp -rf ./circabc-frontend/dist/circabc ./circabc-docker/angular/dist
cd circabc-docker
clean.sh
docker-compose -f docker-compose-weblogic.yml build
docker-compose -f docker-compose-weblogic.yml up




