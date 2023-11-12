package com.towster15.FileTransferUtil.Client;

import com.towster15.FileTransferUtil.Util.IncomingNetMessages;
import com.towster15.FileTransferUtil.Util.LineReader;
import com.towster15.FileTransferUtil.Util.OutgoingNetMessages;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
                BufferedInputStream inputStream = new BufferedInputStream(SOCKET.getInputStream());

                // PrintWriter for writing text
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(SOCKET.getOutputStream());

                // Variable to store the incoming text
                String inputText = "";

                // Authenticate
                outputStreamWriter.write(this.PASSWORD);
                // Add the newline to show the reader this is the end of line
                outputStreamWriter.write("\n");
                // Flush buffers to send
                outputStreamWriter.flush();
                // Checks that we got the right password
                if (LineReader.readLine(inputStream).equals(IncomingNetMessages.AUTH_SUCCESS)) {
                    System.out.println("Authenticated!");
                    // Check whether an interrupt has been received
                    boolean checkInterrupt = false;
                    // Loop and read
                    do {
                        // Check that we've not had anything sent from the server
                        if (inputStream.available() > 0) {
                            inputText = LineReader.readLine(inputStream);
                        }
                        //
                        if (!this.requestQueue.isEmpty()) {
                            if (this.requestQueue.get(0).equals(2)) {
                                // Request the file
                                outputStreamWriter.write(OutgoingNetMessages.REQUEST_FILE);
                                // Flush buffers to send
                                outputStreamWriter.flush();

                                // Looking for the file name, the length of the file and then a 0 to be sent
                                // The 0 breaks us from the loop so that we can start reading in the bytes
                                String fileName = LineReader.readLine(inputStream);
                                int fileLength = Integer.parseInt(LineReader.readLine(inputStream));
                                int packetCount = Integer.parseInt(LineReader.readLine(inputStream));

                                // Check we're ready to receive the file's bytes
                                if (LineReader.readLine(inputStream).equals(IncomingNetMessages.BYTES_INCOMING)) {
                                    // Numbers used for controlling the loop
                                    int packetsReceived = 0;
                                    int bytesToRead = 1024;
                                    // Create our byte array
                                    byte[] fileBytes = new byte[(int) fileLength];
                                    // Loop unitl we've received the right amount of packets
                                    while (packetsReceived < packetCount) {
                                        // Check that we're not going to get a bunch of null chars on the end
                                        if ((fileLength - (packetsReceived * 1024)) < 1024) {
                                            bytesToRead = fileLength - (packetsReceived * 1024);
                                        }
                                        if (bytesToRead < 1) {
                                            System.out.println("Negative byte segment length!");
                                            break;
                                        }
                                        // Read the bytes straight from the stream to the byte array
                                        inputStream.read(fileBytes, packetsReceived * 1024, bytesToRead);
                                        // Mark one more packet as received
                                        packetsReceived++;
                                    }
                                    // Construct the file
                                    FileBuilder.buildFile(fileBytes, fileName);
                                } else {
                                    System.out.println("Received malformed data, transfer cancelled");
                                }
                                // Remove the first request from the queue now that we've dealt with it
                                this.requestQueue.remove(0);

                            } else if (this.requestQueue.get(0).equals(3)) {
                                // Test the connection
                                outputStreamWriter.write(OutgoingNetMessages.REQUEST_TEST);
                                // Flush buffers to send
                                outputStreamWriter.flush();
                                System.out.println("Testing connection...");
                                // Returns a known value
                                if (LineReader.readLine(inputStream).equals(IncomingNetMessages.TEST_SUCCESS)) {
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
                            checkInterrupt = true;
                        }
                        // Check for interrupts
                        if (Thread.interrupted()) {
                            checkInterrupt = true;
                        }

                    } while (!inputText.equals(IncomingNetMessages.CONNECTION_CLOSED) && !checkInterrupt);

                    // Send a final message to ensure the server knows the connection has been closed
                    // Only sends the message if the close request didn't come from the server
                    if (checkInterrupt) {
                        outputStreamWriter.write(OutgoingNetMessages.CONNECTION_CLOSED);
                        // Flush buffers to send
                        outputStreamWriter.flush();
                    } else {
                        System.out.println("Server closed");
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
            System.out.println("Null socket");
        }
    }
}
