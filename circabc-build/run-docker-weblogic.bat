call npm i rimraf -g
cd ../circabc-frontend 
call yarn docker
cd .. 
call mvn clean package -Dbackend-target.env=weblogic-docker  -Dfrontend-target.env=docker -Dserver.node=N1 -D"skip.installnodenpm"=true -D"skip.npm"=true -D"weblogic.version"=12.2.1
call rimraf circabc-docker\weblogic\dist
call mkdir circabc-docker\weblogic\dist
call copy  circabc-backend\target\circabc-backend-2.0-weblogic-docker.ear circabc-docker\weblogic\dist
call rimraf circabc-docker\angular\dist\circabc
call mkdir circabc-docker\angular\dist\circabc
call xcopy circabc-frontend\dist\circabc circabc-docker\angular\dist\circabc /s /i
cd circabc-docker
call docker-compose -f docker-compose-weblogic.yml build
call docker-compose -f docker-compose-weblogic.yml up




