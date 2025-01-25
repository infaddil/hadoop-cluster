########################################################################
# Dockerfile: Hadoop 2.7.2 on Ubuntu 20.04 + Python 3.9 + matplotlib
########################################################################

# ----------------------------------------------------------------------
# 1) Use modern Ubuntu (20.04) instead of the old 14.04
# ----------------------------------------------------------------------
FROM ubuntu:20.04

# We don't want apt-get to ask for timezone or user input
ENV DEBIAN_FRONTEND=noninteractive

MAINTAINER KiwenLau <kiwenlau@gmail.com>
WORKDIR /root

# ----------------------------------------------------------------------
# 2) Install prerequisites: SSH server, Java 8, wget, plus tools
# ----------------------------------------------------------------------
RUN apt-get update && apt-get install -y \
    openssh-server \
    software-properties-common \
    wget \
    curl \
    sudo \
    vim \
    && rm -rf /var/lib/apt/lists/*

# ----------------------------------------------------------------------
# 3) Install OpenJDK 8 specifically
#    (Hadoop 2.7.2 usually expects Java 7 or 8, but we'll do 8)
# ----------------------------------------------------------------------
RUN apt-get update && apt-get install -y openjdk-8-jdk && \
    rm -rf /var/lib/apt/lists/*

# ----------------------------------------------------------------------
# 4) Install Python 3.9 from deadsnakes PPA
#    Because Ubuntu 20.04 default is Python 3.8. You want 3.9 specifically.
# ----------------------------------------------------------------------
RUN add-apt-repository ppa:deadsnakes/ppa && \
    apt-get update && \
    apt-get install -y python3.9 python3.9-dev python3.9-distutils && \
    rm -rf /var/lib/apt/lists/*

# ----------------------------------------------------------------------
# 5) Install pip for Python 3.9
# ----------------------------------------------------------------------
RUN curl https://bootstrap.pypa.io/get-pip.py -o /tmp/get-pip.py && \
    python3.9 /tmp/get-pip.py && \
    rm /tmp/get-pip.py

# ----------------------------------------------------------------------
# 6) Install your Python libraries (matplotlib, etc.)
# ----------------------------------------------------------------------
RUN python3.9 -m pip install --no-cache-dir numpy matplotlib pillow

# ----------------------------------------------------------------------
# 7) Download & install Hadoop 2.7.2
# ----------------------------------------------------------------------
RUN wget https://github.com/kiwenlau/compile-hadoop/releases/download/2.7.2/hadoop-2.7.2.tar.gz && \
    tar -xzvf hadoop-2.7.2.tar.gz && \
    mv hadoop-2.7.2 /usr/local/hadoop && \
    rm hadoop-2.7.2.tar.gz

# ----------------------------------------------------------------------
# 8) Set environment variables for Hadoop & Java
# ----------------------------------------------------------------------
ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
ENV HADOOP_HOME=/usr/local/hadoop
ENV PATH=$PATH:/usr/local/hadoop/bin:/usr/local/hadoop/sbin

# ----------------------------------------------------------------------
# 9) Configure SSH (no key passphrase), plus Hadoop logs
# ----------------------------------------------------------------------
RUN ssh-keygen -t rsa -f ~/.ssh/id_rsa -P '' && \
    cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys && \
    mkdir -p ~/hdfs/namenode && \
    mkdir -p ~/hdfs/datanode && \
    mkdir $HADOOP_HOME/logs

# ----------------------------------------------------------------------
# 10) Copy your config/* files (like in the old Dockerfile) to /tmp
# ----------------------------------------------------------------------
COPY config/* /tmp/

# ----------------------------------------------------------------------
# 11) Move them into Hadoop's etc/hadoop or ~/.ssh/
# ----------------------------------------------------------------------
RUN mv /tmp/ssh_config ~/.ssh/config && \
    mv /tmp/hadoop-env.sh /usr/local/hadoop/etc/hadoop/hadoop-env.sh && \
    mv /tmp/hdfs-site.xml $HADOOP_HOME/etc/hadoop/hdfs-site.xml && \
    mv /tmp/core-site.xml $HADOOP_HOME/etc/hadoop/core-site.xml && \
    mv /tmp/mapred-site.xml $HADOOP_HOME/etc/hadoop/mapred-site.xml && \
    mv /tmp/yarn-site.xml $HADOOP_HOME/etc/hadoop/yarn-site.xml && \
    mv /tmp/slaves $HADOOP_HOME/etc/hadoop/slaves && \
    mv /tmp/start-hadoop.sh ~/start-hadoop.sh && \
    mv /tmp/run-wordcount.sh ~/run-wordcount.sh

# ----------------------------------------------------------------------
# 12) Make the scripts executable
# ----------------------------------------------------------------------
RUN chmod +x ~/start-hadoop.sh && \
    chmod +x ~/run-wordcount.sh && \
    chmod +x $HADOOP_HOME/sbin/start-dfs.sh && \
    chmod +x $HADOOP_HOME/sbin/start-yarn.sh

# ----------------------------------------------------------------------
# 13) Format the NameNode (one-time for brand-new HDFS)
# ----------------------------------------------------------------------
RUN /usr/local/hadoop/bin/hdfs namenode -format

# ----------------------------------------------------------------------
# 14) Start SSH when container runs, then keep shell open
# ----------------------------------------------------------------------
CMD ["sh", "-c", "service ssh start; bash"]
