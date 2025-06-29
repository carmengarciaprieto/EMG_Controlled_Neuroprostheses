package Visualization;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.xy.*;

import javax.swing.*;

public class EMGTimePlotter {

    public static ChartPanel createChart(double[] signal, int fs, String title) {
        XYSeries series = new XYSeries("Señal EMG");
        for (int i = 0; i < signal.length; i++) {
            series.add(i / (double) fs, signal[i]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
            title,
            "Tiempo (s)",
            "mV",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );

        return new ChartPanel(chart);
    }
}