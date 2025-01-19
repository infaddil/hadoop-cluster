hdfs dfs -rm -r /output

hadoop jar hadoop-streaming.jar -input /input/word.txt -output /output \
-mapper mapper.py -reducer reducer.py \
-file mapper.py -file reducer.py

hdfs dfs -copyToLocal /output/part-00000 /hadoop-data/sortedWords.txt