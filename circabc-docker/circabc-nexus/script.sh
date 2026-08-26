cd ${SONATYPE_DIR}
#sudo rm -r /opt/sonatype/sonatype-work/nexus3  
tar xf volume-nexus.tar -C /opt/sonatype/sonatype-work/nexus3
sudo rm -f volume-nexus.tar
./start-nexus-repository-manager.sh
