/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Processing;

import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author user
 */
public class FESCalibration {

    public static void calibration(String puerto, double amplitud, double frecuencia, double anchoPulso) throws IOException, InterruptedException {

        FESControls fes = new FESControls(puerto, amplitud, frecuencia, anchoPulso);

            fes.startStimulation();
            System.out.println("🟢 FES ACTIVADO (" + System.currentTimeMillis() + " ms)");
            System.out.println("Estimulación iniciada. Presiona Enter para detener.");

            // Espera a que el usuario presione Enter
            Scanner scanner = new Scanner(System.in);
            scanner.nextLine();  // Pausa hasta que se presione Enter

            // Detener la estimulación si hay un método disponible
            fes.stopStimulation(); 
            fes.powerOff();
            fes.disconnect();
            System.out.println("Estimulación detenida.");

    }
}
