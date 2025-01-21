#!/usr/bin/env python3
import sys

# Read input line by line
for line in sys.stdin:
    data = line.strip().split(',')

    # Skip the header row by looking at the first column
    # (since your first column is "Strata")
    if data[0].strip() == "Strata":
        continue

    try:
        strata = data[0].strip()        # "Urban", "Rural", etc.
        category = data[1].strip()      # "Top 20%", "Middle 40%", ...
        year = data[2].strip()          # "2019", ...
        monthly_income = float(data[3].strip())  # e.g. 19910

        # Emit a key that includes Year, Strata, and Category
        # so you can later parse "2019,Urban,Top 20%", etc.
        print(f"{year},{strata},{category}\t{monthly_income}")

        # Optionally, also emit a partial key if you want aggregated by Category alone
        #print(f"{category}\t{monthly_income}")
    except (ValueError, IndexError):
        # Skip lines with bad or incomplete data
        continue
