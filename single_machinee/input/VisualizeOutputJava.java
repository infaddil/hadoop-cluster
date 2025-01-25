/*
 * VisualizeOutputJava.java
 *
 * Reads lines from a file "year\taverageIncome",
 * creates a line chart with JFreeChart, saves to "output.png".
 *
 * Java 8 compatible (no "var" usage).
 */

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class VisualizeOutputJava {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java VisualizeOutputJava <output.txt>");
            System.exit(1);
        }
        String outputFile = args[0];

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // read lines "year\tvalue"
        try (BufferedReader br = new BufferedReader(new FileReader(outputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\t");
                if (parts.length < 2) continue;
                double year, income;
                try {
                    year = Double.parseDouble(parts[0]);
                    income = Double.parseDouble(parts[1]);
                } catch (NumberFormatException e) {
                    continue;
                }
                dataset.addValue(income, "Income", String.valueOf((int)year));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // create line chart
        JFreeChart chart = ChartFactory.createLineChart(
                "Mean Household Income Over Years",
                "Year",
                "Income",
                dataset
        );

        try {
            ChartUtils.saveChartAsPNG(new File("output.png"), chart, 800, 600);
            System.out.println("Saved chart to output.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
