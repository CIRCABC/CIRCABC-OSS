FROM ${docker.acs.image}:${alfresco.platform.version}

ARG TOMCAT_DIR=/usr/local/tomcat
ARG USERNAME=${alfresco.platform.docker.user}

USER root

# Copy Dockerfile to avoid an error if no JARs exist
COPY Dockerfile extensions/*.jar $TOMCAT_DIR/webapps/alfresco/WEB-INF/lib/

# Copy Dockerfile to avoid an error if no AMPs exist
COPY Dockerfile extensions/*.amp $TOMCAT_DIR/amps/
RUN java -jar $TOMCAT_DIR/alfresco-mmt/alfresco-mmt*.jar install \
              $TOMCAT_DIR/amps $TOMCAT_DIR/webapps/alfresco -directory -nobackup -force

# OOTBee Support Tools Spring 7 compatibility patch.
# OOTBee 1.2.3.0 (latest release, targets ACS 25.x / Spring 6.x) calls the 4-arg
# PropertyPlaceholderHelper(prefix, suffix, valueSeparator, ignoreUnresolvable) constructor,
# which Spring Framework 7.0 (bundled in ACS 26.1.0) removed. This breaks the admin console
# pages that read config placeholders (caches, solr-tracking, rendition-service2-transform,
# command-console, jsconsole serverInfo) with "Java constructor ... not found".
# Rewrite those calls to the surviving 5-arg form with a null escape character. Applied AFTER
# the AMP install so it patches the files the AMP unpacked into WEB-INF/classes. The sed pattern
# deliberately avoids '${' so Maven resource filtering of this Dockerfile leaves it intact.
# Remove once OOTBee ships a release (> 1.2.3.0) with Spring 7 support.
RUN find $TOMCAT_DIR/webapps/alfresco/WEB-INF/classes/alfresco/templates/webscripts/org/orderofthebee \
    -name '*.js' -exec sed -i "s/'}', ':', true)/'}', ':', null, true)/g" {} +

COPY alfresco-global.properties $TOMCAT_DIR/shared/classes/alfresco-global.properties
COPY dev-log4j2.properties $TOMCAT_DIR/shared/classes/alfresco/extension
COPY disable-webscript-caching-context.xml $TOMCAT_DIR/shared/classes/alfresco/extension

COPY ESAPI.properties $TOMCAT_DIR/webapps/alfresco/WEB-INF/classes/

# Copy Dockerfile to avoid an error if no license file exists
COPY Dockerfile license/*.* $TOMCAT_DIR/webapps/alfresco/WEB-INF/classes/alfresco/extension/license/

USER ${USERNAME}