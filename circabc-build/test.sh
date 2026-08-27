git pull
cd ../circabc-frontend 
npm install
npm run test
cd .. 
mvn clean
mvn clean package --settings settings.xml -D"backend-target.env"=test  -D"frontend-target.env"=test -D"server.node"=N1 -Dweblogic.version=12.2.1 -D"skip.installnodenpm"=true -D"skip.npm"=true  -D"migration.export.enable"=false
cd circabc-backend/target
mv circabc-backend-2.0-test.ear SDK_Circa_ENT-2.0-test.ear