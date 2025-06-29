package Visualization;

import Processing.EMGCalibration;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.xy.*;

import javax.swing.*;

public class EMGFrequencyPlotter {

    public static ChartPanel createChart(double[] signal, int fs, String title) {
        // FFT y eje de frecuencias
        double[] fft = EMGCalibration.computeFFT(signal, fs);
        double[] freqs = EMGCalibration.generateFrequencies(fft.length * 2, fs); // fft = N/2

        XYSeries series = new XYSeries("FFT");
        for (int i = 0; i < fft.length; i++) {
            if (!Double.isNaN(fft[i]) && !Double.isInfinite(fft[i])) {
                series.add(freqs[i], fft[i]);
            }
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