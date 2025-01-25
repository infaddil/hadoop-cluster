#!/bin/bash
# Purpose: Start a single-container Hadoop "master" from the newly built image.

# 1) Remove any old container named hadoop-master
docker rm -f hadoop-master &>/dev/null

# 2) Make sure the "hadoop" bridge network exists (ignore errors if it already does)
docker network create --driver=bridge hadoop &>/dev/null

echo "Starting single-container hadoop-master using image my-hadoop-python39:latest..."
docker run -itd \
  --net=hadoop \
  --name hadoop-master \
  --hostname hadoop-master \
  -p 9870:9870 \
  -p 9000:9000 \
  -p 8088:8088 \
  my-hadoop-python39:latest

# 3) Kick off Hadoop inside the container
echo "Starting Hadoop inside hadoop-master..."
docker exec -it hadoop-master bash /root/start-hadoop.sh

echo "Done! You can now 'docker exec -it hadoop-master bash' to enter the container."
