call git pull
cd ..\circabc-frontend 
call npm install --force
call npm run build-shs-acceptance
cd .. 
call mvn clean package -D"backend-target.env"=wlsacc-shs  -D"frontend-target.env"=build-shs-acceptance -D"server.node"=N1 -Dweblogic.version=12.2.1 -D"skip.installnodenpm"=true -D"skip.npm"=true  -D"migration.export.enable"=false