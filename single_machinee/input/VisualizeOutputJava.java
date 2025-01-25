/*
 * VisualizeOutputJava.java
 *
 * Reads lines from "output.txt" of the form:
 *   year <tab> averageIncome
 * Then uses JFreeChart to plot them and saves "output.png".
 */

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.data.category.DefaultCategoryDataset;
import java.io.*;
import java.util.*;

public class VisualizeOutputJava {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java VisualizeOutputJava <output.txt>");
            System.exit(1);
        }
        String outputFile = args[0];
        // load lines
        List<Double> years = new ArrayList<>();
        List<Double> incomes = new ArrayList<>();

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
                    // skip
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }

        // create dataset for JFreeChart
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < years.size(); i++) {
            double y = years.get(i);
            double inc = incomes.get(i);
            dataset.addValue(inc, "Income", "" + y);
        }

        // create a line chart
        var chart = ChartFactory.createLineChart(
            "Mean Household Income over Years",
            "Year",
            "Income",
            dataset
        );

        // save as output.png
        try {
            ChartUtils.saveChartAsPNG(new File("output.png"), chart, 800, 600);
            System.out.println("Chart saved to output.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
