package edu.uic.cs342.client;

import javafx.application.Platform;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ClientConnection implements Runnable {
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final ClientMain app;
    private String username;

    public ClientConnection(ObjectInputStream in, ObjectOutputStream out, ClientMain app) {
        this.in  = in;
        this.out = out;
        this.app = app;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object msg = in.readObject();
                if (msg instanceof String) {
                    handleMessage((String) msg);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            Platform.runLater(() ->
                    app.switchToGameOver("Connection lost: " + e.getMessage())
            );
        } finally {
            try { in.close(); out.close(); } catch (IOException ignored) {}
        }
    }

    private void handleMessage(String message) {
        if (message.equals("That name is already taken.")) {
            Platform.runLater(() ->
                    LoginScene.getController().showError("Username taken, try another")
            );
        }
        else if (message.equals("Successfully logged in.")) {
            Platform.runLater(app::switchToLobby);
        }
        else if (message.startsWith("START:")) {
            String opponent = message.substring(6);
            Platform.runLater(() -> app.switchToGame(opponent));
        }
        else if (message.equals("Your Move")) {
            Platform.runLater(() ->
                    GameScene.getController().setTurn(true)
            );
        }
        else if (message.equals("Invalid Move")) {
            Platform.runLater(() ->
                    GameScene.getController().showError("Invalid move, try again")
            );
        }
        else if (message.startsWith("Update:")) {
            // format: Update:<playerId>:<row>:<col>
            String[] parts = message.substring(7).split(":");
            int player = Integer.parseInt(parts[0]);
            int row    = Integer.parseInt(parts[1]);
            int col    = Integer.parseInt(parts[2]);
            Platform.runLater(() -> {
                GameScene.getController().handleUpdate(player, row, col);
                // switch turn: if player 1 just moved, now it's player 2's turn (i.e. you if you're player2)
                GameScene.getController().setTurn(player != 1);
            });
        }
        else if (message.startsWith("CHAT:")) {
            // format: CHAT:<fromUsername>:<text>
            String[] parts = message.substring(5).split(":", 2);
            String from = parts[0];
            String text = parts[1];
            Platform.runLater(() -> {
                GameScene.getController().handleChat(from, text);
                MessageScene.getController().addMessage(from, text);
            });
        }
        else if (message.startsWith("Game Over!")) {
            Platform.runLater(() ->
                    app.switchToGameOver(message)
            );
        }
        else if (message.equals("Rematch?")) {
            Platform.runLater(() ->
                    GameOverScene.getController().showRematchPrompt()
            );
        }
    }

    public void send(String msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            Platform.runLater(() ->
                    app.switchToGameOver("Failed to send message: " + e.getMessage())
            );
        }
    }

    public void setUsername(String username) { this.username = username; }
    public String getUsername()               { return username; }
}
