# Stop all containers
docker stop `docker ps -qa`

# Remove all containers
docker rm `docker ps -qa`

# Remove all images
docker rmi -f `docker images -qa `

# Remove all volumes
docker volume rm $(docker volume ls -q)

# Remove all networks
docker network rm `docker network ls -q`

# Prune resources
docker system prune -f -a --volumes
docker volume prune -f
docker network prune -f

# Your installation should now be all fresh and clean.

# The following commands should not output any items:
# docker ps -a
# docker images -a 
# docker volume ls

# The following command show only show the default networks:
# docker network ls

# sudo rm -rf tomcat/volumes/
# sudo rm -rf mysql/data/
