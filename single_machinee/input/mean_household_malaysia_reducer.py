#!/usr/bin/python3
import sys

def reducer():
    current_key = None
    current_sum = 0
    current_count = 0

    # Print debug messages to stderr using standard formatting
    print("DEBUG: Starting reducer", file=sys.stderr)

    for line in sys.stdin:
        print("DEBUG: Processing line: " + line.strip(), file=sys.stderr)  # Debug input lines
        line = line.strip()
        if not line:
            continue

        try:
            key, value = line.split("\t")
            value = float(value)
        except ValueError:
            print("DEBUG: Skipping malformed line: " + line, file=sys.stderr)
            continue

        if current_key == key:
            current_sum += value
            current_count += 1
        else:
            if current_key:
                print("{}\t{}".format(current_key, current_sum / current_count))
                print("DEBUG: Output key={}, avg={}".format(current_key, current_sum / current_count), file=sys.stderr)
            current_key = key
            current_sum = value
            current_count = 1

    if current_key:
        print("{}\t{}".format(current_key, current_sum / current_count))
        print("DEBUG: Final output key={}, avg={}".format(current_key, current_sum / current_count), file=sys.stderr)

if __name__ == "__main__":
    reducer()
