REM call npm i rimraf -g
REM call npm i yarn -g
call docker system prune -a --volumes
cd ../circabc-frontend
call yarn install 
call yarn docker
cd .. 
call rimraf circabc-docker\angular\dist\circabc
call mkdir circabc-docker\angular\dist\circabc
call xcopy circabc-frontend\dist\circabc circabc-docker\angular\dist\circabc /s /i /Y
call mvn clean 
call mvn clean package -Dbackend-target.env=tomcat-solr-docker  -Dfrontend-target.env=docker -Dserver.node=N1 -D"skip.installnodenpm"=true -D"skip.npm"=true
call rimraf circabc-docker\tomcat\dist
call mkdir circabc-docker\tomcat\dist
call copy  circabc-backend\target\circabc.war circabc-docker\tomcat\dist
cd circabc-docker
call docker-compose -f docker-compose-tomcat-solr.yml up
pause