FROM bde2020/hadoop-base:2.0.0-hadoop3.2.1-java8

MAINTAINER Ivan Ermilov <ivan.s.ermilov@gmail.com>

HEALTHCHECK CMD curl -f http://localhost:8042/ || exit 1

ADD run.sh /run.sh
RUN chmod a+x /run.sh

EXPOSE 8042

RUN sed -i 's/http:\/\/archive\.debian\.org\/debian/https:\/\/deb.debian.org\/debian/g' /etc/apt/sources.list

RUN sed -i 's/stretch/buster/g' /etc/apt/sources.list

RUN apt update
RUN apt install -y --no-install-recommends \
  python3 python3-pip \
  nano

CMD ["/run.sh"]
