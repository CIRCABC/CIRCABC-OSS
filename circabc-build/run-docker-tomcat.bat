call npm i rimraf -g
cd ../circabc-frontend 
call yarn docker
cd .. 
call mvn clean package -Dbackend-target.env=tomcat-docker  -Dfrontend-target.env=docker -Dserver.node=N1 -D"skip.installnodenpm"=true -D"skip.npm"=true
call rimraf circabc-docker\tomcat\dist
call mkdir circabc-docker\tomcat\dist
call copy  circabc-backend\target\circabc.war circabc-docker\tomcat\dist
call rimraf circabc-docker\angular\dist\circabc
call mkdir circabc-docker\angular\dist\circabc
call xcopy circabc-frontend\dist\circabc circabc-docker\angular\dist\circabc /s /i
cd circabc-docker
call docker-compose -f docker-compose-tomcat.yml up
pause