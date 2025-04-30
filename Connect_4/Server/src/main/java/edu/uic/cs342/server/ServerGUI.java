package edu.uic.cs342.server;

import javax.swing.*;
import java.awt.*;
//***************************************************************************//
// Class to show the server log in a GUI window
public class ServerGUI {
    // Area for showing the log messages
    private static final JTextArea logArea = new JTextArea();
    //***************************************************************************//
    // Initialize and display the Server GUI
    public static void init() {
        SwingUtilities.invokeLater(() -> {
            // Sets the title of the GUI
            JFrame frame = new JFrame("Connect4 Server Log");
            // Disable the editing in the log
            logArea.setEditable(false);
            logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            frame.getContentPane().add(new JScrollPane(logArea), BorderLayout.CENTER);
            // Sets the size of the window size
            frame.setSize(600, 400);
            // Centers the window onto the screen
            frame.setLocationRelativeTo(null);
            // Exit the application when it is closed
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // Displays the window
            frame.setVisible(true);
        });
    }
    //***************************************************************************//
    // Function to appends a message to the cosole and the log
    public static void log(String msg) {
        // Prints to output
        System.out.println(msg);
        SwingUtilities.invokeLater(() -> {
            // Adds message to the text area
            logArea.append(msg + "\n");
            // Scrolls to the newest message
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}
//***************************************************************************//
