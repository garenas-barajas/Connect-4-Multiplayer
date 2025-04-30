package edu.uic.cs342.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientMain extends Application {
    private Stage primaryStage;
    private ClientConnection connection;

    private Scene loginScene;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double width  = bounds.getWidth() * 0.8;
        double height = bounds.getHeight() * 0.8;
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);
        primaryStage.centerOnScreen();

        // Build the login scene here
        loginScene = LoginScene.build(this);
        primaryStage.setTitle("Connect4 — Login");
        primaryStage.setScene(loginScene);
        primaryStage.show();

        // Now initialize the network in a background thread
        new Thread(this::initNetwork, "net-init").start();
    }

    private void initNetwork() {
        try {
            Socket socket = new Socket("localhost", 5555);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in  = new ObjectInputStream(socket.getInputStream());
            connection = new ClientConnection(in, out, this);
            new Thread(connection, "net-reader").start();
        } catch (IOException e) {
            Platform.runLater(() ->
                    primaryStage.setScene(createErrorScene("Failed to connect: " + e.getMessage()))
            );
        }
    }

    public void switchToLobby() {
        Platform.runLater(() -> {
            // Now that connection exists build the lobby scene
            Scene lobbyScene = LobbyScene.build(this);
            primaryStage.setTitle("Connect4 — Lobby");
            primaryStage.setScene(lobbyScene);
        });
    }

    public void switchToGame(String opponent) {
        Platform.runLater(() -> {
            Scene gameScene = GameScene.build(this);
            primaryStage.setTitle("Connect4 vs " + opponent);
            primaryStage.setScene(gameScene);
        });
    }

    public void switchToGameOver(String result) {
        Platform.runLater(() -> {
            // build and then set the result text
            Scene gameOverScene = GameOverScene.build(this);
            GameOverScene.getController().setResult(result);
            primaryStage.setTitle("Connect4 — Game Over");
            primaryStage.setScene(gameOverScene);
        });
    }

    public void switchToMessage() {
        Platform.runLater(() -> {
            Scene msgScene = MessageScene.create(this);
            primaryStage.setTitle("Connect4 — Chat");
            primaryStage.setScene(msgScene);
        });
    }

    public ClientConnection getConnection() {
        return connection;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private Scene createErrorScene(String message) {
        javafx.scene.control.Label label = new javafx.scene.control.Label(message);
        javafx.scene.control.Button exit  = new javafx.scene.control.Button("Exit");
        exit.setOnAction(e -> Platform.exit());
        javafx.scene.layout.VBox root      = new javafx.scene.layout.VBox(10, label, exit);
        root.setAlignment(javafx.geometry.Pos.CENTER);
        return new Scene(root, 400, 200);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

