package com.towster15.FileTransferUtil.Client;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Client {
    final Scanner kbScanner;
    final int DEFAULT_PORT = 456;
    int port;
    String password;
    boolean activeConnection = false;

    public Client(Scanner kbScanner) {
        // Start a new keyboard scanner for this process
        this.kbScanner = kbScanner;
    }

    public void MainMenu() {
        // Initialise a new client thread, using a default constructor that does
        // absolutely nothing
        ClientThread clientThread = new ClientThread();
        // Initialise the choice option to something that's not valid to ensure
        // nothing is selected by option
        int choice = -1;
        // Loop until the user asks to exit
        while (choice != 0) {
            if (activeConnection && !clientThread.isAlive()) {
                activeConnection = false;
            }
            System.out.println("\nClient options:");
            if (activeConnection) {
                System.out.println("1. Request file" +
                        "\n2. Test connection" +
                        "\n3. Disconnect from server" +
                        "\n0. Exit");
            } else {
                System.out.println("1. Connect to server" +
                        "\n0. Exit");
            }

            System.out.print("> ");
            choice = this.kbScanner.nextInt();
            // Immediately call nextLine to prevent issues when we actually
            // need to call nextLine
            this.kbScanner.nextLine();
            //
            if ((activeConnection) && (choice != 0)) {
                // Increase the choice by one to make our lives easier below
                choice += 1;
            } else if (!activeConnection && (choice > 1)) {
                // If there's no active connection, prevent the user from
                // selecting a choice that requires a connection
                choice = -1;
            }
            if (choice == 1) {
                // Connect to a server
                // Get the hostname/IP
                String hostname = "";
                boolean validIP = false;
                // Loop until a valid IP has been found
                do {
                    System.out.print("Enter the server's IP address: ");
                    String ipInput = this.kbScanner.nextLine();
                    // https://stackoverflow.com/a/32539471
                    if (ipInput.length() != 0) {
                        try {
                            Object res = InetAddress.getByName(ipInput);
                            validIP = res instanceof Inet4Address || res instanceof Inet6Address;
                        } catch (final UnknownHostException exception) {
                            System.out.println("Unknown host! Please check you entered a valid IPv4 address.");
                            continue;
                        }
                    }
                    if (!validIP) {
                        System.out.println("Invalid IP, please enter a valid IPv4 address.");
                    } else {
                        hostname = ipInput;
                    }
                } while (!validIP);

                // Get valid port
                boolean validPort = false;
                do {
                    System.out.printf("Enter server port number or enter for default (%d): ", DEFAULT_PORT);
                    String portInput = this.kbScanner.nextLine();
                    if (portInput.length() == 0) {
                        // If nothing is entered, use the default port
                        this.port = DEFAULT_PORT;
                    } else {
                        // Check to see if we can get an integer from the input
                        try {
                            this.port = Integer.parseInt(portInput);
                            // Prevent the user from putting in an out-of-range
                            // port
                            if (this.port < 0 || this.port > 65535) {
                                System.out.println("Invalid port! Must be between 1 and 65535.");
                            }
                        } catch (NumberFormatException e) {
                            // Catch exception if no valid input is found
                            System.out.println("Invalid port number");
                            // Use continue to skip us breaking out of the loop
                            continue;
                        }
                    }
                    // Break out of the loop if the port was assigned correctly
                    validPort = true;
                } while (!validPort);

                // As passwords are required, force the user to enter one
                do {
                    System.out.print("Enter password: ");
                    this.password = this.kbScanner.nextLine();
                } while (this.password.length() == 0);

                // Try to create the connection
                try {
                    clientThread = this.ConnectToServer(hostname, this.password);
                    clientThread.start();
                    activeConnection = true;
                } catch (IOException e) {
                    System.out.println("Connection failed: ");
                    activeConnection = false;
                }

            } else if (choice == 2 || choice == 3) {
                // Get the file from the server or test connection
                clientThread.requestQueue.add(choice);

            } else if (choice == 4) {
                // Disconnect from the server
                clientThread.interrupt();
                activeConnection = false;

            } else if (choice == 0) {
                // Exit to main menu
                clientThread.interrupt();
                System.out.println("Returning to main menu...");

            } else {
                // User put in something invalid
                System.out.println("Invalid choice.");
            }
        }
    }

    private ClientThread ConnectToServer(String hostname, String password) throws IOException {
        // https://www.codejava.net/java-se/networking/java-socket-server-examples-tcp-ip
        Socket socket = new Socket(hostname, this.port);
        return new ClientThread(socket, password);
    }
}
