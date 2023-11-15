package com.towster15.FileTransferUtil;

import com.towster15.FileTransferUtil.Client.ClientGUI;
import com.towster15.FileTransferUtil.Server.ServerGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainGUI {

    final static String VERSION_NUM = "0.1.47";

    public static void main(String[] args) {
        // Create our window
        final JFrame FRAME = new JFrame("File Transfer Utility");
        FRAME.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Get contents pane and apply layout manager
        Container pane = FRAME.getContentPane();
        GridBagLayout layout = new GridBagLayout();
        pane.setLayout(layout);

        // Reusable variable for the constraints of each element
        GridBagConstraints c = new GridBagConstraints();

        // Create our components for the basic menu
        // Title text
        JLabel titleLabel = new JLabel(
                String.format("Simple File Transfer Utility%s", VERSION_NUM),
                SwingConstants.CENTER
        );
        c.fill = GridBagConstraints.HORIZONTAL;
        // External padding
        c.insets = new Insets(20,50,0,50);
        // Place in the grid
        c.gridx = 0;
        c.gridy = 0;
        pane.add(titleLabel, c);

        // Description text
        JLabel instructionLabel = new JLabel(
                "Select an option:",
                SwingConstants.CENTER
        );
        c.fill = GridBagConstraints.HORIZONTAL;
        // External padding
        c.insets = new Insets(0,50,10,50);
        // Place in the grid
        c.gridx = 0;
        c.gridy = 1;
        pane.add(instructionLabel, c);

        // Start server button
        JButton serverButton = new JButton("Share a file");
        c.fill = GridBagConstraints.HORIZONTAL;
        // Internal padding (around text)
        c.ipadx = 40;
        c.ipady = 20;
        // External padding
        c.insets = new Insets(0,50,5,50);
        // Place in the grid
        c.gridx = 0;
        c.gridy = 2;
        // Create a listener to check for the button being pressed
        serverButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Hide the current window
                FRAME.setVisible(false);
                // Start the server section
                ServerGUI server = new ServerGUI();
                server.MainMenu();
                // Once the server process has finished, re-show the
                // main window
                FRAME.setVisible(true);
            }
        });
        // Add the button to the window
        pane.add(serverButton, c);

        // Start Client button
        JButton clientButton = new JButton("Fetch a file");
        c.fill = GridBagConstraints.HORIZONTAL;
        // Internal padding (around text)
        c.ipadx = 40;
        c.ipady = 20;
        // External padding
        c.insets = new Insets(5,50,5,50);  //top padding
        // Place in the grid
        c.gridx = 0;
        c.gridy = 3;
        // Create a listener to check for the button being pressed
        clientButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Hide the current window
                FRAME.setVisible(false);
                // Start the client section
                ClientGUI client = new ClientGUI();
                client.MainMenu();
                // Once the server process has finished, re-show the
                // main window
                FRAME.setVisible(true);
            }
        });
        // Add the button to the window
        pane.add(clientButton, c);

        // Close program button
        JButton exitButton = new JButton("Exit");
        c.fill = GridBagConstraints.HORIZONTAL;
        // Internal padding (around text)
        c.ipadx = 40;
        c.ipady = 0;
        // External padding
        c.insets = new Insets(5,50,30,50);  //top padding
        // Place in the grid
        c.gridx = 0;
        c.gridy = 4;
        // Create a listener to check for the button being pressed
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FRAME.dispose();
            }
        });
        // Add the button to the window
        pane.add(exitButton, c);

        // Display the window
        FRAME.pack();
        FRAME.setVisible(true);
    }
}
