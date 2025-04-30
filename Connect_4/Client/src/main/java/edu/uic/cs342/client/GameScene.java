package edu.uic.cs342.client;

import java.net.URL;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
//***************************************************************************//
public class GameScene {
    private static final int ROWS = 6, COLS = 7;
    private static GameScene instance;

    private final ClientMain app;
    private boolean myTurn;
    private Scene scene;

    private final Button[][] cellButtons = new Button[ROWS][COLS];
    private final Label turnLabel = new Label("Waiting...");
    private final Label errorLabel = new Label();
    private final TextArea chatArea = new TextArea();
    private final TextField chatInput = new TextField();
    private final Button sendBtn = new Button("Send");
    private final Button chatBtn = new Button("View Chat History");
    //***************************************************************************//
    private GameScene(ClientMain app) {
        this.app = app;
        buildUI();
        instance = this;
    }
    //***************************************************************************//
    public static Scene build(ClientMain app) {
        new GameScene(app);
        return instance.scene;
    }
    //***************************************************************************//
    public static GameScene getController() {
        return instance;
    }
    //***************************************************************************//
    private void buildUI() {
        Stage stage = app.getPrimaryStage();

        // Starry background
        URL bgUrl = GameScene.class.getResource("/images/star-background.png");
        if (bgUrl == null) throw new IllegalStateException("Missing /images/star-background.png");
        ImageView bg = new ImageView(new Image(bgUrl.toExternalForm()));
        bg.setPreserveRatio(false);
        bg.fitWidthProperty().bind(stage.widthProperty());
        bg.fitHeightProperty().bind(stage.heightProperty());

        // Gradient for text
        LinearGradient grad = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.YELLOW),
                new Stop(0.5, Color.ORANGE),
                new Stop(1, Color.RED)
        );

        // Build Connect4 grid
        GridPane grid = new GridPane();
        grid.setGridLinesVisible(true);
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Button cell = new Button();
                cell.setPrefSize(50, 50);
                cell.setFont(Font.font("Impact", 18));
                cell.setTextFill(grad);
                cell.setFocusTraversable(false);
                final int col = c;
                cell.setOnAction(e -> {
                    if (myTurn) {
                        app.getConnection().send("Move:" + col);
                        setTurn(false);
                    }
                });
                cellButtons[r][c] = cell;
                grid.add(cell, c, r);
            }
        }

        // Style turn and error labels
        turnLabel.setFont(Font.font("Impact", 16));
        turnLabel.setTextFill(grad);
        errorLabel.setFont(Font.font("Impact", 16));
        errorLabel.setTextFill(grad);

        // Chat area setup
        chatArea.setEditable(false);
        chatArea.setPrefHeight(150);
        chatArea.setFont(Font.font("Impact", 14));
        chatInput.setPromptText("Type message…");
        chatInput.setFont(Font.font("Impact", 14));
        sendBtn.setFont(Font.font("Impact", 16));
        sendBtn.setTextFill(grad);
        sendBtn.setOnAction(e -> {
            String text = chatInput.getText().trim();
            if (!text.isEmpty()) {
                app.getConnection().send("CHAT:" + text);
                chatInput.clear();
            }
        });
        chatBtn.setFont(Font.font("Impact", 16));
        chatBtn.setTextFill(grad);
        chatBtn.setOnAction(e -> app.switchToMessage());

        // Chat input and send button HBox with dynamic sizing
        HBox chatControls = new HBox(5, chatInput, sendBtn);
        chatControls.setAlignment(Pos.CENTER);
        HBox.setHgrow(chatInput, Priority.ALWAYS);
        chatInput.setPrefWidth(Region.USE_COMPUTED_SIZE);

        // Chat box layout
        VBox chatBox = new VBox(5, chatArea, chatControls);
        chatBox.setAlignment(Pos.CENTER);

        // center both grid and chat horizontally
        HBox main = new HBox(10, grid, chatBox);
        main.setAlignment(Pos.CENTER);
        main.setPadding(new Insets(10));

        // Vertical layout
        VBox rootContent = new VBox(10, turnLabel, errorLabel, main, chatBtn);
        rootContent.setAlignment(Pos.CENTER);
        rootContent.setPadding(new Insets(10));

        // Stack background and content
        StackPane root = new StackPane(bg, rootContent);
        StackPane.setAlignment(rootContent, Pos.CENTER);
        scene = new Scene(root, 750, 500);
    }

    //***************************************************************************//
    public void setTurn(boolean yourTurn) {
        myTurn = yourTurn;
        Platform.runLater(() -> {
            turnLabel.setText(yourTurn ? "Your turn" : "Opponent's turn");
            errorLabel.setText("");
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    Button cell = cellButtons[r][c];
                    if (cell.getText().isEmpty()) {
                        cell.setDisable(!yourTurn);
                    }
                }
            }
        });
    }
    //***************************************************************************//
    public void handleUpdate(int player, int row, int col) {
        Platform.runLater(() -> {
            Button cell = cellButtons[row][col];
            cell.setText(player == 1 ? "X" : "O");
            cell.setDisable(true);
        });
    }
    //***************************************************************************//
    public void handleChat(String from, String text) {
        Platform.runLater(() -> chatArea.appendText(from + ": " + text + "\n"));
    }
    //***************************************************************************//
    public void showError(String message) {
        Platform.runLater(() -> errorLabel.setText(message));
    }
}
