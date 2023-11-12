package com.towster15.FileTransferUtil.Server;

import com.towster15.FileTransferUtil.Util.IncomingNetMessages;
import com.towster15.FileTransferUtil.Util.OutgoingNetMessages;

import java.io.*;
import java.net.Socket;

public class ServerThread extends Thread {
    private final Socket SOCKET;
    private final byte[] FILE_BYTES;
    private final String FILE_NAME;
    private final String PASSWORD;

    public ServerThread(Socket socket, String password, byte[] fileBytes, String fileName) {
        this.SOCKET = socket;
        this.PASSWORD = password;
        this.FILE_BYTES = fileBytes;
        this.FILE_NAME = fileName;
    }

    public void run() {
        // FIXME: Error handling for this needs a lot of improvement
        boolean checkInterrupt;
        try {
            // Create a stream for monitoring traffic from the client
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(SOCKET.getInputStream()));
            // OutputStream for writing raw bytes, PrintWriter for writing text
            OutputStream outputStream = SOCKET.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            // Get password
            String latestLine = inputReader.readLine();
            // Check for password being correct
            if (latestLine == null || !latestLine.equals(this.PASSWORD)) {
                outputStreamWriter.write(OutgoingNetMessages.CONNECTION_CLOSED);
                // Flush buffers to send
                outputStreamWriter.flush();
                System.out.println("Authentication failed");
                this.SOCKET.close();
                return;
            } else {
                System.out.println("Authenticated successfully");
                outputStreamWriter.write(OutgoingNetMessages.AUTH_SUCCESS);
                // Flush buffers to send
                outputStreamWriter.flush();
            }

            // Variable to store the incoming text
            String inputText = "";
            do {
                if (inputReader.ready()) {
                    inputText = inputReader.readLine();
                    if (inputText.equals(IncomingNetMessages.REQUEST_FILE)) {
                        //
                        System.out.println("File requested");
                        // Request the file
                        // Send the file name, extensions and flush to send
                        outputStreamWriter.write(String.format("%s%n", this.FILE_NAME));
                        outputStreamWriter.flush();
                        // Send the file length and flush to send
                        outputStreamWriter.write(String.format("%d%n", FILE_BYTES.length));
                        outputStreamWriter.flush();
                        // Send the amount of incoming packets and flush to send
                        outputStreamWriter.write(
                                String.format("%d%n", (int) Math.ceil(FILE_BYTES.length / 1024.0))
                        );
                        outputStreamWriter.flush();
                        // Send a message to show it'll switch to bytes and
                        // flush to send
                        outputStreamWriter.write(OutgoingNetMessages.BYTES_INCOMING);
                        outputStreamWriter.flush();

                        // Send the file after
                        // Loop through the file to break it up into individual
                        // packets, sending 1KB at a time
                        int offset = 0;
                        int length = 1024;
                        do {
                            // Check that we're not at the end of the file, so
                            // that we don't send loads of null chars
                            if ((FILE_BYTES.length - offset) < 1024) {
                                length = FILE_BYTES.length - offset;
                            }
                            if (length < 1) {
                                System.out.println("Negative byte segment length!");
                                break;
                            }
                            // Send the KB
                            outputStream.write(FILE_BYTES, offset, length);
                            // Flush to send
                            outputStreamWriter.flush();
                            // Increment offset to make sure we get the next KB
                            // of the file
                            offset += 1024;
                        } while (offset <= FILE_BYTES.length);

                        System.out.println("File sent");

                    } else if (inputText.equals(IncomingNetMessages.REQUEST_TEST)) {
                        // Test the connection
                        // Return a known value
                        System.out.println("Test requested");
                        outputStreamWriter.write(OutgoingNetMessages.TEST_SUCCESS);

                    }
                    // Flush buffers to send
                    outputStreamWriter.flush();
                }
                // Check for interrupts
                checkInterrupt = Thread.interrupted();
                // Sleep briefly to stop max CPU
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } while (!inputText.equals(IncomingNetMessages.CONNECTION_CLOSED) && !checkInterrupt);
            // Only send the interrupt request if the client didn't send us one
            // first
            if (checkInterrupt) {
                // Send a final message to ensure the client knows the
                // connection has been closed
                outputStreamWriter.write(OutgoingNetMessages.CONNECTION_CLOSED);
                // Flush buffers to send
                outputStreamWriter.flush();
            }
            // Show anybody reading from the console that a client has disconnected
            System.out.println("Client disconnected");
            // Close the socket once the client has requested to disconnect
            this.SOCKET.close();
        } catch (IOException ioException) {
            System.out.println(ioException.getMessage());
        }
    }
}
