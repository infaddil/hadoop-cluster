########################################################################
# Dockerfile: Hadoop 2.7.2 on Ubuntu 20.04 + Python 3.9 (default) + matplotlib
########################################################################

FROM ubuntu:20.04

ENV DEBIAN_FRONTEND=noninteractive

# We can remove the old MAINTAINER line to avoid the warning
LABEL maintainer="KiwenLau <kiwenlau@gmail.com>"

WORKDIR /root

########################################################################
# 1) Install base packages: SSH, software-properties-common, etc.
########################################################################
RUN apt-get update && apt-get install -y \
    openssh-server \
    software-properties-common \
    wget \
    curl \
    vim \
    && rm -rf /var/lib/apt/lists/*

########################################################################
# 2) Install OpenJDK 8 (for Hadoop 2.7.2)
########################################################################
RUN apt-get update && apt-get install -y openjdk-8-jdk && \
    rm -rf /var/lib/apt/lists/*

########################################################################
# 3) Install Python 3.9 from Deadsnakes, then set python3 => 3.9
########################################################################
RUN add-apt-repository ppa:deadsnakes/ppa && \
    apt-get update && \
    apt-get install -y python3.9 python3.9-dev python3.9-distutils && \
    rm -rf /var/lib/apt/lists/*

# Install pip for Python 3.9
RUN curl -sS https://bootstrap.pypa.io/get-pip.py -o /tmp/get-pip.py && \
    python3.9 /tmp/get-pip.py && \
    rm /tmp/get-pip.py

# Make python3 -> python3.9
RUN update-alternatives --install /usr/bin/python3 python3 /usr/bin/python3.9 1

########################################################################
# 4) Install python libs for your data analysis
########################################################################
RUN python3 --version  # should show 3.9 now
RUN python3 -m pip install --no-cache-dir numpy matplotlib pillow

########################################################################
# 5) Download & install Hadoop 2.7.2
########################################################################
RUN wget https://github.com/kiwenlau/compile-hadoop/releases/download/2.7.2/hadoop-2.7.2.tar.gz && \
    tar -xzvf hadoop-2.7.2.tar.gz && \
    mv hadoop-2.7.2 /usr/local/hadoop && \
    rm hadoop-2.7.2.tar.gz

########################################################################
# 6) Set environment variables for Hadoop & Java
########################################################################
ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
ENV HADOOP_HOME=/usr/local/hadoop
ENV PATH=$PATH:/usr/local/hadoop/bin:/usr/local/hadoop/sbin

########################################################################
# 7) Configure SSH + Hadoop directories
########################################################################
RUN ssh-keygen -t rsa -f ~/.ssh/id_rsa -P '' && \
    cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys && \
    mkdir -p ~/hdfs/namenode && \
    mkdir -p ~/hdfs/datanode && \
    mkdir -p /usr/local/hadoop/logs

########################################################################
# 8) Copy config/* into place
########################################################################
COPY config/* /tmp/

RUN mv /tmp/ssh_config ~/.ssh/config && \
    mv /tmp/hadoop-env.sh /usr/local/hadoop/etc/hadoop/hadoop-env.sh && \
    mv /tmp/hdfs-site.xml /usr/local/hadoop/etc/hadoop/hdfs-site.xml && \
    mv /tmp/core-site.xml /usr/local/hadoop/etc/hadoop/core-site.xml && \
    mv /tmp/mapred-site.xml /usr/local/hadoop/etc/hadoop/mapred-site.xml && \
    mv /tmp/yarn-site.xml /usr/local/hadoop/etc/hadoop/yarn-site.xml && \
    mv /tmp/slaves /usr/local/hadoop/etc/hadoop/slaves && \
    mv /tmp/start-hadoop.sh /root/start-hadoop.sh && \
    mv /tmp/run-wordcount.sh /root/run-wordcount.sh

RUN chmod +x /root/start-hadoop.sh && \
    chmod +x /root/run-wordcount.sh && \
    chmod +x /usr/local/hadoop/sbin/start-dfs.sh && \
    chmod +x /usr/local/hadoop/sbin/start-yarn.sh

########################################################################
# 9) Format HDFS (One-time)
########################################################################
RUN /usr/local/hadoop/bin/hdfs namenode -format

########################################################################
# 10) CMD: start ssh, then drop into bash
########################################################################
CMD ["sh", "-c", "service ssh start; bash"]
