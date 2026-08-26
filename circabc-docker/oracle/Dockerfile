FROM container-registry.oracle.com/database/standard:12.1.0.2


COPY ./patch/dockerInit.sh /home/oracle/setup/dockerInitPatched.sh
RUN chown oracle:oinstall /home/oracle/setup/dockerInitPatched.sh
RUN chmod u+x /home/oracle/setup/dockerInitPatched.sh
COPY ./patch/setupScriptsDB.sh /home/oracle/setup/setupScriptsDB.sh
RUN chown oracle:oinstall /home/oracle/setup/setupScriptsDB.sh
RUN chmod u+x /home/oracle/setup/setupScriptsDB.sh
