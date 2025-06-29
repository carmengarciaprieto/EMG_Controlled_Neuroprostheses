package Processing;

import java.util.List;

public class ContractionResult {
    private final double[] envelope;
    private final double thresholdOn;
    private final double thresholdOff;
    private final List<Integer> onsets;
    private final List<Integer> offsets;

    public ContractionResult(double[] envelope, double thresholdOn, double thresholdOff, List<Integer> onsets, List<Integer> offsets) {
        this.envelope = envelope;
        this.thresholdOn = thresholdOn;
        this.thresholdOff = thresholdOff;
        this.onsets = onsets;
        this.offsets = offsets;
    }

    public double[] getEnvelope() {
        return envelope;
    }

    public double getThresholdOn() {
        return thresholdOn;
    }

    public double getThresholdOff() {
        return thresholdOff;
    }

    public List<Integer> getOnsets() {
        return onsets;
    }

    public List<Integer> getOffsets() {
        return offsets;
    }
}