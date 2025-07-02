package Processing;

import Visualization.*;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.SwingUtilities;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.*;
import uk.me.berndporr.iirj.Butterworth;

public class EMGCalibration {

    public static ContractionResult calculateThreshold(ArrayList<Integer> rawData, int fs) { // static means the method can be called without creating an instance of the class
        int[] raw = new int[rawData.size()];
        for (int i = 0; i < rawData.size(); i++) {
            raw[i] = rawData.get(i);
        }

        double[] mv = convertToMillivolts(raw);
        double[] notch = applyNotchFilter(mv, fs, 50.0, 0.8);
        double[] filtered = applyBandpassFilter(notch, fs);

        // Remove NaN (Not a Number) or infinite values
        for (int i = 0; i < filtered.length; i++) {
            if (Double.isNaN(filtered[i]) || Double.isInfinite(filtered[i])) {
                filtered[i] = 0;
            }
        }

        double[] envelope = smoothEnvelope(filtered, 25);

        ContractionResult result = detectContractions(envelope, fs, 0.25, 0.15, 0.2);

        showCalibrationCharts(mv, notch, filtered, result, fs);

        return result;
    }

    public static double[] convertToMillivolts(int[] rawSignal) {
        double VCC = 3.0;
        int n = 10;
        double gain = 500.0;
        double[] mv = new double[rawSignal.length];

        for (int i = 0; i < rawSignal.length; i++) {
            mv[i] = ((rawSignal[i] / Math.pow(2, n) - 0.5) * VCC / gain) * 1000;
        }

        return mv;
    }

    public static double[] computeFFT(double[] signal, int fs) {
        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);

        int N = nextPowerOfTwo(signal.length);

        double[] padded = new double[N];
        System.arraycopy(signal, 0, padded, 0, signal.length);

        Complex[] fftResult = fft.transform(padded, TransformType.FORWARD);

        double[] magnitude = new double[N / 2];
        for (int i = 0; i < N / 2; i++) {
            magnitude[i] = 2.0 / signal.length * fftResult[i].abs();
        }

        return magnitude;
    }

    // Generates the frequency axis for N samples and sampling rate fs
    public static double[] generateFrequencies(int N, int fs) {
        double[] freqs = new double[N / 2];
        for (int i = 0; i < freqs.length; i++) {
            freqs[i] = i * fs / (double) N;
        }
        return freqs;
    }

    // Calculates the next power of two greater than n
    private static int nextPowerOfTwo(int n) {
        int pow = 1;
        while (pow < n) {
            pow *= 2;
        }
        return pow;
    }

    public static double[] applyNotchFilter(double[] signal, int fs, double f0, double bandwidth) {
        Butterworth notch = new Butterworth();
        notch.bandStop(4, fs, f0, bandwidth);

        double[] filtered = new double[signal.length];
        for (int i = 0; i < signal.length; i++) {
            filtered[i] = notch.filter(signal[i]);

            if (Double.isNaN(filtered[i]) || Double.isInfinite(filtered[i])) {
                System.out.println("⚠️ Notch NaN detected at index " + i);
                filtered[i] = 0;
            }
        }

        return filtered;
    }

    public static double[] applyBandpassFilter(double[] signal, int fs) {
        Butterworth bandpass = new Butterworth();
        bandpass.bandPass(4, fs, 20.0, 450.0);

        double[] filtered = new double[signal.length];
        for (int i = 0; i < signal.length; i++) {
            filtered[i] = bandpass.filter(signal[i]);
        }

        return filtered;
    }

    public static double[] smoothEnvelope(double[] signal, int windowSize) {
        int n = signal.length;
        double[] envelope = new double[n];

        for (int i = 0; i < n; i++) {
            double sum = 0;
            int count = 0;
            for (int j = -windowSize; j <= windowSize; j++) {
                int idx = i + j;
                if (idx >= 0 && idx < n) {
                    sum += Math.abs(signal[idx]);
                    count++;
                }
            }
            envelope[i] = sum / count;
        }
        return envelope;
    }

    public static ContractionResult detectContractions(double[] envelope, int fs, double kOn, double kOff, double minDurationSec) {
        double mean = Arrays.stream(envelope).average().orElse(0);
        double std = Math.sqrt(Arrays.stream(envelope).map(v -> Math.pow(v - mean, 2)).average().orElse(0));
        double thresholdOn = mean + kOn * std;
        double thresholdOff = mean + kOff * std;

        boolean inside = false;
        ArrayList<Integer> onsets = new ArrayList<>();
        ArrayList<Integer> offsets = new ArrayList<>();

        for (int i = 0; i < envelope.length; i++) {
            if (!inside && envelope[i] > thresholdOn) {
                inside = true;
                onsets.add(i);
            } else if (inside && envelope[i] < thresholdOff) {
                inside = false;
                offsets.add(i);
            }
        }

        if (!onsets.isEmpty() && !offsets.isEmpty() && offsets.get(0) < onsets.get(0)) {
            offsets.remove(0);
        }
        if (onsets.size() > offsets.size()) {
            onsets = new ArrayList<>(onsets.subList(0, offsets.size()));
        }

        int minSamples = (int) (minDurationSec * fs);
        ArrayList<Integer> validOnsets = new ArrayList<>();
        ArrayList<Integer> validOffsets = new ArrayList<>();
        for (int i = 0; i < onsets.size(); i++) {
            if (offsets.get(i) - onsets.get(i) >= minSamples) {
                validOnsets.add(onsets.get(i));
                validOffsets.add(offsets.get(i));
            }
        }

        return new ContractionResult(envelope, thresholdOn, thresholdOff, validOnsets, validOffsets);
    }

    public static void showCalibrationCharts(double[] mv, double[] notch, double[] filtered, ContractionResult result, int fs) {
        SwingUtilities.invokeLater(() -> {
            EMGDashboard.clear();
            EMGDashboard.addChart(mv, fs, "EMG - Raw Signal in Time Domain");
            EMGDashboard.addChart(mv, fs, "EMG - Raw Spectrum", true);
            EMGDashboard.addChart(notch, fs, "EMG - Spectrum After 50 Hz Notch", true);
            EMGDashboard.addChart(filtered, fs, "EMG - Spectrum After Bandpass 20–450 Hz", true);
            EMGDashboard.addEnvelopeChart(result.getEnvelope(), result.getThresholdOn(), result.getThresholdOff(), fs, "EMG - Envelope + Thresholds");
            EMGDashboard.showAll();
        });
    }
}