#!/usr/bin/env python3

import matplotlib.pyplot as plt
import numpy as np

def visualize_output(output_file):
    """
    Reads the reducer output (lines like "2009,Urban,Bottom 40%\t1794.0"),
    gathers data for all years (2002..2019, etc.) and 6 combos:
      - Urban Top 20%, Urban Middle 40%, Urban Bottom 40%
      - Rural Top 20%, Rural Middle 40%, Rural Bottom 40%
    Then it draws a GROUPED BAR chart:
      - X-axis: years
      - 6 bars per year
    Saves to /tmp/urban_2019_income_pie_chart_fixed.png
    (keeping the original filename).
    """

    # We'll collect data as: data[(strata, category)][year] = income
    data = {}

    with open(output_file, 'r', encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if not line:
                continue

            try:
                # Example: "2002,Urban,Top 20%\t9085.0"
                key, avg_str = line.split('\t')
                avg_income = float(avg_str.strip())
            except ValueError:
                continue

            # Parse the key "2002,Urban,Top 20%"
            parts = key.split(',')
            if len(parts) == 3:
                year_str = parts[0].strip()
                strata_str = parts[1].strip()    # "Urban" or "Rural"
                category_str = parts[2].strip() # "Top 20%", "Middle 40%", or "Bottom 40%"

                # Convert year to int
                try:
                    year = int(year_str)
                except ValueError:
                    # If year isn't an integer, skip
                    continue

                # Store in our dictionary
                combo = (strata_str, category_str)
                if combo not in data:
                    data[combo] = {}
                data[combo][year] = avg_income

    # The 6 combos we want to show
    combos = [
        ("Urban", "Top 20%"),
        ("Urban", "Middle 40%"),
        ("Urban", "Bottom 40%"),
        ("Rural", "Top 20%"),
        ("Rural", "Middle 40%"),
        ("Rural", "Bottom 40%"),
    ]

    # Gather all the years from the data, sorted
    all_years = set()
    for combo_map in data.values():
        all_years.update(combo_map.keys())
    if not all_years:
        print("No valid data found in reducer output (check if file is empty).")
        return
    sorted_years = sorted(all_years)

    # Create a grouped bar chart
    # x-positions for each year
    x = np.arange(len(sorted_years))

    # Bar width (a fraction of 1.0 since we have 6 combos)
    bar_width = 0.12

    plt.figure(figsize=(12, 6))

    for i, (strata, category) in enumerate(combos):
        # For each combo, retrieve incomes in year order
        y_values = [data.get((strata, category), {}).get(year, 0) for year in sorted_years]

        # Shift each combo's bars horizontally by i*bar_width
        plt.bar(
            x + i * bar_width,
            y_values,
            bar_width,
            label=f"{strata} {category}"
        )

    # Label the X-axis with years in the center of each group
    # The shift is half of total combos * bar_width
    midpoint_shift = bar_width * (len(combos) - 1) / 2
    plt.xticks(x + midpoint_shift, sorted_years, rotation=45)

    # Chart labeling
    plt.xlabel("Year")
    plt.ylabel("Mean Monthly Household Gross Income (MYR)")
    plt.title("Mean Monthly Household Income by Strata & Category (All Years)")
    plt.grid(axis='y', linestyle='--', alpha=0.7)
    plt.legend()

    plt.tight_layout()

    # Save under the same old filename (though it's now a bar chart)
    plt.savefig("/tmp/urban_2019_income_pie_chart_fixed.png")
    print("Bar Chart saved as /tmp/urban_2019_income_pie_chart_fixed.png")

if __name__ == "__main__":
    # We keep using 'output.txt' as before
    visualize_output("output.txt")
