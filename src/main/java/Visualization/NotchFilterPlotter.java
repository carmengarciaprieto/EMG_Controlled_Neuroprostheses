package Visualization;

import Processing.EMGCalibration;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.xy.*;

import javax.swing.*;

public class NotchFilterPlotter {

    public static ChartPanel createChart(double[] signal, int fs, String title) {
        double[] fft = EMGCalibration.computeFFT(signal, fs);
        double[] freqs = EMGCalibration.generateFrequencies(fft.length * 2, fs);

        XYSeries series = new XYSeries("FFT Notch");
        for (int i = 0; i < fft.length; i++) {
            series.add(freqs[i], fft[i]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
            title,
            "Frecuencia (Hz)",
            "Amplitud",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );

        return new ChartPanel(chart);
    }
}