REM SET JAVA_HOME="C:\ProgramData\AppV\1E69598A-0916-46CF-A75B-FBE77BCC81EA\9C44A07C-E9C0-4981-903D-9EB855FB8C41\Root\VFS\ProgramFilesX64\Java\jdk7"
cd .. 
call mvn clean
call mvn clean package -D"backend-target.env"=tomcat-solr-acc-michelia  -D"frontend-target.env"=acceptance -D"server.node"=N2 -D"skip.installnodenpm"=true -D"skip.npm"=true  -D"migration.export.enable"=false
