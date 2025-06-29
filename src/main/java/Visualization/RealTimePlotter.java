package Visualization;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class RealTimePlotter {

    private final XYSeries series;
    private final JFrame frame;
    private final XYPlot plot;
    private final int windowInSeconds = 10; // ventana visible de 10 segundos

    private final long startTime; // tiempo real de inicio en nanosegundos

    public RealTimePlotter(String title) {
        series = new XYSeries("EMG", true, false);
        XYSeriesCollection dataset = new XYSeriesCollection(series);

        JFreeChart chart = ChartFactory.createXYLineChart(
                title, "Tiempo (s)", "Señal EMG (mV)", dataset
        );
        chart.setAntiAlias(true);

        plot = chart.getXYPlot();
        plot.getRangeAxis().setAutoRange(false);
        plot.getRangeAxis().setRange(-4, 4);

        NumberAxis domain = (NumberAxis) plot.getDomainAxis();
        domain.setAutoRange(false);
        domain.setRange(0, windowInSeconds);
        domain.setTickUnit(new NumberTickUnit(1.0)); // ticks cada 1 segundo

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesShapesVisible(0, false);
        plot.setRenderer(renderer);

        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(new ChartPanel(chart), BorderLayout.CENTER);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);

        startTime = System.nanoTime(); // guardamos tiempo inicial
    }

    public void addDataPoint(double y) {
        SwingUtilities.invokeLater(() -> {
            long nowNs = System.nanoTime();
            double elapsedSec = (nowNs - startTime) / 1_000_000_000.0;

            try {
                series.add(elapsedSec, y);
            } catch (org.jfree.data.general.SeriesException e) {
                series.update(elapsedSec, y);
            }

            double start = Math.max(0, elapsedSec - windowInSeconds);
            double end = start + windowInSeconds;
            plot.getDomainAxis().setRange(start, end);

            while (series.getItemCount() > 0 && series.getX(0).doubleValue() < start) {
                series.remove(0);
            }
        });
    }

    public void close() {
        SwingUtilities.invokeLater(() -> frame.dispose());
    }

    public static void main(String[] args) {
        RealTimePlotter plotter = new RealTimePlotter("EMG Tiempo Real");

        Timer timer = new Timer();
        int updateRateMs = 10;

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                double y = 50 * Math.sin(2 * Math.PI * 1 * System.currentTimeMillis() / 1000.0) + Math.random() * 10;
                plotter.addDataPoint(y);
            }
        }, 0, updateRateMs);
    }
}
