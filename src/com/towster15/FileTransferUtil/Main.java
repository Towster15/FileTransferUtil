package com.towster15.FileTransferUtil;

import com.towster15.FileTransferUtil.Client.Client;
import com.towster15.FileTransferUtil.Server.Server;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        final String versionNum = "0.1.23";
        Scanner kbScanner = new Scanner(System.in);
        int choice = -1;

        while (choice != 0) {
            System.out.printf("\nSimple file transfer utility v%s%n", versionNum);
            System.out.println("What do you wish to do:");
            System.out.println("1. Send a file");
            System.out.println("2. Receive a file");
            System.out.println("0. Quit");
            System.out.print("> ");
            try {
                choice = kbScanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input, please select a number");
            }
            // Remove the new line character that would follow the int
            kbScanner.nextLine();
            if (choice == 1) {
                // Start the server section
                Server server = new Server(kbScanner);
                server.MainMenu();

            } else if (choice == 2) {
                // Start the client section
                Client client = new Client(kbScanner);
                client.MainMenu();

            } else if (choice == 0) {
                // Exit the program gracefully
                System.out.println("Exiting...");

            } else {
                // User put in something invalid
                System.out.println("Invalid choice.");
            }
        }
        kbScanner.close();
    }
}
