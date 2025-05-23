package com.towster15.FileTransferUtil.Server;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConnectionHandler extends Thread {
    static byte[] fileBytes;
    static String fileName;
    private final int PORT;
    private final List<Thread> threads;
    int maxConnections;
    String password;

    public ConnectionHandler(int port, String password, int maxConnections) {
        this.PORT = port;
        this.maxConnections = maxConnections;
        this.threads = new ArrayList<Thread>();
        this.password = password;
    }

    public void run() {
        ServerSocket serverSocket;
        // Try creating a new server socket
        try {
            serverSocket = new ServerSocket(this.PORT);
        } catch (IOException e) {
            System.out.println("Failed to start Server Socket.");
            System.out.println(e.getMessage());
            return;
        }

        // Let the user know that the server has started successfully
        System.out.printf("System is listening on port %d%s", this.PORT, "\n");

        try {
            serverSocket.setSoTimeout(1);
        } catch (SocketException e) {
            assert true;
        }

        // Keep scanning for new connections until the close command is
        // called
        while (!Thread.interrupted()) {
            // Check that a new thread wouldn't be above the max connection
            // Makes the new connection wait if above the max
            if (this.threads.size() < this.maxConnections) {
                // Attempt to create a new socket for an incoming connection
                Socket socket;
                try {
                    socket = serverSocket.accept();
                } catch (InterruptedIOException ex) {
                    continue;
                } catch (IOException e) {
                    System.out.println("Failed to accept incoming connection");
                    // Continue onto the next loop/next connection if this one
                    // failed
                    continue;
                }
                System.out.println("New client connected");
                // https://stackoverflow.com/a/3663949
                // Add the new thread to the list of threads
                this.threads.add(new ServerThread(socket, password, fileBytes, fileName));
                // Should never return -1 as we've just added something to
                // the list beforehand
                // https://stackoverflow.com/a/687842
                this.threads.get(this.threads.size() - 1).start();
            }
        }
        System.out.println("Closing all connections...");
        // Prune threads first to prevent issuing commands to dead threads
        this.pruneThreads();
        for (Thread thread : this.threads) {
            thread.interrupt();
        }
    }

    private void pruneThreads() {
        // https://stackoverflow.com/a/17279584
        for (Iterator<Thread> iter = this.threads.listIterator(); iter.hasNext(); ) {
            Thread thread = iter.next();
            if (!thread.isAlive()) {
                // Remove the thread from the list if it's dead
                iter.remove();
            }
        }
    }

    public int activeConnections() {
        this.pruneThreads();
        return this.threads.size();
    }
}