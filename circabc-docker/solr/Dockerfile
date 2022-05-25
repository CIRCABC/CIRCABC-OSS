FROM tomcat:8.5.41-jdk8


WORKDIR /usr/local/tomcat/

RUN ["rm", "-fr", "/usr/local/tomcat/webapps/ROOT"]
RUN ["rm", "-fr", "/usr/local/tomcat/webapps/examples"]
RUN ["rm", "-fr", "/usr/local/tomcat/webapps/manager"]

RUN mkdir /opt/solr
COPY ./dist/alfresco-solr-4.2.f.zip /opt/solr/
RUN unzip -q /opt/solr/alfresco-solr-4.2.f.zip -d /opt/solr
RUN rm /opt/solr/alfresco-solr-4.2.f.zip
RUN unzip -q /opt/solr/apache-solr-1.4.1.war -d webapps/solr

COPY ./conf/context.xml /usr/local/tomcat/webapps/solr/META-INF
COPY ./conf/web.xml /usr/local/tomcat/webapps/solr/WEB-INF
COPY /conf/archive-SpacesStore/conf/solrcore.properties  /opt/solr/archive-SpacesStore/conf/ 
COPY /conf/workspace-SpacesStore/conf/solrcore.properties /opt/solr/workspace-SpacesStore/conf/


ENV CATALINA_OPTS="-Dsolr/home=/opt/solr"
ENV JAVA_OPTS " -XX:-DisableExplicitGC -Djava.security.egd=file:/dev/./urandom -Djava.awt.headless=true -Dfile.encoding=UTF-8 "

WORKDIR /root
COPY wait-for-it.sh /usr/local/tomcat/wait-for-it.sh
CMD ["/usr/local/tomcat/wait-for-it.sh","tomcat:8080","--timeout=0","--strict", "--","catalina.sh", "run"]


