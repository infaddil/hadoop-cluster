#!/usr/bin/env python
"""multithread_reducer.py"""

import sys
import threading

def reduce_lines(lines):
    current_word = None
    current_count = 0

    for line in lines:
        # remove leading and trailing whitespace
        line = line.strip()

        # parse the input we got from mapper.py
        word, count = line.split('\t', 1)

        # convert count (currently a string) to int
        try:
            count = int(count)
        except ValueError:
            # count was not a number, so silently
            # ignore/discard this line
            continue

        # this IF-switch only works because Hadoop sorts map output
        # by key (here: word) before it is passed to the reducer
        if current_word == word:
            current_count += count
        else:
            if current_word:
                # write result to STDOUT
                print '%s\t%s' % (current_word, current_count)
            current_count = count
            current_word = word

    # do not forget to output the last word if needed!
    if current_word == word:
        print '%s\t%s' % (current_word, current_count)

def reduce_lines_with_threads(input_lines):
    num_threads = 5
    lines_per_thread = len(input_lines) // num_threads
    threads = []

    for i in range(0, len(input_lines), lines_per_thread):
        thread = threading.Thread(target=reduce_lines, args=(input_lines[i:i+lines_per_thread],))
        threads.append(thread)
        thread.start()

    for thread in threads:
        thread.join()

if __name__ == "__main__":
    input_lines = sys.stdin.readlines()
    reduce_lines_with_threads(input_lines)
