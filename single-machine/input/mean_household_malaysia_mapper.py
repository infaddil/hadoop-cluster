#!/usr/bin/env python3
import sys

# Read input line by line
for line in sys.stdin:
    # Split the line into columns
    data = line.strip().split(',')

    # Skip the header row
    if data[0] == "Year":
        continue

    try:
        year = data[0].strip()  # Year
        category = data[1].strip()  # Category (Urban/Rural, etc.)
        monthly_income = float(data[2].strip())  # Monthly income

        # Emit key-value pairs for year-category and category
        print(f"{year},{category}\t{monthly_income}")
        print(f"{category}\t{monthly_income}")
    except ValueError:
        # Skip lines with bad data
        continue
