
package Processing;

import java.io.IOException;
import java.util.Scanner;


public class FESCalibration {

    public static void calibration(String port, double amplitude, double frequency, double pulseWidth) throws IOException, InterruptedException {

        FESControls fes = new FESControls(port, amplitude, frequency, pulseWidth);

        fes.startStimulation();
        System.out.println("FES ACTIVATED (" + System.currentTimeMillis() + " ms)");
        System.out.println("Stimulation started. Press Enter to stop.");

        // Wait for the user to press Enter
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();  // Pauses until Enter is pressed

        fes.stopStimulation(); 
        fes.powerOff();
        fes.disconnect();
        System.out.println("Stimulation stopped.");
    }
}