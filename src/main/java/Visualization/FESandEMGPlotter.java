/*package Visualization;

import org.knowm.xchart.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChartBuilder;
import org.jfree.data.xy.XYSeries;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.SwingWrapper;
import javax.swing.SwingUtilities;

import java.util.ArrayList;
import java.util.List;

public class FESandEMGPlotter {

    public static void plotEMGWithFESBinary(List<Double> emgValues, List<Long> fesTimestamps, List<Boolean> fesStates) {
        if (emgValues == null || emgValues.isEmpty() || fesTimestamps == null || fesStates == null) {
            System.err.println("❌ Datos insuficientes para generar el gráfico.");
            return;
        }

        // Crear eje X (índices)
        List<Integer> xData = new ArrayList<>();
        for (int i = 0; i < emgValues.size(); i++) {
            xData.add(i);
        }

        // Crear señal binaria FES
        List<Double> fesBinary = new ArrayList<>();
        int index = 0;
        boolean currentState = false;

        long startTime = fesTimestamps.get(0);
        long totalDuration = System.currentTimeMillis() - startTime;
        double msPerSample = (double) totalDuration / emgValues.size();

        for (int i = 0; i < emgValues.size(); i++) {
            long currentTime = startTime + (long)(i * msPerSample);

            // Cambiar estado si se supera un timestamp
            while (index < fesTimestamps.size() && currentTime >= fesTimestamps.get(index)) {
                currentState = fesStates.get(index);
                index++;
            }

            fesBinary.add(currentState ? 1.0 : 0.0);
        }

        // Crear gráfico
        XYChart chart = new XYChartBuilder()
                .width(1000)
                .height(500)
                .title("EMG + Señal FES Binaria")
                .xAxisTitle("Muestras")
                .yAxisTitle("EMG / FES")
                .build();

        chart.getStyler().setLegendVisible(true);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);

        chart.addSeries("EMG (mV)", xData, emgValues).setMarker(new None());
        chart.addSeries("FES ON/OFF", xData, fesBinary).setMarker(new None());

        SwingUtilities.invokeLater(() -> new SwingWrapper<>(chart).displayChart());
    }
}
*/