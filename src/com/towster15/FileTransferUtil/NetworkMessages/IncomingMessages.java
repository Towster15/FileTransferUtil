package com.towster15.FileTransferUtil.NetworkMessages;

public class IncomingMessages {
    // MAKE SURE THERE'S *NO* NEW LINE CHAR AT THE END OF EACH MESSAGE!
    // New line characters are stripped when reading incoming traffic

    // Connection
    public final static String AUTH_SUCCESS = "authenticated";
    // public final static String AUTH_FAIL = "\n";
    // public final static String CONNECTION_SUCCESS = "\n";
    public final static String CONNECTION_CLOSED = "end";

    // File transfer
    public final static String REQUEST_FILE = "sendfile";
    public final static String BYTES_INCOMING = "bytes";

    // Connection test
    public final static String REQUEST_TEST = "test";
    public final static String TEST_SUCCESS = "testpass";
}
