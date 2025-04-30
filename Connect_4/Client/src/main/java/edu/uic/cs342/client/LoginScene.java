package edu.uic.cs342.client;

import java.net.URL;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.stage.Stage;
//***************************************************************************//
// Class for logging user in
public class LoginScene {
    private static LoginScene instance;
    private final Label msgLabel;
    private final ClientMain app;

    //***************************************************************************//
    // Constructor to initialize the fields
    private LoginScene(ClientMain app) {
        // stores the app reference
        this.app = app;
        // Message label
        this.msgLabel = new Label();
        // Sets an istance
        instance = this;
    }

    //***************************************************************************//
    // Method to build the scence
    public static Scene build(ClientMain app) {
        // Creates a new instance
        instance = new LoginScene(app);
        // Returns the scene
        return instance.buildScene();
    }

    //***************************************************************************//
    // Method to provide access to controller
    public static LoginScene getController() {
        // Return the instance
        return instance;
    }

    //***************************************************************************//
    // Methos to return the scene created
    private Scene buildScene() {
        Stage stage = app.getPrimaryStage();

        // Leads the background graphics of the game
        URL starUrl = getClass().getResource("/images/star-background.png");
        URL boardUrl = getClass().getResource("/images/board-overlay.png");
        // Checks if backgrounds are not noll
        if (starUrl == null || boardUrl == null) {
            // if null, throw an exception
            throw new IllegalStateException("Missing images");
        }
        // Creates an imageView for the background
        ImageView bg = new ImageView(new Image(starUrl.toExternalForm()));
        bg.setPreserveRatio(false);
        bg.fitWidthProperty().bind(stage.widthProperty());
        bg.fitHeightProperty().bind(stage.heightProperty());
        // Overlap the image graphics
        ImageView boardView = new ImageView(new Image(boardUrl.toExternalForm()));
        boardView.setPreserveRatio(true);
        boardView.fitWidthProperty().bind(stage.widthProperty().multiply(0.55));

        // Gradient for the text
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                // Top color
                new Stop(0, Color.YELLOW),
                // Middle color
                new Stop(0.5, Color.ORANGE),
                // Bottom color
                new Stop(1, Color.RED)
        );
//***************************************************************************//
        // Vertical layout
        VBox overlay = new VBox(15);
        // Centers the elements
        overlay.setAlignment(Pos.CENTER);
        // Adjust board width and height
        overlay.prefWidthProperty().bind(boardView.fitWidthProperty());
        overlay.prefHeightProperty().bind(boardView.fitHeightProperty());

        // Title of the login Scene
        Label title = new Label("CONNECT 4");
        // Font and text
        title.setFont(Font.font("Impact", 90));
        // Fills in the text with the gradient
        title.setTextFill(gradient);
        title.setStyle("-fx-background-color:black; -fx-padding:5px; -fx-background-radius:5px;");
        // Prompts user to enter in their username
        Label prompt = new Label("Enter Username:");
        // Set the font and size of text
        prompt.setFont(Font.font("Verdana", 24));
        // Fills text with the gradient
        prompt.setTextFill(gradient);
        prompt.setStyle("-fx-background-color:black; -fx-padding:3px; -fx-background-radius:3px;");
        // Nnew text field for user input
        TextField tf = new TextField();
        tf.setMaxWidth(200);
        tf.setPromptText("Username");

        // Declare start button
        Button startBtn = new Button("START");
        // Initialize font and size
        startBtn.setFont(Font.font("Impact", 48));
        // fill in text with gradient
        startBtn.setTextFill(gradient);
        startBtn.setStyle("-fx-background-color:black; -fx-padding:5px; -fx-background-radius:5px;");
        // Handles when button is pressed
        startBtn.setOnAction(e -> {
            String name = tf.getText().trim();
            // If no username is entered, display an error
            if (name.isEmpty()) {
                showError("Username may not be empty");
            } else {
                // Clear the previous errors
                clearError();
                // store the username in connection
                app.getConnection().setUsername(name);
                // Send the Login request with username
                app.getConnection().send("LOGIN:" + name);
            }
        });
        overlay.getChildren().addAll(title, prompt, tf, startBtn, msgLabel);
        // Stack the background
        StackPane root = new StackPane(bg, boardView, overlay);
        // Center the elements
        StackPane.setAlignment(boardView, Pos.CENTER);
        StackPane.setAlignment(overlay, Pos.CENTER);
        // Return the scene
        return new Scene(root);
    }

    //***************************************************************************//
    // Display error message
    public void showError(String message) {
        msgLabel.setText(message);
        msgLabel.setStyle(
                "-fx-background-color:black; -fx-text-fill:white; -fx-padding:3px; -fx-background-radius:3px;"
        );
    }
    //***************************************************************************//
    // method to clear the error text
    private void clearError() {
        msgLabel.setText("");
        msgLabel.setStyle("");
    }
}
//***************************************************************************//