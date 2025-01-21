#!/usr/bin/env python3

import matplotlib.pyplot as plt

def visualize_output(output_file):
    income_by_category = {}

    with open(output_file, 'r', encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if not line:
                continue

            try:
                # Example line: "2019,Urban,Bottom 40%\t3454.0"
                key, avg_str = line.split('\t')  # split on tab
                avg_income = float(avg_str.strip())

                # Now parse the key: "2019,Urban,Bottom 40%"
                parts = key.split(',')
                if len(parts) == 3:
                    year_str = parts[0].strip()
                    strata_str = parts[1].strip()
                    category_str = parts[2].strip()

                    # Compare with "2019" and "Urban"
                    if year_str == "2019" and strata_str == "Urban":
                        income_by_category[category_str] = avg_income

            except ValueError:
                continue

    # If no data matched year=2019 + strata=Urban
    if not income_by_category:
        print("No 'Urban, 2019' data found in reducer output.")
        return

    # Plot Pie Chart
    labels = list(income_by_category.keys())
    sizes = [income_by_category[cat] for cat in labels]

    plt.figure(figsize=(6, 6))
    plt.pie(sizes, labels=labels, autopct='%1.1f%%', startangle=140)
    plt.title("Urban 2019 Income Distribution (B40, M40, T20)")
    plt.axis('equal')
    plt.savefig("/tmp/urban_2019_income_pie_chart_fixed.png")
    print("Pie Chart saved as /tmp/urban_2019_income_pie_chart_fixed.png")


if __name__ == "__main__":
    # Make sure we point to the same 'output.txt'
    visualize_output("output.txt")
