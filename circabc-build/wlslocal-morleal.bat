call git pull
cd ..\circabc-frontend
call npm install --force
call npm run localhost
cd ..  
call mvn clean
call mvn clean package -D"backend-target.env"=wlslocal-morleal  -D"frontend-target.env"=localhost -D"server.node"=N1 -Dweblogic.version=12.2.1 -D"skip.installnodenpm"=true -D"skip.npm"=true  -D"migration.export.enable"=false