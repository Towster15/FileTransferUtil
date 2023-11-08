package com.towster15.FileTransferUtil.Server;

import com.towster15.FileTransferUtil.NetworkMessages.*;

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
            // outputStreamWriter.close();
            System.out.println("Waiting for password authentication...");
            String latestLine = inputReader.readLine();
            // Check for password being correct
            if (latestLine == null || !latestLine.equals(this.PASSWORD)) {
                outputStreamWriter.write(OutgoingMessages.CONNECTION_CLOSED);
                // Flush buffers to send
                outputStreamWriter.flush();
                System.out.println("Authentication failed");
                this.SOCKET.close();
                return;
            } else {
                System.out.println("Authenticated successfully");
                outputStreamWriter.write(OutgoingMessages.AUTH_SUCCESS);
                // Flush buffers to send
                outputStreamWriter.flush();
            }

            // Variable to store the incoming text
            String inputText = "";
            do {
                if (inputReader.ready()) {
                    inputText = inputReader.readLine();
                    if (inputText.equals(IncomingMessages.REQUEST_FILE)) {
                        // Request the file
                        // Send the file length and extension first
                        outputStreamWriter.write(String.format("%s%n", this.FILE_NAME));
                        // Flush buffers to send
                        outputStreamWriter.flush();
                        outputStreamWriter.write(String.format("%d%n", FILE_BYTES.length));
                        // Flush buffers to send
                        outputStreamWriter.flush();
                        // Send a message to show it'll switch to bytes
                        outputStreamWriter.write(OutgoingMessages.BYTES_INCOMING);
                        // Flush buffers to send
                        outputStreamWriter.flush();
                        // Send the file after
                        outputStream.write(FILE_BYTES);
                        // Add the newline to show the reader this is the end of line
                        outputStreamWriter.write("\n");

                    } else if (inputText.equals(IncomingMessages.REQUEST_TEST)) {
                        // Test the connection
                        // Return a known value
                        System.out.println("Test requested");
                        outputStreamWriter.write(OutgoingMessages.TEST_SUCCESS);

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
            } while (!inputText.equals(IncomingMessages.CONNECTION_CLOSED) && !checkInterrupt);
            // Only send the interrupt request if the client didn't send us one
            // first
            if (checkInterrupt) {
                // Send a final message to ensure the client knows the
                // connection has been closed
                outputStreamWriter.write(OutgoingMessages.CONNECTION_CLOSED);
                // Flush buffers to send
                outputStreamWriter.flush();
            }
            // Close the socket once the client has requested to disconnect
            this.SOCKET.close();
        } catch (IOException ioException) {
            System.out.println(ioException.getMessage());
        }
    }
}
