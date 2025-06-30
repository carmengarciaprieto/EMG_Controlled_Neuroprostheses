package BITalino;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;


public class BitalinoDemo {

    // Como solo se mide EMG, siempre se configura igual
    public static int[] configureChannels() {
        return new int[]{0}; // Canal 0 para EMG
    }

    // Grabar datos y guardarlos en un archivo
    public static ArrayList<Integer> recordAndSaveData(BITalino bitalino, String recordingDate, AtomicBoolean stopFlag, String usuario) throws BITalinoException, IOException {

        ArrayList<Integer> data = new ArrayList<>();
        int blockSize = 10;

        while (!stopFlag.get()) {
            try {
                Frame[] frames = bitalino.read(blockSize);
                for (Frame frame : frames) {
                    int value = getEMGSignalValue(frame); // función para obtener EMG del frame
                    data.add(value);
                }
            } catch (BITalinoException e) {
                System.err.println("Error: " + e.getMessage());
                break;
            }
        }

        String fileName = generateFileName(recordingDate, usuario);
        saveDataToFile(fileName, data);
        System.out.println("✅ Grabación finalizada. Archivo: " + fileName);

        return data;
    }

    // Obtener el valor de la señal EMG
    public static int getEMGSignalValue(Frame frame) {
        return frame.analog[0]; // Canal 0 de EMG
    }

    // Validar dirección MAC
    public static boolean isValidMacAddress(String macAddress) {
        String macPattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
        return macAddress != null && macAddress.matches(macPattern);
    }

    // Generar el nombre del archivo
    public static String generateFileName(String recordingDate, String usuario) {
        return "EMG_" + usuario + "_" + recordingDate.replace(":", "-").replace(" ", "_") + ".txt";
    }

    // Guardar los datos grabados en un archivo
    private static void saveDataToFile(String fileName, ArrayList<Integer> data) throws IOException {
    // Ruta absoluta completa, incluyendo el nombre del archivo
    String fullPath = "C:\\Users\\user\\Downloads\\TFG\\calibration_recordings\\" + fileName;

    try (FileWriter writer = new FileWriter(fullPath)) {
        for (Integer value : data) {
            writer.write(value + "\n");
        }
        System.out.println("Data saved with name: " + fileName);
        System.out.println("File saved in: " + new File(fullPath).getAbsolutePath());
    }
}
}
