call git pull
cd ..\circabc-frontend 
call npm install --force
call npm run build-shs-prod
cd .. 
call mvn clean package -D"backend-target.env"=wlsprod-shs  -D"frontend-target.env"=build-shs-prod -D"server.node"=N1 -Dweblogic.version=12.2.1 -D"skip.installnodenpm"=true -D"skip.npm"=true  -D"migration.export.enable"=false