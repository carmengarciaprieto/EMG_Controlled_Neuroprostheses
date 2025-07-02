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
