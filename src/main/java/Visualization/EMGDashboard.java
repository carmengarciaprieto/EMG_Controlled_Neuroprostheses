package Visualization;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;

import javax.swing.*;
import java.awt.*;

public class EMGDashboard {

    private static final JTabbedPane tabs = new JTabbedPane();
    private static JFrame frame;

    public static void addChart(String title, ChartPanel chartPanel) {
        tabs.addTab(title, chartPanel);
    }

    public static void addChart(double[] signal, int fs, String title, boolean isFrequency) {
        XYSeries series = new XYSeries(title);

        if (isFrequency) {
            double[] fft = Processing.EMGCalibration.computeFFT(signal, fs);
            double[] freqs = Processing.EMGCalibration.generateFrequencies(fft.length * 2, fs);
            for (int i = 0; i < fft.length; i++) {
                series.add(freqs[i], fft[i]);
            }
        } else {
            for (int i = 0; i < signal.length; i++) {
                series.add(i / (double) fs, signal[i]);
            }
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                isFrequency ? "Frecuencia (Hz)" : "Tiempo (s)",
                isFrequency ? "Amplitud" : "mV",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        tabs.addTab(title, chartPanel);
    }

    public static void addChart(double[] signal, int fs, String title) {
        addChart(signal, fs, title, false);
    }

    public static void addEnvelopeChart(double[] envelope, double thresholdOn, double thresholdOff, int fs, String title) {
        XYSeries envSeries = new XYSeries("Envolvente");
        XYSeries thOnSeries = new XYSeries("Umbral Onset");
        XYSeries thOffSeries = new XYSeries("Umbral Offset");

        for (int i = 0; i < envelope.length; i++) {
            double t = i / (double) fs;
            envSeries.add(t, envelope[i]);
            thOnSeries.add(t, thresholdOn);
            thOffSeries.add(t, thresholdOff);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(envSeries);
        dataset.addSeries(thOnSeries);
        dataset.addSeries(thOffSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                title, "Tiempo (s)", "Amplitud", dataset,
                PlotOrientation.VERTICAL, true, true, false
        );

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        renderer.setSeriesPaint(0, Color.RED);      // Envolvente
        renderer.setSeriesPaint(1, Color.BLUE);     // Umbral Onset
        renderer.setSeriesPaint(2, Color.ORANGE);   // Umbral Offset

        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesShapesVisible(1, false);
        renderer.setSeriesShapesVisible(2, false);

        plot.setRenderer(renderer);

        ChartPanel panel = new ChartPanel(chart);
        tabs.addTab(title, panel);
    }

    public static void showAll() {
        SwingUtilities.invokeLater(() -> {
            if (frame == null) {
                frame = new JFrame("EMG Dashboard");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setSize(1000, 600);
                frame.setContentPane(tabs);
                frame.setLocationRelativeTo(null); // centra la ventana
            }
            frame.setVisible(true);
            frame.setExtendedState(JFrame.NORMAL);   // asegurar no minimizada
            frame.setAlwaysOnTop(true);               // poner arriba
            frame.toFront();
            frame.requestFocus();
            frame.setAlwaysOnTop(false);              // quitar siempre arriba
            frame.repaint();
        });
    }

    public static void clear() {
        tabs.removeAll();
    }
}
