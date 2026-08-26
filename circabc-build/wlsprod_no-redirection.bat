call git pull
cd ..\circabc-frontend 
call npm install --force
call npm run production
cd .. 
call mvn clean
call mvn clean package -D"backend-target.env"=wlsprod_no-redirection  -D"frontend-target.env"=production -D"server.node"=N3 -Dweblogic.version=12.2.1 -D"skip.installnodenpm"=true -D"skip.npm"=true  -D"migration.export.enable"=false
cd circabc-backend\target
ren circabc-backend-2.0-wlsprod_no-redirection.ear SDK_Circa_ENT-2.0-wlsprod.ear