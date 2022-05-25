FROM tomcat:8.5.41-jdk8

RUN ["rm", "-fr", "/usr/local/tomcat/webapps/ROOT"]
RUN ["rm", "-fr", "/usr/local/tomcat/webapps/examples"]
#RUN ["rm", "-fr", "/usr/local/tomcat/webapps/manager"]

#COPY ./dist/circabc.war /usr/local/tomcat/webapps/ROOT.war
COPY ./lib/ecas-weblogic-12.2.1-authprovider-4.26.3.jar /usr/local/tomcat/lib
COPY ./lib/mysql-connector-java-8.0.16.jar /usr/local/tomcat/lib
COPY ./conf/context.xml /usr/local/tomcat/conf/context.xml
COPY ./conf/server.xml /usr/local/tomcat/conf/server.xml

CMD ["/usr/local/tomcat/wait-for-it.sh","db:3306","--timeout=0","--strict", "--","catalina.sh", "run"]
COPY wait-for-it.sh /usr/local/tomcat/wait-for-it.sh

