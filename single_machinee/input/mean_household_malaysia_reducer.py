#!/usr/bin/python3
import sys

def reducer():
    current_key = None
    current_sum = 0
    current_count = 0
    
    print(f"DEBUG: Starting reducer", file=sys.stderr)

    for line in sys.stdin:
        print(f"DEBUG: Processing line: {line.strip()}", file=sys.stderr)  # Debug input lines
        line = line.strip()
        if not line:
            continue
        
        try:
            key, value = line.split("\t")
            value = float(value)
        except ValueError:
            print(f"DEBUG: Skipping malformed line: {line}", file=sys.stderr)
            continue

        if current_key == key:
            current_sum += value
            current_count += 1
        else:
            if current_key:
                print(f"{current_key}\t{current_sum / current_count}")
                print(f"DEBUG: Output key={current_key}, avg={current_sum / current_count}", file=sys.stderr)
            current_key = key
            current_sum = value
            current_count = 1

    if current_key:
        print(f"{current_key}\t{current_sum / current_count}")
        print(f"DEBUG: Final output key={current_key}, avg={current_sum / current_count}", file=sys.stderr)

if __name__ == "__main__":
    reducer()
