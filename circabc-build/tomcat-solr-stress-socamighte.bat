call git pull
cd ..\circabc-frontend
call yarn
call yarn stress
cd .. 
call mvn clean
call mvn clean package -D"backend-target.env"=tomcat-solr-stress-socamighte  -D"frontend-target.env"=stress -D"server.node"=N1 -Dweblogic.version=12.2.1 -D"skip.installnodenpm"=true -D"skip.npm"=true  -D"migration.export.enable"=false
cd circabc-backend\target
ren circabc-backend-2.0-tomcat-solr-stress-socamighte.ear SDK_Circa_ENT-2.0-stress1.ear