package com.towster15.FileTransferUtil.Client;

import com.towster15.FileTransferUtil.NetworkMessages.IncomingMessages;
import com.towster15.FileTransferUtil.NetworkMessages.OutgoingMessages;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientThread extends Thread {
    private final Socket SOCKET;
    private final String PASSWORD;
    List<Integer> requestQueue;

    public ClientThread() {
        this.SOCKET = null;
        this.PASSWORD = "password";
    }

    public ClientThread(Socket socket, String password) {
        this.SOCKET = socket;
        this.PASSWORD = password;
        this.requestQueue = new ArrayList<Integer>();
    }

    public void run() {
        if (this.SOCKET != null) {
            // FIXME: Implement better error handling
            try {
                // Create a stream for monitoring traffic from the server
                InputStream inputStream = SOCKET.getInputStream();
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(SOCKET.getInputStream()));

                // PrintWriter for writing text
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(SOCKET.getOutputStream());

                // Variable to store the incoming text
                String inputText = "";

                // Authenticate
                System.out.println("About to send password");
                outputStreamWriter.write(this.PASSWORD);
                // Add the newline to show the reader this is the end of line
                outputStreamWriter.write("\n");
                // Flush buffers to send
                outputStreamWriter.flush();
                System.out.println("Sent password");
                // Checks that we got the right password
                if (inputReader.readLine().equals(IncomingMessages.AUTH_SUCCESS)) {
                    System.out.println("Authenticated!");
                    // Loop and read
                    do {
                        // Check that we've not had anything sent from the server
                        if (inputReader.ready()) {
                            inputText = inputReader.readLine();
                            // TODO: This is why we get longer files printed to the terminal
                            System.out.println(inputText);
                        }
                        //
                        if (!this.requestQueue.isEmpty()) {
                            if (this.requestQueue.get(0).equals(2)) {
                                // Request the file
                                outputStreamWriter.write(OutgoingMessages.REQUEST_FILE);
                                // Flush buffers to send
                                outputStreamWriter.flush();

                                // Looking for the file name, the length of the file and then a 0 to be sent
                                // The 0 breaks us from the loop so that we can start reading in the bytes
                                String fileName = inputReader.readLine();
                                int fileLength = Integer.parseInt(inputReader.readLine());
                                int packetCount = Integer.parseInt(inputReader.readLine());
                                System.out.printf("Expecting %d packets containing %d bytes%n", packetCount, fileLength);
                                // Check we're ready to receive the file's bytes
                                if (inputReader.readLine().equals(IncomingMessages.BYTES_INCOMING)) {
                                    System.out.println("Bytes incoming");
                                    // Numbers used for controlling the loop
                                    int packetsReceived = 0;
                                    int bytesToRead = 1024;
                                    // Create our byte array
                                    byte[] fileBytes = new byte[(int) fileLength];
                                    // Loop unitl we've received the right amount of packets
                                    do {
                                        System.out.printf("Packets received: %d%n", packetsReceived);
                                        // Check that we're not going to get a bunch of null chars on the end
                                        if ((fileLength - (packetsReceived * 1024)) < 1024) {
                                            bytesToRead = fileLength - (packetsReceived * 1024);
                                        } if (bytesToRead < 1) {
                                            System.out.println("Negative byte segment length!");
                                            break;
                                        }
                                        // Read the bytes straight from the stream to the byte array
                                        inputReader.read(fileBytes, packetsReceived * 1024, bytesToRead);
                                        // Mark one more packet as received
                                        packetsReceived++;
                                    } while (packetsReceived <= packetCount);
                                    // Construct the file
                                    FileBuilder.buildFile(fileBytes, fileName);
                                } else {
                                    System.out.println("Received malformed data, transfer cancelled");
                                }
                                // Remove the first request from the queue now that we've dealt with it
                                this.requestQueue.remove(0);

                            } else if (this.requestQueue.get(0).equals(3)) {
                                // Test the connection
                                outputStreamWriter.write(OutgoingMessages.REQUEST_TEST);
                                // Flush buffers to send
                                outputStreamWriter.flush();
                                System.out.println("Testing connection...");
                                // Returns a known value
                                if (inputReader.readLine().equals(IncomingMessages.TEST_SUCCESS)) {
                                    System.out.println("Connection is alive.");
                                }
                                // Remove the first request from the queue now that we've dealt with it
                                this.requestQueue.remove(0);
                            }
                            // Flush buffers to send
                            outputStreamWriter.flush();
                        }
                        // Sleep briefly to stop max CPU
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    } while (!inputText.equals(IncomingMessages.CONNECTION_CLOSED) && !Thread.interrupted());

                    // Send a final message to ensure the server knows the connection has been closed
                    // Only sends the message if the close request didn't come from the server
                    if (Thread.interrupted()) {
                        outputStreamWriter.write(OutgoingMessages.CONNECTION_CLOSED);
                        // Flush buffers to send
                        outputStreamWriter.flush();
                    }

                // Display the incorrect password message
                } else {
                    System.out.println("Incorrect password, connection closed.");
                }

                // Close the socket once a disconnect has been requested
                this.SOCKET.close();

            } catch (IOException ioException) {
                System.out.println(ioException.getMessage());
            }
        } else {
            System.out.println("null socket");
        }
    }
}
