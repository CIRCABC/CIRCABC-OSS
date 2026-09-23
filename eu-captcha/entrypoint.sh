#!/bin/bash
set -e

# Default to 'eu-captcha' if env var is not set
CONTEXT_PATH=${SERVER_SERVLET_CONTEXT_PATH:-/eu-captcha}

# Remove leading slash and replace remaining slashes with hashes for Tomcat naming
# e.g. /circabc-caas-dev/eu-captcha -> circabc-caas-dev#eu-captcha
WAR_NAME=$(echo "$CONTEXT_PATH" | sed 's/^\///' | sed 's/\//\#/g').war

echo "Deploying application to context: $CONTEXT_PATH (WAR: $WAR_NAME)"

# Move the WAR to the correct name
mv /usr/local/tomcat/webapps/eu-captcha.war "/usr/local/tomcat/webapps/$WAR_NAME"

# Start Tomcat
exec catalina.sh run