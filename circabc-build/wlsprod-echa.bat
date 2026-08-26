call git pull
cd ..\circabc-frontend
call npm install --force
call npm run build-scircabc-prod
cd .. 
call mvn clean
call mvn clean package -D"backend-target.env"=wlsprod-echa  -D"frontend-target.env"=build-scircabc-prod -D"server.node"=N1 -Dweblogic.version=12.2.1 -D"skip.installnodenpm"=true -D"skip.npm"=true  -D"migration.export.enable"=false
cd circabc-backend\target
ren circabc-backend-2.0-wlsprod-echa.ear SDK_Circa_ENT-2.0-wlsprod-echa.ear
