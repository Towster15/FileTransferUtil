package com.towster15.FileTransferUtil.Client;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileBuilder {
    public static void buildFile(byte[] fileBytes, String fileName) throws IOException {
        // Create the file save dialogue
        JFileChooser fileSaveDialogue = new JFileChooser();
        int returnVal = -1;
        do {
            // Show a dialogue box to allow you select where to save the file to
            fileSaveDialogue.setSelectedFile(new File(fileName));
            returnVal = fileSaveDialogue.showSaveDialog(null);
            // If the user chooses to close the window, check that it wasn't an
            // accident
            if (returnVal == JFileChooser.CANCEL_OPTION || returnVal == JFileChooser.ERROR_OPTION) {
                int confirmCancelDialog = JOptionPane.showConfirmDialog(
                        null,
                        "Are you sure you don't want to select a save location?" +
                                "\nThis will delete your received file!",
                        "Confirm cancellation?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                // If intentional, break out of the do-while loop,
                if (confirmCancelDialog == JOptionPane.YES_OPTION) {
                    break;
                }

            }
        } while (returnVal != JFileChooser.APPROVE_OPTION);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File incomingFile = fileSaveDialogue.getSelectedFile();
            // Creates a file stream to write to
            FileOutputStream fileOutStream = new FileOutputStream(incomingFile);
            // Writes all the file's bytes to the file
            fileOutStream.write(fileBytes);
            // Closes the file
            fileOutStream.close();
            // Show success dialogue
            JOptionPane.showMessageDialog(
                    null,
                    String.format("File saved successfully!\nSaved to %s", incomingFile.getAbsolutePath()),
                    "Success!",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            // Show fail dialogue
            JOptionPane.showMessageDialog(
                    null,
                    "Re-request the file to save it.",
                    "File not saved!!",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
}
