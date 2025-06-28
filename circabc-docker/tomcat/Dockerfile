FROM tomcat:8.5.100-jdk8-temurin

RUN ["rm", "-fr", "/usr/local/tomcat/webapps/ROOT"]
RUN ["rm", "-fr", "/usr/local/tomcat/webapps/examples"]
RUN ["rm", "-fr", "/usr/local/tomcat/webapps/manager"]

COPY ./dist/ROOT.war /usr/local/tomcat/webapps/ROOT.war
COPY ./lib/ecas-weblogic-12.2.1-authprovider-4.26.3.jar /usr/local/tomcat/lib
COPY ./lib/mysql-connector-j-9.3.0.jar /usr/local/tomcat/lib

COPY ./conf/context.xml /usr/local/tomcat/conf/context.xml

EXPOSE 8889

ENV JPDA_ADDRESS=8889
ENV JPDA_TRANSPORT=dt_socket

CMD ["/usr/local/tomcat/wait-for-it.sh","db:3306","--timeout=0","--strict", "--","catalina.sh", "run"]
COPY wait-for-it.sh /usr/local/tomcat/wait-for-it.sh



CMD ["catalina.sh", "jpda","run"]