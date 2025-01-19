#!/usr/bin/env python
"""multithread_mapper.py"""

import sys
import threading

def map_line(line):
    # remove leading and trailing whitespace
    line = line.strip()
    # split the line into words
    words = line.split()
    # increase counters
    for word in words:
        # write the results to STDOUT (standard output)
        # tab-delimited; the trivial word count is 1
        print '%s\t%s' % (word, 1)

def map_lines(lines):
    for line in lines:
        map_line(line)

if __name__ == "__main__":
    input_lines = sys.stdin.readlines()
    num_threads = 5

    lines_per_thread = len(input_lines) // num_threads
    threads = []

    for i in range(0, len(input_lines), lines_per_thread):
        thread = threading.Thread(target=map_lines, args=(input_lines[i:i+lines_per_thread],))
        threads.append(thread)
        thread.start()

    for thread in threads:
        thread.join()
