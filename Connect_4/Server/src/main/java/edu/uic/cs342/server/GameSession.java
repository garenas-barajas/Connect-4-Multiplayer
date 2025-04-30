package edu.uic.cs342.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
//***************************************************************************//
public class GameSession implements Runnable {
    private final int sessionId;
    private final Socket p1, p2;
    private ObjectInputStream in1, in2;
    private ObjectOutputStream out1, out2;
    private String user1, user2;

    // Queues for  messages from each player
    private final BlockingQueue<String> queue1 = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> queue2 = new LinkedBlockingQueue<>();

    public GameSession(int sessionId, Socket p1, Socket p2) {
        this.sessionId = sessionId;
        this.p1 = p1;
        this.p2 = p2;
    }

    @Override
    public void run() {
        try {
            //set up streams
            out1 = new ObjectOutputStream(p1.getOutputStream());
            in1  = new ObjectInputStream(p1.getInputStream());
            out2 = new ObjectOutputStream(p2.getOutputStream());
            in2  = new ObjectInputStream(p2.getInputStream());

            // login handshake
            user1 = waitForLogin(in1, out1);
            user2 = waitForLogin(in2, out2);

            //notify both to start first game
            out1.writeObject("START:" + user2);
            out1.flush();
            out2.writeObject("START:" + user1);
            out2.flush();

            // kick off chat–message readers
            startChatReaders();

            //play & rematch loop
            boolean bothWantRematch = true;
            while (bothWantRematch) {
                playOneGame();

                // ask for rematch
                out1.writeObject("Rematch?");
                out1.flush();
                out2.writeObject("Rematch?");
                out2.flush();

                // pull their answers from the queues
                String ans1 = queue1.take();
                String ans2 = queue2.take();
                bothWantRematch = "YES".equalsIgnoreCase(ans1)
                        && "YES".equalsIgnoreCase(ans2);

                if (bothWantRematch) {
                    out1.writeObject("START:" + user2);
                    out1.flush();
                    out2.writeObject("START:" + user1);
                    out2.flush();
                    // clear any leftover messages so old moves don’t bleed through
                    queue1.clear();
                    queue2.clear();
                }
            }
        } catch (Exception e) {
            ServerGUI.log("Session " + sessionId + " error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
            Connect4Server.endSession(sessionId);
        }
    }
    //***************************************************************************//
    private String waitForLogin(ObjectInputStream in, ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
        while (true) {
            String msg = (String) in.readObject();
            if (!msg.startsWith("LOGIN:")) throw new IOException("Bad login");
            String name = msg.substring(6);

            if (!Connect4Server.registerUsername(name)) {
                ServerGUI.log("Session " + sessionId + ": username taken '" + name + "'");
                out.writeObject("That name is already taken.");
                out.flush();
                continue;
            }
            ServerGUI.log("Session " + sessionId + ": '" + name + "' logged in");
            out.writeObject("Successfully logged in.");
            out.flush();
            return name;
        }
    }
    //***************************************************************************//
    private void startChatReaders() {
        // Reader for player 1
        new Thread(() -> {
            try {
                while (true) {
                    String msg = (String) in1.readObject();
                    if (msg.startsWith("CHAT:")) {
                        String text = msg.substring(5);
                        broadcastChat(user1, text);
                    } else {
                        queue1.put(msg);
                    }
                }
            } catch (Exception e) { /* connection closed or error */ }
        }, "ChatReader-" + sessionId + "-" + user1).start();

        // Reader for player 2
        new Thread(() -> {
            try {
                while (true) {
                    String msg = (String) in2.readObject();
                    if (msg.startsWith("CHAT:")) {
                        String text = msg.substring(5);
                        broadcastChat(user2, text);
                    } else {
                        queue2.put(msg);
                    }
                }
            } catch (Exception e) { /* connection closed or error */ }
        }, "ChatReader-" + sessionId + "-" + user2).start();
    }
    //***************************************************************************//
    private void broadcastChat(String from, String text) throws IOException {
        String outMsg = "CHAT:" + from + ":" + text;
        out1.writeObject(outMsg); out1.flush();
        out2.writeObject(outMsg); out2.flush();
        ServerGUI.log("Session " + sessionId + " CHAT from " + from + ": " + text);
    }
    //***************************************************************************//
    private void playOneGame() throws Exception {
        ServerGUI.log("Session " + sessionId + ": game start");
        Connect4Board board = new Connect4Board();
        boolean playerOneTurn = true;

        while (true) {
            ObjectOutputStream currentOut = playerOneTurn ? out1 : out2;
            ObjectOutputStream otherOut   = playerOneTurn ? out2 : out1;
            BlockingQueue<String> currentQueue = playerOneTurn ? queue1 : queue2;

            // Notify players
            currentOut.writeObject("Your Move");
            currentOut.flush();
            otherOut.writeObject("WAIT");
            otherOut.flush();
            ServerGUI.log("Session " + sessionId
                    + ": " + (playerOneTurn ? user1 : user2) + "'s turn");

            // fetch their next non‐chat message
            String cmd = currentQueue.take();
            if (!cmd.startsWith("Move:")) {
                throw new IOException("Expected Move:, got " + cmd);
            }

            int col = Integer.parseInt(cmd.substring(5));
            ServerGUI.log("Session " + sessionId
                    + ": " + (playerOneTurn ? user1 : user2) + " drops in col " + col);

            // apply it
            int row = board.dropToken(col, playerOneTurn ? 1 : 2);
            if (row < 0) {
                currentOut.writeObject("Invalid Move");
                currentOut.flush();
                continue;
            }

            // broadcast update
            String update = "Update:" + (playerOneTurn ? 1 : 2)
                    + ":" + row + ":" + col;
            out1.writeObject(update); out1.flush();
            out2.writeObject(update); out2.flush();
            ServerGUI.log("Session " + sessionId
                    + ": updated row=" + row + " col=" + col);

            if (board.checkWin(row, col)) {
                String win = "Game Over! Winner:" + (playerOneTurn ? user1 : user2);
                out1.writeObject(win); out1.flush();
                out2.writeObject(win); out2.flush();
                ServerGUI.log("Session " + sessionId + ": win by "
                        + (playerOneTurn ? user1 : user2));
                break;
            }
            if (board.isFull()) {
                String draw = "Game Over! Draw.";
                out1.writeObject(draw); out1.flush();
                out2.writeObject(draw); out2.flush();
                ServerGUI.log("Session " + sessionId + ": draw");
                break;
            }

            // switch turns
            playerOneTurn = !playerOneTurn;
        }
    }
    //***************************************************************************//
    private ObjectOutputStream getCurrentOut(boolean playerOneTurn) {
        return playerOneTurn ? out1 : out2;
    }
    //***************************************************************************//
    private void cleanup() {
        try { p1.close(); } catch (IOException ignored) {}
        try { p2.close(); } catch (IOException ignored) {}
        if (user1 != null) Connect4Server.unregisterUsername(user1);
        if (user2 != null) Connect4Server.unregisterUsername(user2);
    }
}
//***************************************************************************//
