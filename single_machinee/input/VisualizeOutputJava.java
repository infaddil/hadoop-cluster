/*
 * VisualizeOutputJava.java (Corrected for Java 8)
 *
 * Reads lines from "output.txt" of form "year\t averageIncome"
 * Then uses JFreeChart line chart, saves "output.png".
 */

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class VisualizeOutputJava {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java VisualizeOutputJava <output.txt>");
            System.exit(1);
        }
        String outputFile = args[0];

        ArrayList<Double> years = new ArrayList<>();
        ArrayList<Double> incomes = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(outputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\t");
                if (parts.length < 2) continue;
                try {
                    double year = Double.parseDouble(parts[0]);
                    double income = Double.parseDouble(parts[1]);
                    years.add(year);
                    incomes.add(income);
                } catch (NumberFormatException e) {
                    // skip lines that aren't "double\t double"
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }

        // Build dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < years.size(); i++) {
            double yr = years.get(i);
            double inc = incomes.get(i);
            dataset.addValue(inc, "Income", String.valueOf(yr));
        }

        // Create chart
        JFreeChart chart = ChartFactory.createLineChart(
                "Mean Household Income over Years",
                "Year",
                "Income",
                dataset
        );

        // Save chart
        try {
            ChartUtils.saveChartAsPNG(new File("output.png"), chart, 800, 600);
            System.out.println("Chart saved to output.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
