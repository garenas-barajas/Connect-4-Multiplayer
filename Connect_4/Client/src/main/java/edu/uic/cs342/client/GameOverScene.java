package edu.uic.cs342.client;

import java.net.URL;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
//***************************************************************************//
public class GameOverScene {
    private static GameOverScene instance;
    // Game over label
    private final Label resultLabel = new Label();
    // Play again prommpt
    private final Label promptLabel = new Label();
    // Play again button
    private final Button rematchBtn = new Button("PLAY AGAIN");
    // Exit button
    private final Button exitBtn    = new Button("EXIT");
    private final ClientMain app;
    private Scene scene;
    //***************************************************************************//
    // Constructor to set up the graphical components
    private GameOverScene(ClientMain app) {
        this.app = app;
        buildUI();
        instance = this;
    }
    //***************************************************************************//
    // Creates and returns scene
    public static Scene build(ClientMain app) {
        new GameOverScene(app);
        return instance.scene;
    }
    //***************************************************************************//
    // Method to get access to the controller instance
    public static GameOverScene getController() {
        return instance;
    }
    //***************************************************************************//
    // Build the graphical elements
    private void buildUI() {
        Stage stage = app.getPrimaryStage();

        // Loads the ackground image and sets it on the screen to fit the boungs
        URL bgUrl = GameOverScene.class.getResource("/images/star-background.png");
        // If the image is not avaiable throw exception
        if (bgUrl == null) throw new IllegalStateException("Missing /images/star-background.png");
        ImageView bg = new ImageView(new Image(bgUrl.toExternalForm()));
        bg.setPreserveRatio(false);
        bg.fitWidthProperty().bind(stage.widthProperty());
        bg.fitHeightProperty().bind(stage.heightProperty());

        // Text gradient
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
        // Font, size, and color of the GAME OVER label
        resultLabel.setText("GAME OVER");
        resultLabel.setFont(Font.font("Impact", 90));
        resultLabel.setTextFill(gradient);
        resultLabel.setStyle("-fx-background-color:black; -fx-padding:5px; -fx-background-radius:5px;");

        // // Font, size, and color of the smaller play again label
        promptLabel.setText("PLAY AGAIN?");
        promptLabel.setFont(Font.font("Verdana", 24));
        promptLabel.setTextFill(gradient);
        promptLabel.setStyle("-fx-background-color:black; -fx-padding:3px; -fx-background-radius:3px;");

        // Font, size, and color of the Play AGAIN button
        rematchBtn.setFont(Font.font("Impact", 48));
        rematchBtn.setTextFill(gradient);
        rematchBtn.setStyle("-fx-background-color:black; -fx-padding:5px; -fx-background-radius:5px;");
        rematchBtn.setOnAction(e -> app.getConnection().send("YES"));
        // Font, size, and color of the EXIT button
        exitBtn.setFont(Font.font("Impact", 48));
        exitBtn.setTextFill(gradient);
        exitBtn.setStyle("-fx-background-color:black; -fx-padding:5px; -fx-background-radius:5px;");
        exitBtn.setOnAction(e -> {
            // Sends NO on click
            app.getConnection().send("NO");
            // Goes back to the login scene
            Scene login = LoginScene.build(app);
            app.getPrimaryStage().setTitle("Connect4 — Login");
            app.getPrimaryStage().setScene(login);
        });
        // Vertical layout for buttons and labels
        VBox box = new VBox(20, resultLabel, promptLabel, rematchBtn, exitBtn);
        box.setAlignment(Pos.CENTER);
        // Sets background
        StackPane root = new StackPane(bg, box);
        StackPane.setAlignment(box, Pos.CENTER);
        // Sets the scene size
        scene = new Scene(root, 800, 600);
    }
//***************************************************************************//
    // Method to set the game results (who won)
    public void setResult(String result) {
        Platform.runLater(() -> resultLabel.setText(result.toUpperCase()));
    }
//***************************************************************************//
    // Method to show the rematch prompt
    public void showRematchPrompt() {
        Platform.runLater(() -> promptLabel.setText("Waiting for opponent..."));
    }
}
//***************************************************************************//