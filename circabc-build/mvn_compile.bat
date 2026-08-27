cd .. 
call mvn compile -D"backend-target.env"=tomcat-filipsl  -D"frontend-target.env"=tomcat -D"server.node"=N1 -D"skip.installnodenpm"=true -D"skip.npm"=true  -D"migration.export.enable"=true
cd build