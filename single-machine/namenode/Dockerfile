FROM bde2020/hadoop-base:2.0.0-hadoop3.2.1-java8

MAINTAINER Ivan Ermilov <ivan.s.ermilov@gmail.com>

HEALTHCHECK CMD curl -f http://localhost:9870/ || exit 1

ENV HDFS_CONF_dfs_namenode_name_dir=file:///hadoop/dfs/name
RUN mkdir -p /hadoop/dfs/name
VOLUME /hadoop/dfs/name

ADD run.sh /run.sh
RUN chmod a+x /run.sh

EXPOSE 9870

RUN sed -i 's/http:\/\/archive\.debian\.org\/debian/https:\/\/deb.debian.org\/debian/g' /etc/apt/sources.list

RUN sed -i 's/stretch/buster/g' /etc/apt/sources.list

RUN apt update
RUN apt install -y --no-install-recommends \
  python3 python3-pip \
  nano

ADD hadoop-streaming.jar /hadoop-data
ADD mapper.py /hadoop-data
ADD reducer.py /hadoop-data
ADD word.txt /hadoop-data
ADD runmapreduce.sh /hadoop-data
RUN chmod +x /hadoop-data/runmapreduce.sh

CMD ["/run.sh"]


