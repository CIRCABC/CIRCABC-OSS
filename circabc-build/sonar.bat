REM SET JAVA_HOME="C:\ProgramData\AppV\1E69598A-0916-46CF-A75B-FBE77BCC81EA\9C44A07C-E9C0-4981-903D-9EB855FB8C41\Root\VFS\ProgramFilesX64\Java\jdk8"
cd ../circabc-backend
call mvn sonar:sonar -Psonar -Dsonar.projectKey=DIGITCIRCABC-CIRCABC   -Dsonar.host.url=https://webgate.ec.europa.eu/CITnet/sonarqube -Dsonar.login=aebf7f58fdde49381d1c38309320ca1eabe0ef89

