package edu.uic.cs342.client;

import java.net.URL;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.stage.Stage;
//***************************************************************************//
// Class to build the lobby scene
public class LobbyScene {
    // Method to build the scene
    public static Scene build(ClientMain app) {
        Stage stage = app.getPrimaryStage();

        // Add the background into the lobby scene
        URL starUrl = LobbyScene.class.getResource("/images/star-background.png");
        // If the background is not found, throw an error
        if (starUrl == null) throw new IllegalStateException("Missing /images/star-background.png");
        ImageView bg = new ImageView(new Image(starUrl.toExternalForm()));
        bg.setPreserveRatio(false);
        bg.fitWidthProperty().bind(stage.widthProperty());
        bg.fitHeightProperty().bind(stage.heightProperty());

        // Gradient for the text
        LinearGradient grad = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                // Top gradient color
                new Stop(0, Color.YELLOW),
                // Middle gradient color
                new Stop(0.5, Color.ORANGE),
                // Bottom gradient color
                new Stop(1, Color.RED)
        );

        // Welcome label with username
        String user = app.getConnection().getUsername().toUpperCase();
        Label welcome = new Label("WELCOME " + user + "!");
        // Set the font and size
        welcome.setFont(Font.font("Impact", 80));
        // Fills the gradient
        welcome.setTextFill(grad);

        // Label that notifies user that they are being connected
        Label status = new Label("Connecting to opponent…");
        // Sets the font and size of the text
        status.setFont(Font.font("Impact", 32));
        // Fills the text with the gradient
        status.setTextFill(grad);

        // Load the loading images and avatars
        URL avatarUrl  = LobbyScene.class.getResource("/images/avatar.png");
        URL loadingUrl = LobbyScene.class.getResource("/images/loading.png");
        URL avatar2Url = LobbyScene.class.getResource("/images/avatar2.png");
        // If the images were not found, throw an exception
        if (avatarUrl == null || loadingUrl == null || avatar2Url == null) {
            throw new IllegalStateException("Missing one of: avatar.png, loading.png, avatar2.png");
        }
        // Avatar for the player
        ImageView avatarView  = new ImageView(new Image(avatarUrl.toExternalForm()));
        // Loading icon image
        ImageView loadingView = new ImageView(new Image(loadingUrl.toExternalForm()));
        // Avat for the opponent
        ImageView avatar2View = new ImageView(new Image(avatar2Url.toExternalForm()));
        // Sets the size and ratio for the avatars and loading image
        avatarView.setFitWidth(100); avatarView.setPreserveRatio(true);
        loadingView.setFitWidth(150); loadingView.setPreserveRatio(true);
        avatar2View.setFitWidth(100); avatar2View.setPreserveRatio(true);

        // Horizontal box with spacing
        HBox graphicsBox = new HBox(20, avatarView, loadingView, avatar2View);
        // Centers the elements
        graphicsBox.setAlignment(Pos.CENTER);

        // Vertical layout
        VBox overlay = new VBox(30, welcome, status, graphicsBox);
        // Centers all of the elements
        overlay.setAlignment(Pos.CENTER);
        overlay.setTranslateY(-20);

        // Stack the background
        StackPane root = new StackPane(bg, overlay);
        // Centers all of the elements
        StackPane.setAlignment(overlay, Pos.CENTER);
        // Returns the new scene
        return new Scene(root);
    }
}
//***************************************************************************//
