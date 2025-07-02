package Processing;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FESControls {

    private SerialPort serialPort;
    private String portName;
    private static final int BAUD_RATE = 9600;

    public FESControls(String portName, double amplitud, double frecuencia, double anchoPulso) {
        this.portName = portName;
        if (!connect()) {  // <-- Aquí se llama a fes.connect()
            System.out.println("Could not connect to FES");
            return; // Sale si no conecta
        }

        int canal = 16;
        try {
            powerOn();
            System.out.println(">>> Stimulating channel " + canal);

            // Configura amplitud, ancho de pulso y frecuencia
            setCurrent(amplitud, canal);
            setPulseWidth(anchoPulso, canal);
            sendCommand("w " + canal + " re 0\r");
            sendCommand("w " + canal + " in 0\r");
            setFrequency(frecuencia);

            // Activa el canal con máscara (estado 1 para activo)
            setMask(canal, 0);  // Llama a setMask simplificada

            sendCommand("e fl 0\r");  // fin de lista en índice 0
        } catch (IOException ex) {
            Logger.getLogger(EMGRealTimeProcessing.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean connect() {
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setBaudRate(BAUD_RATE); //velocidad de transmisionde bits
        serialPort.setNumDataBits(8); //8 bits de datos por cada paquete de comunicaion
        serialPort.setNumStopBits(1); //1 bit de parada para idnicar el final de un paquete de datos
        serialPort.setParity(SerialPort.NO_PARITY); //sin paridad (no se detectan errores en la transmisión

        if (serialPort.openPort()) {
            System.out.println("Connecting FES in " + portName);
            return true;
        } else {
            System.out.println("Error connecting device " + portName);
            return false;
        }
    }

    public void powerOn() {
        String command = "on2\r"; //escribe en el dispostivo, refiriendose a la fuente de alimentacion 2, y la activa

        try {
            sendCommand(command);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Power supply ON");
    }

    public void powerOff() {
        String command = "off2\r"; //escribe en el dispostivo, refiriendose a la fuente de alimentacion 2, y la desactiva

        try {
            sendCommand(command);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Poweer supply OFF");
    }

    public void startStimulation() {
        String command = "s\r";
        try {
            sendCommand(command);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Stimulation started");
    }

    public void stopStimulation() {
        String command = "p\r";
        try {
            sendCommand(command);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Stimulation stopped");
    }

    public void disconnect() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            System.out.println("Conexion closed");
        }
    }

    public void setMask(int canal, int estado) {

        String command = "e lc " + canal + " " + estado + "\r";
        try {
            sendCommand(command);
            System.out.println("Comand sent: " + command);
        } catch (IOException e) {
            throw new RuntimeException("Error sending comand to channel " + canal, e);
        }

    }

    public void setFrequency(double frequency) {
        if (frequency <= 0) {
            throw new IllegalArgumentException("Frequency must be higher than 0");
        }

        int timeMs = (int) ((1.0 / frequency) / 0.0005); // convierte Hz a tiempo en ms
        int timeMs2;
        timeMs2 = (int) ((1.0 / 1000) / (0.0005));
        String command = "e tg " + timeMs + "\r";
        String command2 = "e ti " + timeMs2 + "\r";
        
        try {
            sendCommand(command);
            sendCommand(command2);
            System.out.println("Frequency set: " + frequency + " Hz (tiempo: " + timeMs + ")");
        } catch (IOException e) {
            throw new RuntimeException("Error sending frequency comand", e);
        }
    }

    public void setPulseWidth(double pulseWidth, int canal) {
        int pulseValue = (int) ((pulseWidth - 27.6) / 2.4);
        if (pulseValue < 0) {
            throw new IllegalArgumentException("Pulse width must be higher or equal than 27.6 ms");
        }
        String command = "w " + canal + " tp " + pulseValue + "\r";  // Canal 16 fijo
        String command2 = "w " + canal + " tn " + pulseValue + "\r";  // Canal 16 fijo

        try {
            sendCommand(command);
            sendCommand(command2);
            System.out.println("Pulse width set: " + pulseWidth + " ms (valor: " + pulseValue + ")");
        } catch (IOException e) {
            throw new RuntimeException("Error sending pulse width comand", e);
        }
    }

    public void setCurrent(double amplitude, int canal) {
        int currentValue = (int) (amplitude / 0.78);
        System.out.println("Pulse width set: " + amplitude + " ms (valor: " + currentValue + ")");
        if (currentValue < 0) {
            throw new IllegalArgumentException("Amplitude must be higher or equal than 0");
        }
        String command = "w " + canal + " ap " + currentValue + "\r";  // Canal 16 fijo
        String command2 = "w " + canal + " an " + currentValue + "\r";  // Canal 16 fijo

        try {
            sendCommand(command);
            sendCommand(command2);
            System.out.println("Current set: " + amplitude + " mA (valor: " + currentValue + ")");
        } catch (IOException e) {
            throw new RuntimeException("Error sending current comand", e);
        }
    }

    void sendCommand(String command) throws IOException {
        serialPort.writeBytes(command.getBytes(), command.length()); //convierte el comando en un array de bits y lo envia por el puerto serie, indica la cantidad de bytes a enviar
        byte[] comando = command.getBytes();
        for (int i = 0; i < comando.length; i++) {
            System.out.println(comando[i]);
        }
        System.out.println("\n");
        try {
            Thread.sleep(200); // Pequeña espera para recibir la respuesta
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        byte[] buffer = new byte[20];  // Tamaño del buffer mayor para que cubra cualquier respuesta posible
        int numBytes = serialPort.readBytes(buffer, buffer.length); // Lee los bytes desde el puerto serie

        if (numBytes > 0) {
            String response = new String(buffer, 0, numBytes, StandardCharsets.UTF_8).trim();  // Convierte bytes a string
            System.out.println("Response: \"" + response + "\"");

        } else {
            System.out.println("No response from the device.");
        }
    }
}
