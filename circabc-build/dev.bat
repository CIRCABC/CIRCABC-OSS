call git pull
REM SET JAVA_HOME="C:\ProgramData\AppV\1E69598A-0916-46CF-A75B-FBE77BCC81EA\9C44A07C-E9C0-4981-903D-9EB855FB8C41\Root\VFS\ProgramFilesX64\Java\jdk7"
cd ..\circabc-frontend 
call npm install --force
call npm run development
cd .. 
call mvn clean
call mvn clean package -D"backend-target.env"=dev  -D"frontend-target.env"=development -D"server.node"=N1 -Dweblogic.version=12.2.1 -D"skip.installnodenpm"=true -D"skip.npm"=true  -D"migration.export.enable"=false
cd circabc-backend\target
ren circabc-backend-2.0-dev.ear SDK_Circa_ENT-2.0-dev.ear