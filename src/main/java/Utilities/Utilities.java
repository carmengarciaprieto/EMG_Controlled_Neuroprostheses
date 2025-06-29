/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utilities;

import BITalino.BitalinoDemo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 *
 * @author carmengarciaprieto
 */
public class Utilities {

    private static BufferedReader r = new BufferedReader(new InputStreamReader(System.in));

    public static String readString() {
        String text = null;
        boolean ok = false;
        do {
            try {
                text = r.readLine();
                if (!text.isEmpty()) {
                    ok = true;
                } else {
                    System.out.println("Empty input, please try again:");
                }
            } catch (IOException e) {

            }
        } while (!ok);

        return text;
    }

    public static void displayRecordingInstructions() {
        System.out.println("\n  Recording Instructions ===");
        System.out.println("1. Place the electrodes on the extensor muscle:\n"
                + "   - Electrode 1 (Red): Upper part of the extensor muscle.\n"
                + "   - Electrode 2 (Black): 2-3 cm below Electrode 1, along the muscle line.\n"
                + "   - Electrode 3 (White - Ground): On the elbow.\n"
                + "2. Make movement.\n"
                + "3. Avoid unnecessary external movements.");

        System.out.println("\n The recording will last 60 seconds.");
    }

    public static String getValidMacAddress() {
        String macAddress;
        while (true) {
            System.out.println("Introduce a valid MAC address (format: XX:XX:XX:XX:XX:XX): ");
            macAddress = readString();
            if (BitalinoDemo.isValidMacAddress(macAddress)) {
                break;
            }
            System.out.println("MAC address invalid. Try again.");
        }
        return macAddress;
    }

    public static boolean askToRetry(String message) {
        while (true) {
            System.out.println(message + " [YES/NO]: ");
            String retryResponse = readString().toUpperCase();
            if (retryResponse.equals("YES")) {
                return true;
            }
            if (retryResponse.equals("NO")) {
                return false;
            }
            System.out.println("Not a valid response. Please type YES or NO.");
        }
    }
}
