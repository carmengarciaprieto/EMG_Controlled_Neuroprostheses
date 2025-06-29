package Visualization;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;

public class FilteredTimePlotter {

    public static ChartPanel createChart(double[] envelope, double thresholdOn, double thresholdOff, int fs, String title) {
        XYSeries series = new XYSeries("Envolvente EMG");
        XYSeries threshOn = new XYSeries("Umbral Onset");
        XYSeries threshOff = new XYSeries("Umbral Offset");

        for (int i = 0; i < envelope.length; i++) {
            double t = i / (double) fs;
            series.add(t, envelope[i]);
            threshOn.add(t, thresholdOn);
            threshOff.add(t, thresholdOff);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        dataset.addSeries(threshOn);
        dataset.addSeries(threshOff);

        JFreeChart chart = ChartFactory.createXYLineChart(
            title,
            "Tiempo (s)",
            "Amplitud (mV)",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.RED);         // Envolvente
        renderer.setSeriesPaint(1, Color.BLUE);        // Threshold Onset
        renderer.setSeriesPaint(2, Color.ORANGE);      // Threshold Offset
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesShapesVisible(1, false);
        renderer.setSeriesShapesVisible(2, false);
        plot.setRenderer(renderer);

        return new ChartPanel(chart);
    }
}