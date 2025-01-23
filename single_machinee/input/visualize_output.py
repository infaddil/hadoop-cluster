#!/usr/bin/env python3

import csv
import matplotlib.pyplot as plt

def visualize_income_time_series(csv_file):
    """
    Reads a CSV with columns:
        Strata, Category, Year, Mean Monthly Household Gross Income
    and plots a line chart from 2002 to 2019 showing Urban & Rural
    incomes for Top 20%, Middle 40%, and Bottom 40% categories.
    """
    # Dictionary to hold data in the form:
    #   data[(strata, category)][year] = mean_income
    data = {}

    # Read the CSV using DictReader so column headers match automatically
    with open(csv_file, 'r', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        for row in reader:
            try:
                strata = row["Strata"].strip()
                category = row["Category"].strip()
                year_str = row["Year"].strip()
                income_str = row["Mean Monthly Household Gross Income"].strip()

                year = int(year_str)
                income = float(income_str)

                # Store in a nested dictionary keyed by (strata, category)
                key = (strata, category)
                if key not in data:
                    data[key] = {}
                data[key][year] = income

            except (ValueError, KeyError):
                # Skip lines that don't parse correctly or have missing columns
                continue

    # We want to plot 6 possible lines: (Urban, Top 20%), (Urban, Middle 40%), etc.
    combos = [
        ("Urban", "Top 20%"),
        ("Urban", "Middle 40%"),
        ("Urban", "Bottom 40%"),
        ("Rural", "Top 20%"),
        ("Rural", "Middle 40%"),
        ("Rural", "Bottom 40%")
    ]

    # Collect all the years that appear in your data, then sort them
    all_years = set()
    for combo_dict in data.values():
        all_years.update(combo_dict.keys())
    all_years = sorted(all_years)  # e.g. [2002, 2004, 2007, 2009, ...]

    # Create a figure and plot
    plt.figure(figsize=(10, 6))

    for (strata, category) in combos:
        # For each year in sorted order, look up the income (or None if missing)
        y_values = [data.get((strata, category), {}).get(y, None) for y in all_years]

        # Plot this line if at least some data is present
        plt.plot(
            all_years,
            y_values,
            marker='o',
            label="{} {}".format(strata, category)

        )

    # Labeling and layout
    plt.xlabel("Year")
    plt.ylabel("Mean Monthly Household Income (MYR)")
    plt.title("Mean Monthly Household Income by Strata & Category (2002–2019)")
    plt.grid(True, axis='y', linestyle='--', alpha=0.5)
    plt.legend()
    plt.tight_layout()

    # Save the figure
    out_file = "/tmp/mean_household_income_timeseries.png"
    plt.savefig(out_file)
    print("Saved chart to {}".format(out_file))

if __name__ == "__main__":
    # Point to your updated CSV. Adjust the path or filename as needed.
    visualize_income_time_series("my_updated_income_data.csv")
