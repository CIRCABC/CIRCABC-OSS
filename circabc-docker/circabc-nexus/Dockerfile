FROM sonatype/nexus3
USER root
RUN yum install -y -q sudo
COPY script.sh /opt/sonatype/script.sh
COPY volume-nexus.tar /opt/sonatype/volume-nexus.tar
#ADD volume-nexus.tar /opt/sonatype/sonatype-work/nexus3
CMD ["sh", "-c", "/opt/sonatype/script.sh"]
