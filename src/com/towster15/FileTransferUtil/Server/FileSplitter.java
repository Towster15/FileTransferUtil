package com.towster15.FileTransferUtil.Server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileSplitter {
    public static byte[] splitFile(File inputFile) throws IOException {
        // https://howtodoinjava.com/java/io/read-file-content-into-byte-array/#2-using-fileinputstream
        // Creates a file stream to read from
        FileInputStream fileInStream = new FileInputStream(inputFile);
        // Create a byte array that's the same length of the file to read into
        byte[] fileBytes = new byte[(int) inputFile.length()];
        /* Read all the bytes from the file to the byte array
         * Ignore returned int - it only tells us how many bytes were read into
         * the array/if we've reached end of file
         */
        int returnVal = fileInStream.read(fileBytes);
        // Close the file
        fileInStream.close();
        // Return file data
        return fileBytes;
    }
}