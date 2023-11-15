package com.towster15.FileTransferUtil.Server;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Server {
    final Scanner kbScanner;
    final int DEFAULT_PORT = 456;
    final int DEFAULT_MAX_CONNECTIONS = 1;
    int port;
    String password;
    int maxConnections;
    boolean sharing;
    File sharedFile = null;
    ConnectionHandler connectionHandler;

    public Server(Scanner kbScanner) {
        // Start a new keyboard scanner for this process
        this.kbScanner = kbScanner;

        // Get port number input
        System.out.printf("Enter port number or enter for default (%d): ", DEFAULT_PORT);
        String portInput = this.kbScanner.nextLine();

        // If nothing is entered, use the default port
        if (portInput.length() == 0) {
            this.port = DEFAULT_PORT;
            System.out.println("Using default port.");
        } else {
            // Check to see if we can get an integer from the port input
            try {
                this.port = Integer.parseInt(portInput);
                // Making sure the chosen port is within the valid range
                if (this.port < 0 || this.port > 65535) {
                    System.out.println("Invalid port selected, using default port instead.");
                    this.port = DEFAULT_PORT;
                }
            } catch (Exception e) {
                // Catch exception if no number is found, reverting to default
                // port
                System.out.println("Invalid port selected, using default port instead.");
                this.port = DEFAULT_PORT;
            }
        }

        // Ensure the user enters a password by refusing to move on until a
        // password is set
        this.password = GetPassword();

        // Get maximum connections input
        System.out.printf("Enter maximum connections or enter for default (%d): ", DEFAULT_MAX_CONNECTIONS);
        String maxConnectionsInput = this.kbScanner.nextLine();

        // If nothing is entered, use the default amount of connections
        if (maxConnectionsInput.length() == 0) {
            this.maxConnections = DEFAULT_MAX_CONNECTIONS;
        } else {
            // Check to see if we can get an integer from the input
            try {
                this.maxConnections = Integer.parseInt(maxConnectionsInput);
            } catch (Exception e) {
                // Catch exception if no number is found, reverting to default
                // connections
                System.out.println("Invalid amount of connections, using default instead.");
                this.maxConnections = DEFAULT_MAX_CONNECTIONS;
            }
        }

        // Tell the user that no files are currently being shared
        sharing = false;
    }

    public void MainMenu() {
        int choice = -1;

        while (choice != 0) {
            // Sharing has to be disabled if the connection handling thread
            // isn't running/isn't assigned
            if (connectionHandler != null && !connectionHandler.isAlive()) {
                sharing = false;
            }
            // Show the main menu
            System.out.println("\nServer options:");
            System.out.println("1. See server information" +
                    "\n2. Change Shared file" +
                    "\n3. Change the password" +
                    "\n4. Start/stop sharing" +
                    "\n0. Exit server"
            );
            if (sharing) {
                System.out.println("File is currently available.");
            } else {
                System.out.println("No files are currently available.");
            }
            // User input
            System.out.print("> ");
            choice = this.kbScanner.nextInt();
            // Immediately call nextLine to prevent issues when we actually
            // need to call nextLine
            this.kbScanner.nextLine();

            // Show information about the server
            if (choice == 1) {
                // Display local IP for clients to connect to
                try {
                    System.out.println(Inet4Address.getLocalHost());
                } catch (UnknownHostException e) {
                    System.out.println("Unable to get localhost IP");
                }
                // Show file information
                if (sharedFile != null) {
                    System.out.printf("Shared file: %s%n", sharedFile.getName());
                } else {
                    System.out.println("Shared file: None");
                }
                System.out.printf("File available: %s%n", sharing);
                // Print amount of connected users
                if (connectionHandler != null && connectionHandler.isAlive()) {
                    System.out.printf("Connected users: %d%n", connectionHandler.activeConnections());
                } else {
                    System.out.println("Connected users: 0");
                }

                // Change the file that's shared
            } else if (choice == 2) {
                System.out.println("\nServer options:");
                System.out.println("1. Type (FULL) file path" +
                        "\n2. Select file from GUI");
                System.out.print("> ");
                choice = this.kbScanner.nextInt();
                // Immediately call nextLine to prevent issues when we actually
                // need to call nextLine
                this.kbScanner.nextLine();
                if (choice == 1 || choice == 2) {
                    this.SelectSharedFile(choice);
                    if (this.sharedFile != null) {
                        try {
                            ConnectionHandler.fileBytes = FileSplitter.splitFile(this.sharedFile);
                            ConnectionHandler.fileName = this.sharedFile.getName();
                        } catch (IOException e) {
                            System.out.println("File not found! Please select another file.");
                        }
                    } else {
                        // The shared file was set to null because a valid file
                        // wasn't selected
                        System.out.println("Invalid file selected. SSF failed");
                    }
                } else {
                    System.out.println("Invalid choice.");
                    this.sharedFile = null;
                }

                // Change server password
            } else if (choice == 3) {
                this.password = GetPassword();

                // Enable/disable file availability
            } else if (choice == 4) {
                // Check for a valid file to share before enabling sharing
                if (this.sharedFile == null) {
                    System.out.println("Cannot start file sharing until a file is selected!");
                } else {
                    sharing = !sharing;
                    if (sharing) {
                        // Start file sharing
                        connectionHandler = new ConnectionHandler(
                                this.port,
                                this.password,
                                this.maxConnections
                        );
                        connectionHandler.start();
                    } else {
                        // End file sharing
                        // Send an interrupt to tell the loop to quit
                        connectionHandler.interrupt();
                    }
                    try {
                        TimeUnit.MILLISECONDS.sleep(50);
                    } catch (InterruptedException e) {
                        /* This exception is raised when the thread is
                         * interrupted whilst sleeping - breaking you out of the
                         * sleep so that you can deal with the interrupt
                         */
                        System.out.println("Something interrupted the main thread!");
                        Thread.currentThread().interrupt();
                    }
                }

                // Exit the program gracefully
            } else if (choice == 0) {
                System.out.println("Server closing...");
                // Send an interrupt to tell the loop to quit
                if (connectionHandler != null) {
                    connectionHandler.interrupt();
                }

                // User put in something invalid
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private String GetPassword() {
        // Ensure the user enters a password by refusing to move on until a
        // password is set
        String pass;
        do {
            System.out.print("Enter password: ");
            pass = this.kbScanner.nextLine();
        } while (pass.length() == 0);
        return pass;
    }

    private void SelectSharedFile(int mode) {
        // Set the shared file to null so that we can check if it's been
        // correctly assigned later
        this.sharedFile = null;
        File chosenFile;
        // Select file from CLI
        if (mode == 1) {
            System.out.print("\nEnter (FULL) file path: ");
            chosenFile = new File(kbScanner.nextLine());
            // Check that the file is valid
            if (checkFileExists(chosenFile) == 0) {
                this.sharedFile = chosenFile;
            }

        // Select file from GUI
        } else if (mode == 2) {
            // Create the file chooser
            final JFileChooser fileOpenDialogue = new JFileChooser();
            int returnVal = fileOpenDialogue.showOpenDialog(null);
            // Check that the user actually selected an option
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                chosenFile = fileOpenDialogue.getSelectedFile();
                // Check that the file is valid
                if (checkFileExists(chosenFile) == 0) {
                    this.sharedFile = chosenFile;
                }
            }
        }

        // Invalid option selected
        if (mode != 1 && mode != 2) {
            System.out.println("Invalid option selected. Invalid mode");
        }
    }

    private int checkFileExists(File file) {
        // Check that the file exists
        if (file.exists() && !file.isDirectory()) {
            return 0;
        } else {
            return -1;
        }
    }
}
