FROM tomcat:10.1-jdk21

# 1. Install netcat/curl if needed for healthchecks inside (optional) or other tools

# 2. Copy the WAR
COPY target/EuCaptcha.war /usr/local/tomcat/webapps/eu-captcha.war

# 3. Copy and set entrypoint
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

RUN sed -i 's/port="8080"/port="80"/' /usr/local/tomcat/conf/server.xml
EXPOSE 80

CMD ["/usr/local/bin/entrypoint.sh"]