package Visualization;

import Processing.EMGCalibration;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.xy.*;

import javax.swing.*;
import java.util.Arrays;

public class BandpassFilterPlotter {

    public static ChartPanel createChart(double[] signal, int fs, String title) {
        double[] fft = EMGCalibration.computeFFT(signal, fs);
        double[] freqs = EMGCalibration.generateFrequencies(fft.length * 2, fs);

        // Depuración
        System.out.println("🔎 FFT (bandpass) - primeros valores:");
        for (int i = 0; i < Math.min(10, fft.length); i++) {
            System.out.println(freqs[i] + " Hz → " + fft[i]);
        }

        XYSeries series = new XYSeries("FFT Bandpass");
        boolean hasValidData = false;
        double maxVal = 0;

        for (int i = 0; i < fft.length; i++) {
            double y = fft[i];
            if (!Double.isNaN(y) && !Double.isInfinite(y)) {
                hasValidData = true;
                maxVal = Math.max(maxVal, y);
                series.add(freqs[i], y);
            }
        }

        if (!hasValidData || maxVal == 0) {
            System.out.println("⚠️ FFT contiene solo ceros o NaN. Nada que graficar.");
            return null;
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                title, "Frecuencia (Hz)", "Amplitud", dataset,
                PlotOrientation.VERTICAL, true, true, false
        );

        XYPlot plot = chart.getXYPlot();
        plot.getRangeAxis().setRange(0.0, maxVal * 1.2);

        return new ChartPanel(chart);
    }
}