package Processing;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import uk.me.berndporr.iirj.Butterworth;

public class EMGRealTimeProcessing {

    private final int windowSize;         
    private final double thresholdOn;     
    private final double thresholdOff;   
    private final double amplitude;
    private final double frequency;
    private final double pulseWidth;
    private final String user;
    private long startTime = -1;
    private final int warmUpMillis = 5000; 

    private final LinkedList<Double> buffer = new LinkedList<>();
    private boolean fesActive = false;
    private FESControls fes;
    private final List<Long> emgTimestamps = new ArrayList<>();
    private final List<Long> fesTimestamps = new ArrayList<>();
    private final List<Boolean> fesStates = new ArrayList<>();
    private final List<Double> emgValues = new ArrayList<>();

    private Butterworth notchFilter;
    private Butterworth bandpassFilter;
    private final int fs = 1000; 

    public EMGRealTimeProcessing(int windowSize, double thresholdOn, double thresholdOff, String port, double amplitude, double frequency, double pulseWidth, String user) {
        this.windowSize = windowSize;
        this.thresholdOn = thresholdOn;
        this.thresholdOff = thresholdOff;
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.pulseWidth = pulseWidth;
        this.user = user;


        notchFilter = new Butterworth();
        notchFilter.bandStop(4, fs, 50.0, 0.8);   

        bandpassFilter = new Butterworth();
        bandpassFilter.bandPass(4, fs, 20.0, 450.0);  


        fes = new FESControls(port, amplitude, frequency, pulseWidth);
    }

    public void addDataPoint(double value) {
        if (startTime < 0) {
            startTime = System.currentTimeMillis();
        }

        long elapsed = System.currentTimeMillis() - startTime;

        // Filter signal with Notch and Bandpass
        double filteredNotch = notchFilter.filter(value);
        double filteredBandpass = bandpassFilter.filter(filteredNotch);

        double absValue = Math.abs(filteredBandpass);

        // Add absolute filtered value to buffer
        buffer.add(absValue);
        if (buffer.size() > windowSize) {
            buffer.removeFirst();
        }

        // Ignore data during warm-up period
        if (elapsed < warmUpMillis) {
            return;
        }

        // Wait until buffer is full before processing
        if (buffer.size() < windowSize) {
            return;
        }

        // Compute simple moving average (envelope)
        double sum = 0;
        for (double v : buffer) {
            sum += v;
        }
        double envelope = sum / buffer.size();


        emgValues.add(envelope);

 
        if (!fesActive && envelope > thresholdOn) {
            long emgTime = System.currentTimeMillis();
            emgTimestamps.add(emgTime);
            activateFES();
            fesActive = true;


            fesTimestamps.add(emgTime);
            fesStates.add(true);

        } else if (fesActive && envelope < thresholdOff) {
            long emgTime = System.currentTimeMillis();
            emgTimestamps.add(emgTime);
            deactivateFES();
            fesActive = false;


            fesTimestamps.add(emgTime);
            fesStates.add(false);
        }
    }

    public void activateFES() {
        fes.startStimulation();
        System.out.println("FES ACTIVATED (" + System.currentTimeMillis() + " ms)");
    }

    public void deactivateFES() {
        fes.stopStimulation();
        System.out.println("FES DEACTIVATED (" + System.currentTimeMillis() + " ms)");
    }

    public void shutDownFES() {
        fes.stopStimulation();
        fes.powerOff();
        fes.disconnect();
    }

    public void saveFESEvents(String eventFolderPath, String signalFolderPath) {
        try {
            // Create events folder (CSV)
            File eventFolder = new File(eventFolderPath);
            if (!eventFolder.exists()) {
                eventFolder.mkdirs();
            }

            // Create signal folder (TXT)
            File signalFolder = new File(signalFolderPath);
            if (!signalFolder.exists()) {
                signalFolder.mkdirs();
            }

            String dateTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String baseName = user + "_" + dateTime;


            File eventsFile = new File(eventFolder, baseName + ".csv");
            try (PrintWriter writer = new PrintWriter(eventsFile)) {
                writer.println("emg_timestamp_ms;fes_timestamp_ms;state");
                for (int i = 0; i < fesTimestamps.size(); i++) {
                    String state = fesStates.get(i) ? "ON" : "OFF";
                    writer.println(emgTimestamps.get(i) + ";" + fesTimestamps.get(i) + ";" + state);
                }
            }


            File signalFile = new File(signalFolder, baseName + ".txt");
            try (PrintWriter writer = new PrintWriter(signalFile)) {
                for (Double val : emgValues) {
                    writer.println(val);
                }
            }

            System.out.println("✅ Events saved to: " + eventsFile.getAbsolutePath());
            System.out.println("✅ Signal saved to: " + signalFile.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("❌ Error saving files: " + e.getMessage());
        }
    }

    public List<Double> getEmgValues() {
        return emgValues;
    }

    public List<Long> getFesTimestamps() {
        return fesTimestamps;
    }

    public List<Boolean> getFesStates() {
        return fesStates;
    }
}