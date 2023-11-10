package com.towster15.FileTransferUtil.Util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class LineReader {
    public static String readLine(InputStream inputStream) {
        // https://coderanch.com/t/493502/java/read-character-stream-byte-stream
        // Post 7

        // Use this to store our line
        ByteArrayOutputStream line = new ByteArrayOutputStream();
        //
        byte[] nextByte = new byte[1];
        boolean eol = false;
        try {
            // Stop if we hit the end of the data
            while (inputStream.read(nextByte, 0, 1) != -1) {
                // only stop when we see \r (13) followed by \n (10)
                if(nextByte[0] == 13) {
                    // Set a flag, so we know if an EOL is coming
                    // /r only sent from Windows hosts
                    eol = true;
                    continue;
                }
                // Looking for the \n (10) or EOL so we know the string is over
                if(eol || nextByte[0] == 10) {
                    // We found the end of file
                    break;
                } else {
                    // Not EOL, add the character to the line
                    line.write(nextByte[0]);
                }
            }
        } catch(IOException e) {
            System.err.println("Err at readLine(): " + e);
        }
        return line.toString();
    }
}
