pdeset MAVEN_OPTS=-Dhttps.protocols=TLSv1.2 -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true

cd ..\circabc-frontend
@REM call yarn install
@REM call yarn build-tomcat
cd ..   
call    
call mvn clean package -D"backend-target.env"=tomcat-filipsl  -D"frontend-target.env"=tomcat -D"server.node"=N1 -D"skip.installnodenpm"=true -D"skip.npm"=true  -D"migration.export.enable"=true
cd circabc-backend\target
call rimraf %CATALINA_HOME%\webapps\*
call rimraf %CATALINA_HOME%\logs
ren circabc.war ROOT.war    
copy ROOT.war  %CATALINA_HOME%\webapps\
ren new.war ui.war 
copy ui.war  %CATALINA_HOME%\webapps\
pause