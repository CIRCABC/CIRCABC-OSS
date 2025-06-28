docker build -t next-circabc .
docker run -d -v "$(pwd)/src:/usr/src/app/src"   --network docker_default -p 4200:4200  next-circabc