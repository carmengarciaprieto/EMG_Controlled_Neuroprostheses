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
    private final double amplitud;
    private final double frecuencia;
    private final double anchoPulso;
    private final String usuario;
    private long startTime = -1;
    private final int warmUpMillis = 5000; 

    private final LinkedList<Double> buffer = new LinkedList<>();
    private boolean fesActivo = false;
    private FESControls fes;
    private final List<Long> emgTimestamps = new ArrayList<>();
    private final List<Long> fesTimestamps = new ArrayList<>();
    private final List<Boolean> fesStates = new ArrayList<>();
    private final List<Double> emgValues = new ArrayList<>();

    // Filtros Butterworth
    private Butterworth notchFilter;
    private Butterworth bandpassFilter;
    private final int fs = 1000; 

    public EMGRealTimeProcessing(int windowSize, double thresholdOn, double thresholdOff, String puerto, double amplitud, double frecuencia, double anchoPulso, String usuario) {
        this.windowSize = windowSize;
        this.thresholdOn = thresholdOn;
        this.thresholdOff = thresholdOff;
        this.amplitud = amplitud;
        this.frecuencia = frecuencia;
        this.anchoPulso = anchoPulso;
        this.usuario = usuario;

        // Inicializa filtros
        notchFilter = new Butterworth();
        notchFilter.bandStop(4, fs, 50.0, 0.8);   

        bandpassFilter = new Butterworth();
        bandpassFilter.bandPass(4, fs, 20.0, 450.0);  

        // Inicializa FES
        fes = new FESControls(puerto, amplitud, frecuencia, anchoPulso);
    }

    public void addDataPoint(double value) {
        if (startTime < 0) {
            startTime = System.currentTimeMillis();
        }

        long elapsed = System.currentTimeMillis() - startTime;

        // Filtra la señal con Notch y Bandpass
        double filteredNotch = notchFilter.filter(value);
        double filteredBandpass = bandpassFilter.filter(filteredNotch);

        double absValue = Math.abs(filteredBandpass);

        // Añade valor absoluto filtrado a buffer
        buffer.add(absValue);
        if (buffer.size() > windowSize) {
            buffer.removeFirst();
        }

        // Ignorar datos durante periodo de warm-up
        if (elapsed < warmUpMillis) {
            return;
        }

        // Esperar a que el buffer esté lleno para procesar
        if (buffer.size() < windowSize) {
            return;
        }

        // Calcular media móvil simple (envelope)
        double sum = 0;
        for (double v : buffer) {
            sum += v;
        }
        double envelope = sum / buffer.size();

        // Debug: imprimir envelope y estado actual de FES
        //System.out.println(String.format("Envelope: %.4f, FES activo: %b", envelope, fesActivo));

        // Guardar valor para análisis posterior
        emgValues.add(envelope);

        // Control de activación/desactivación de FES basado en thresholds
        if (!fesActivo && envelope > thresholdOn) {
            long emgTime = System.currentTimeMillis();
            emgTimestamps.add(emgTime);
            activarFES();
            fesActivo = true;

            // Guardar evento FES ON
            fesTimestamps.add(emgTime);
            fesStates.add(true);

        } else if (fesActivo && envelope < thresholdOff) {
            long emgTime = System.currentTimeMillis();
            emgTimestamps.add(emgTime);
            desactivarFES();
            fesActivo = false;

            // Guardar evento FES OFF
            fesTimestamps.add(emgTime);
            fesStates.add(false);
        }
    }

    public void activarFES() {
        // Descomenta si quieres activar la estimulación física
        fes.startStimulation();
        System.out.println("🟢 FES ACTIVADO (" + System.currentTimeMillis() + " ms)");
    }

    public void desactivarFES() {
        // Descomenta si quieres desactivar la estimulación física
        fes.stopStimulation();
        System.out.println("🔴 FES DESACTIVADO (" + System.currentTimeMillis() + " ms)");
    }

    public void apagarFES() {
        fes.stopStimulation();
        fes.powerOff();
        fes.disconnect();
    }

    public void guardarEventosFES(String carpetaEventosPath, String carpetaSenalPath) {
        try {
            // Crear carpeta eventos (CSV)
            File carpetaEventos = new File(carpetaEventosPath);
            if (!carpetaEventos.exists()) {
                carpetaEventos.mkdirs();
            }

            // Crear carpeta señal (TXT)
            File carpetaSenal = new File(carpetaSenalPath);
            if (!carpetaSenal.exists()) {
                carpetaSenal.mkdirs();
            }

            String fechaHora = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String nombreBase = usuario + "_" + fechaHora;

            // Guardar eventos en CSV
            File archivoEventos = new File(carpetaEventos, nombreBase + ".csv");
            try (PrintWriter writer = new PrintWriter(archivoEventos)) {
                writer.println("emg_timestamp_ms;fes_timestamp_ms;estado");
                for (int i = 0; i < fesTimestamps.size(); i++) {
                    String estado = fesStates.get(i) ? "ON" : "OFF";
                    writer.println(emgTimestamps.get(i) + ";" + fesTimestamps.get(i) + ";" + estado);
                }
            }

            // Guardar señal EMG completa en TXT (valores línea a línea)
            File archivoSenal = new File(carpetaSenal, nombreBase + ".txt");
            try (PrintWriter writer = new PrintWriter(archivoSenal)) {
                for (Double val : emgValues) {
                    writer.println(val);
                }
            }

            System.out.println("✅ Eventos guardados en: " + archivoEventos.getAbsolutePath());
            System.out.println("✅ Señal guardada en: " + archivoSenal.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("❌ Error guardando archivos: " + e.getMessage());
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
