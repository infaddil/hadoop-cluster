# Install Ubuntu On Windows Using WSL

1. Seach Windows PowerShell in Windows search bar, then select Run as administrator
2. To install WSL in the command prompt, run:
```
  wsl --install
```
3. After WSL has been installed, restart the laptop
4. Go to Microsoft Store > Search Ubuntu > Download Ubuntu

# Configure Ubuntu
1. After Ubuntu has been installed, open Ubuntu
2. To install latest updates, run:
```
  sudo apt upgrade
```

# Setup Local Docker Hadoop Cluster
## Prerequisites: Docker and Docker Compose

1. In the command prompt, change the current working directory to the location where you want the cloned directory to be
2. To clone the repository, run:
```
  git clone https://github.com/hanashah-01/docker-hadoop-with-python-mapreduce.git
```
3. Change the directory to where docker-compose.yml is at. In this case, type 'cd docker-hadoop'
4. To start the docker containers, run:
```
  docker-compose up -d
```
5. To confirm the availability of containers, run:
```
  docker ps
```

# Running Python MapReduce function

1. To access the container of Hadoop cluster's namenode, run:
```
  docker exec -it namenode bash
```
2. To create folder structure in HDFS to allocate files, run:
```
  hdfs dfs -l/
```
```
  hdfs dfs -mkdir -p /user/root
```
3. Exit the container. Then, to move the input file, mapper.py and reducer.py to namenode, run:
```
  docker cp input namenode:/tmp
```
```
  docker cp mapper.py namenode:/tmp
```
```
  docker cp reducer.py namenode:/tmp
```
4. Get in namenode container again. To create the input folder, run:
```
  hdfs dfs -mkdir /user/root/input
```
5. Change directory to /tmp
6. To move the input files to the input folder, run:
```
  hdfs dfs -put input/* /user/root/input
```
7. Find the path to the JAR file. To locate the hadoop string library JAR file, run:
```
  find / -name 'hadoop-streaming*.jar'
```
8. To run the MapReduce program, run:
```
  hadoop jar /opt/hadoop-3.2.1/share/hadoop/tools/lib/hadoop-streaming-3.2.1.jar -files mapper.py -mapper mapper.py -file reducer.py -reducer reducer.py -input /user/root/input/* -output /user/root/output
```

# Python Configuration In Container
## This must be done to run the MapReduce Python program

1. To install python in each container, run:
```
  docker exec -it namenode bash -c "apt update && apt install python -y"
```
```
  docker exec -it datanode bash -c "apt update && apt install python -y"
```
```
  docker exec -it resourcemanager bash -c "apt update && apt install python -y"
```
```
  docker exec -it nodemanager bash -c "apt update && apt install python -y"
```
