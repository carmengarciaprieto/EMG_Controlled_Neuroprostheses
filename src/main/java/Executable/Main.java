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

    public static void main(String[] args) throws Throwable {
        Scanner sc = new Scanner(System.in);
        boolean salir = false;
        double thresholdOn;
        double thresholdOff;
        String usuario;
        System.out.println("Dime tu nombre: ");
        usuario = sc.nextLine();
        while (!salir) {

            System.out.println("\n=== MENÚ PRINCIPAL ===");
            System.out.println("1. Calibración de EMG");
            System.out.println("2. Calibracion FES");
            System.out.println("3. Análisis en tiempo real");
            System.out.println("4. Salir");
            System.out.print("Selecciona una opción (1-3): ");

            String opcion = sc.nextLine().trim();

            switch (opcion) {
                //case "1":
                /* try {

                        ArrayList<Integer> data = createCalibrationRecording(usuario, sc);

                        if (data != null && !data.isEmpty()) {

                            System.out.println("✅ Grabación de calibración completada.");
                            ContractionResult result = EMGCalibration.calculateThreshold(data, 1000);
                            thresholdOn = result.getThresholdOn();
                            thresholdOff = result.getThresholdOff();
                            System.out.printf("📊 Threshold ON: %.4f mV%n", thresholdOn);
                            System.out.printf("📊 Threshold OFF: %.4f mV%n", thresholdOff);
                            System.out.println("Quiere realizar el análisis en tiempo real? Escribe YES/NO \n");
                            String answer = sc.nextLine().toUpperCase();

                            if (answer.equals("YES")) {
                                realTimeProcessing(thresholdOn, thresholdOff, usuario, sc);
                            }

                        } else {
                            System.out.println("❌ La grabación fue vacía o se canceló.");
                        }
                    } catch (Throwable t) {
                        System.err.println("❌ Error durante la calibración: " + t.getMessage());
                    }
                    break;*/
                case "1":
                    try {
                        // Leer archivo directamente
                        String filePath = "/Users/carmengarciaprieto/Downloads/TFGDEFINITIVO/calibration_recordings/EMG_carmen_2025-06-23_11-48-48_DEFINITIVO.txt.txt";
                        ArrayList<Integer> data = new ArrayList<>();

                        try (Scanner fileScanner = new Scanner(new java.io.File(filePath))) {
                            while (fileScanner.hasNextLine()) {
                                String line = fileScanner.nextLine().trim();
                                if (!line.isEmpty()) {
                                    try {
                                        data.add(Integer.parseInt(line));
                                    } catch (NumberFormatException e) {
                                        System.err.println("⚠️ Línea no válida en el archivo: " + line);
                                    }
                                }
                            }
                        }

                        if (!data.isEmpty()) {
                            System.out.println("✅ Datos de calibración cargados desde el archivo.");
                            ContractionResult result = EMGCalibration.calculateThreshold(data, 1000);
                            thresholdOn = result.getThresholdOn();
                            thresholdOff = result.getThresholdOff();
                            System.out.printf("📊 Threshold ON: %.4f mV%n", thresholdOn);
                            System.out.printf("📊 Threshold OFF: %.4f mV%n", thresholdOff);
                            System.out.println("¿Quiere realizar el análisis en tiempo real? Escribe YES/NO \n");
                            String answer = sc.nextLine().toUpperCase();

                            if (answer.equals("YES")) {
                                realTimeProcessing(thresholdOn, thresholdOff, usuario, sc);
                            }

                        } else {
                            System.out.println("❌ El archivo no contenía datos válidos.");
                        }

                    } catch (Throwable t) {
                        System.err.println("❌ Error durante la calibración: " + t.getMessage());
                    }
                    break;
                case "2":
                    try {
                        FESCalibration(sc);
                    } catch (Throwable t) {
                        System.err.println("❌ Error durante la calibración: " + t.getMessage());
                    }
                    break;

                case "3":

                    System.out.println("Introduce el threshold ON (en mV): ");
                    thresholdOn = sc.nextDouble();
                    System.out.println("Introduce el threshold OFF (en mV): ");
                    thresholdOff = sc.nextDouble();
                    sc.nextLine(); // limpiar buffer
                    realTimeProcessing(thresholdOn, thresholdOff, usuario, sc);

                    break;

                case "4":
                    System.out.println("👋 Saliendo del programa.");
                    salir = true;
                    break;

                default:
                    System.out.println("❌ Opción no válida. Intenta de nuevo.");
                    break;
            }
        }

        sc.close();
    }

    public static ArrayList<Integer> createCalibrationRecording(String usuario, Scanner sc) throws Throwable {
        System.out.println("=== Iniciando grabación de calibración ===");
        //String macAddress = Utilities.getValidMacAddress();
        String macAddress = "98:D3:C1:FD:2F:EC";
        int[] channelsToAcquire = BitalinoDemo.configureChannels();
        int sampleRate = 1000;

        BITalino bitalino = new BITalino();
        try {
            bitalino.open(macAddress, sampleRate);
        } catch (Exception e) {
            System.err.println("❌ No se pudo conectar al BITalino.");
            return null;
        }

        bitalino.start(channelsToAcquire);

        // ✅ Control externo de parada
        AtomicBoolean stopFlag = new AtomicBoolean(false);

        System.out.println("⏺ Grabando... Presiona ENTER para detener.");
        Thread inputThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Scanner scanner = new Scanner(System.in);
                scanner.nextLine(); // espera ENTER
                stopFlag.set(true);
            }
        });
        inputThread.start();

        // ⏳ Ejecutar grabación
        String date = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        ArrayList<Integer> data = BitalinoDemo.recordAndSaveData(bitalino, date, stopFlag, usuario);

        bitalino.stop();
        bitalino.close();
        return data;
    }

    public static void realTimeProcessing(double thresholdOn, double thresholdOff, String usuario, Scanner sc) throws Exception {
        // String macAddress = Utilities.getValidMacAddress();
        String macAddress = "98:D3:C1:FD:2F:EC";
        int[] channelsToAcquire = BitalinoDemo.configureChannels();
        int sampleRate = 1000;

        BITalino bitalino = new BITalino();
        try {
            bitalino.open(macAddress, sampleRate);
        } catch (Exception e) {
            System.err.println("❌ No se pudo conectar al BITalino.");
            return;
        }

        try {
            bitalino.start(channelsToAcquire);
        } catch (Throwable ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Entradas del usuario
        System.out.println("Dime la amplitud: ");
        double amplitud = sc.nextDouble();
        System.out.println("Dime la frecuencia: ");
        double frecuencia = sc.nextDouble();
        System.out.println("Dime el ancho de pulso: ");
        double anchoPulso = sc.nextDouble();
        String puerto = "COM23";

        System.out.println("🔄 Iniciando análisis en tiempo real...");

        // Crear plotter
        RealTimePlotter plotter = new RealTimePlotter("EMG en Tiempo Real");

        EMGRealTimeProcessing processor = new EMGRealTimeProcessing(200, thresholdOn, thresholdOff, puerto, amplitud, frecuencia, anchoPulso, usuario);
        AtomicBoolean stopFlag = new AtomicBoolean(false);

        System.out.println("🟢 Procesamiento en tiempo real... Pulsa ENTER para parar.");
        Thread inputThread = new Thread(() -> {
            new Scanner(System.in).nextLine();
            stopFlag.set(true);
        });
        inputThread.start();

        // Parámetros para conversión correcta:
        final double VCC = 3.0;
        final double gain = 1000.0;  // Ajusta si tu amplificador tiene ganancia distinta
        final int ADC_RESOLUTION = 1024;

        while (!stopFlag.get()) {
            try {
                Frame[] frames = bitalino.read(1);  // Leer 1 frame

                if (frames == null || frames.length == 0) {
                    System.err.println("⚠️ No se recibieron frames del BITalino.");
                    Thread.sleep(10);
                    continue;
                }

                Frame frame = frames[0];
                int rawEmg = BitalinoDemo.getEMGSignalValue(frame);

                // Convertir rawEmg a mV con ganancia incluida para que la escala coincida con thresholds
                double emgValueMv = (((rawEmg / (double) ADC_RESOLUTION) - 0.5) * VCC / gain) * 1000.0;

                if (Double.isNaN(emgValueMv) || Double.isInfinite(emgValueMv) || Math.abs(emgValueMv) > 50) {
                    // Descarta valores absurdos (he reducido a ±50 mV que es más razonable)
                    continue;
                }

                processor.addDataPoint(emgValueMv);
                plotter.addDataPoint(emgValueMv);

            } catch (Exception e) {
                System.err.println("⚠️ Error leyendo del BITalino: " + e.getMessage());
                e.printStackTrace();
            }
        }

        processor.apagarFES();

        processor.guardarEventosFES("C:\\Users\\user\\Downloads\\TFG\\FES_recordings", "C:\\Users\\user\\Downloads\\TFG\\EMG_signals");

        bitalino.stop();
        bitalino.close();

        System.out.println("🛑 Sesión terminada.");
    }

    public static void FESCalibration(Scanner sc) throws IOException, InterruptedException {
        System.out.println("Dime la amplitud: ");
        double amplitud = sc.nextDouble();
        System.out.println("Dime la frecuencia: ");
        double frecuencia = sc.nextDouble();
        System.out.println("Dime el ancho de pulso: ");
        double anchoPulso = sc.nextDouble();
        String puerto = "COM23";

        FESCalibration.calibration(puerto, amplitud, frecuencia, anchoPulso);
    }
}
