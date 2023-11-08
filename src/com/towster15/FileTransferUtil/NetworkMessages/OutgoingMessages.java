package com.towster15.FileTransferUtil.NetworkMessages;

public class OutgoingMessages {
    // MAKE SURE TO ADD THE NEW LINE CHAR AT THE END OF EACH MESSAGE!
    // Add the newline to show the reader this is the end of line

    // Connection
    public final static String AUTH_SUCCESS = String.format("%s%n", IncomingMessages.AUTH_SUCCESS);
    // public final static String AUTH_FAIL = "\n";
    // public final static String CONNECTION_SUCCESS = "\n";
    public final static String CONNECTION_CLOSED = String.format("%s%n", IncomingMessages.CONNECTION_CLOSED);

    // File transfer
    public final static String REQUEST_FILE = String.format("%s%n", IncomingMessages.REQUEST_FILE);
    public final static String BYTES_INCOMING = String.format("%s%n", IncomingMessages.BYTES_INCOMING);

    // Connection test
    public final static String REQUEST_TEST = String.format("%s%n", IncomingMessages.REQUEST_TEST);
    public final static String TEST_SUCCESS = String.format("%s%n", IncomingMessages.TEST_SUCCESS);
}

