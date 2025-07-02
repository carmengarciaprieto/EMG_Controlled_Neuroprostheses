package Executable;

import BITalino.BITalino;
import BITalino.BITalinoException;
import BITalino.BitalinoDemo;
import static BITalino.BitalinoDemo.configureChannels;
import BITalino.Frame;
import static Executable.Main.FESCalibration;
import Processing.ContractionResult;
import Processing.EMGCalibration;
import Processing.EMGRealTimeProcessing;
import Processing.FESCalibration;
import Processing.FESControls;
import Utilities.Utilities;
import Visualization.RealTimePlotter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static ContractionResult result; // since main is static, all variables or methods called within main must be static
    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws Throwable {

        boolean exit = false;

        String user;
        System.out.println("Enter your name: ");
        user = sc.nextLine();
        while (!exit) {

            System.out.println("\n=== MAIN MENU ===");
            System.out.println("1. EMG Calibration");
            System.out.println("2. FES Calibration");
            System.out.println("3. Real-Time Analysis");
            System.out.println("4. Exit");
            System.out.print("Select an option (1-3): ");

            String option = sc.nextLine().trim();

            switch (option) {
                case "1":
                    calibration(user);
                    break;
                case "2":
                    FESCalibration();
                    break;
                case "3":
                    realTimeProcessing(user);
                    break;
                case "4":
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
                    break;
            }
        }
        System.out.println("Exiting program.");
        sc.close();
    }

    public static void calibration(String user) {
        try {

            ArrayList<Integer> data = createCalibrationRecording(user);

            if (data != null && !data.isEmpty()) {

                System.out.println("Calibration recording completed.");
                result = EMGCalibration.calculateThreshold(data, 1000);
                System.out.printf("Threshold ON: %.4f mV%n", result.getThresholdOn());
                System.out.printf("Threshold OFF: %.4f mV%n", result.getThresholdOff());

            } else {
                System.out.println("The recording was empty or cancelled.");
            }
        } catch (Throwable t) {
            System.err.println("Error during calibration: " + t.getMessage());
        }
    }

    public static ArrayList<Integer> createCalibrationRecording(String user) throws Throwable {
        System.out.println("=== Starting calibration recording ===");
        //String macAddress = Utilities.getValidMacAddress();
        String macAddress = "98:D3:C1:FD:2F:EC";
        int[] channelsToAcquire = BitalinoDemo.configureChannels();
        int sampleRate = 1000;

        BITalino bitalino = new BITalino();
        try {
            bitalino.open(macAddress, sampleRate);
        } catch (Exception e) {
            System.err.println("Could not connect to BITalino.");
            return null;
        }

        bitalino.start(channelsToAcquire);


        AtomicBoolean stopFlag = new AtomicBoolean(false);

        System.out.println("Recording... Press ENTER to stop.");
        Thread inputThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Scanner scanner = new Scanner(System.in);
                scanner.nextLine(); // wait for ENTER
                stopFlag.set(true);
            }
        });
        inputThread.start();

        // ⏳ Perform recording
        String date = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        ArrayList<Integer> data = BitalinoDemo.recordAndSaveData(bitalino, date, stopFlag, user);

        bitalino.stop();
        bitalino.close();
        return data;
    }

    public static void realTimeProcessing(String user) throws Exception {
        // String macAddress = Utilities.getValidMacAddress();
        String macAddress = "98:D3:C1:FD:2F:EC";
        int[] channelsToAcquire = BitalinoDemo.configureChannels();
        int sampleRate = 1000;

        BITalino bitalino = new BITalino();
        try {
            bitalino.open(macAddress, sampleRate);
        } catch (Exception e) {
            System.err.println("Could not connect to BITalino.");
            return;
        }

        try {
            bitalino.start(channelsToAcquire);
        } catch (Throwable ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Enter threshold ON (in mV): ");
        double thresholdOn = sc.nextDouble();
        System.out.println("Enter threshold OFF (in mV): ");
        double thresholdOff = sc.nextDouble();
        sc.nextLine(); // clear buffer
        System.out.println("Enter amplitude: ");
        double amplitude = sc.nextDouble();
        System.out.println("Enter frequency: ");
        double frequency = sc.nextDouble();
        System.out.println("Enter pulse width: ");
        double pulseWidth = sc.nextDouble();
        String port = "COM23";

        System.out.println("🔄 Starting real-time analysis...");

        RealTimePlotter plotter = new RealTimePlotter("Real-Time EMG");

        EMGRealTimeProcessing processor = new EMGRealTimeProcessing(200, thresholdOn, thresholdOff, port, amplitude, frequency, pulseWidth, user);
        AtomicBoolean stopFlag = new AtomicBoolean(false);

        System.out.println("Real-time processing... Press ENTER to stop.");
        Thread inputThread = new Thread(() -> {
            new Scanner(System.in).nextLine();
            stopFlag.set(true);
        });
        inputThread.start();

        final double VCC = 3.0;
        final double gain = 1000.0;  
        final int ADC_RESOLUTION = 1024;

        while (!stopFlag.get()) {
            try {
                Frame[] frames = bitalino.read(1);  // Read 1 frame

                if (frames == null || frames.length == 0) {
                    System.err.println("No frames received from BITalino.");
                    Thread.sleep(10);
                    continue;
                }

                Frame frame = frames[0];
                int rawEmg = BitalinoDemo.getEMGSignalValue(frame);

                // Convert rawEmg to mV including gain so scale matches thresholds
                double emgValueMv = (((rawEmg / (double) ADC_RESOLUTION) - 0.5) * VCC / gain) * 1000.0;

                if (Double.isNaN(emgValueMv) || Double.isInfinite(emgValueMv) || Math.abs(emgValueMv) > 50) {
                    // Discard absurd values (±50 mV is more reasonable)
                    continue;
                }

                processor.addDataPoint(emgValueMv);
                plotter.addDataPoint(emgValueMv);

            } catch (Exception e) {
                System.err.println("⚠️ Error reading from BITalino: " + e.getMessage());
                e.printStackTrace();
            }
        }

        processor.shutDownFES();

        processor.saveFESEvents("C:\\Users\\user\\Downloads\\TFG\\FES_recordings", "C:\\Users\\user\\Downloads\\TFG\\EMG_signals");

        bitalino.stop();
        bitalino.close();

        System.out.println("Session ended.");
    }

    public static void FESCalibration() {
        try {
            System.out.println("Enter amplitude: ");
            double amplitude = sc.nextDouble();
            System.out.println("Enter frequency: ");
            double frequency = sc.nextDouble();
            System.out.println("Enter pulse width: ");
            double pulseWidth = sc.nextDouble();
            String port = "COM23";
            FESCalibration.calibration(port, amplitude, frequency, pulseWidth);
        } catch (Throwable t) {
            System.err.println("Error during calibration: " + t.getMessage());
        }
    }
}