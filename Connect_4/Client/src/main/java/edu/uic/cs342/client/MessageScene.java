package edu.uic.cs342.client;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
//***************************************************************************//
// Class for messaging between clients
public class MessageScene {
    private static MessageScene instance;
    private final TextArea chatArea = new TextArea();
    private final ClientMain app;
    //***************************************************************************//
    // Constructor
    private MessageScene(ClientMain app) {
        // Stores references to the main app
        this.app = app;
        // Set up an instance
        instance = this;
    }
    //***************************************************************************//
    // Method to build the scene
    public static Scene create(ClientMain app) {
        // Creates a new instance
        new MessageScene(app);
        // Returns the scene
        return instance.buildScene();
    }
    //***************************************************************************//
    // Function to provide access to the controller functions
    public static MessageScene getController() {
        // Return the instance
        return instance;
    }
    //***************************************************************************//
    // Constructs and returns the scene
    private Scene buildScene() {
        // Disables chat to be editied
        chatArea.setEditable(false);
        // Height of the chat display
        chatArea.setPrefHeight(200);

        // Input for new messages
        TextField chatInput = new TextField();
        // Prompts user to type a message
        chatInput.setPromptText("Type message…");
        // Button for user to send message
        Button sendBtn = new Button("Send");
        // Handles if user presses on the send button
        sendBtn.setOnAction(e -> {
            String t = chatInput.getText().trim();
            // Ensures non-empty texts are sent
            if (!t.isEmpty()) {
                // Sends message to server
                app.getConnection().send("CHAT:" + t);
                // Clears the input field
                chatInput.clear();
            }
        });
        //***************************************************************************//
        // Button for user to return to game
        Button back = new Button("Back to Game");
        back.setOnAction(e -> app.switchToGame(app.getConnection().getUsername()));

        // Vertical layout for the chat section
        VBox root = new VBox(10,
                new Label("Chat History"),
                chatArea,
                chatInput,
                sendBtn,
                back
        );
        // Centers the elements
        root.setAlignment(Pos.CENTER);
        // Adds padding to the edges
        root.setStyle("-fx-padding:20;");
        // Creates and returns scene with dimensions
        return new Scene(root, 400, 300);
    }
    //***************************************************************************//
    // Function to append username to the messages
    public void addMessage(String from, String text) {
        chatArea.appendText(from + ": " + text + "\n");
    }
}
//***************************************************************************//
