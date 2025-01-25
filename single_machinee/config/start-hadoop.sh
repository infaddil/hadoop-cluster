#!/bin/bash

# hadoop-env.sh might define JAVA_HOME, HADOOP_HOME, etc.
export HADOOP_PREFIX=/usr/local/hadoop

echo -e "\nStarting Hadoop..."
$HADOOP_PREFIX/sbin/start-dfs.sh
$HADOOP_PREFIX/sbin/start-yarn.sh
