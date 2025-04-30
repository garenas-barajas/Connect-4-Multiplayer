package edu.uic.cs342.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
//***************************************************************************//
public class Connect4Server {
    // Contains currently logged in usernames
    private static final Set<String> loggedInUsers =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    // thread‐safe queue of waiting clients
    private static final BlockingQueue<Socket> waitingClients =
            new LinkedBlockingQueue<>();

    // Countes each game session a unique ID
    private static final AtomicInteger sessionCount = new AtomicInteger(0);

    // Active sessions maps session IDs to their handling threads
    private static final ConcurrentMap<Integer, Thread> activeSessions =
            new ConcurrentHashMap<>();
    //***************************************************************************//
    // Entry point of the server
    public static void main(String[] args) {
        ServerGUI.init();
        int port = 5555;
        ServerGUI.log("Server listening on port " + port);
        // Creates a serverSocket
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                // Waiting for the client
                Socket clientSocket = serverSocket.accept();
                ServerGUI.log("Client connected: " + clientSocket.getRemoteSocketAddress());
                // Add to the waitng queue
                waitingClients.put(clientSocket);
                // Matches two clients
                matchClients();
            }
            // Handles any thread interruption
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            ServerGUI.log("Server interrupted, shutting down...");
            // Handles any I/O errors
        } catch (IOException ioe) {
            ServerGUI.log("Server I/O error: " + ioe.getMessage());
            ioe.printStackTrace();
        }
    }
    //***************************************************************************//
    // Function to  pair two clients
    private static void matchClients() throws InterruptedException {
        // Wont pair if the amount of clients is less than two
        if (waitingClients.size() < 2) return;
        // Removes the first waiting client
        Socket p1 = waitingClients.take();
        // Removes the second waiting client
        Socket p2 = waitingClients.take();

        ServerGUI.log("Pairing " +
                p1.getRemoteSocketAddress() + " ↔ " + p2.getRemoteSocketAddress());

        // Unique ID for the session
        int sessionId = sessionCount.getAndIncrement();
        // Creates a new session handler
        GameSession session = new GameSession(sessionId, p1, p2);
        // New thread for the game logic
        Thread t = new Thread(session, "GameSession-" + sessionId);
        // Tracks the active session
        activeSessions.put(sessionId, t);
        // Starts to handle the game
        t.start();
    }
    //***************************************************************************//
    // Method to register a username
    public static boolean registerUsername(String name) {
        // Returns false if the username is already present
        return loggedInUsers.add(name);
    }
    //***************************************************************************//
    // Method to remove a username if they disconnect
    public static void unregisterUsername(String name) {
        loggedInUsers.remove(name);
    }
    //***************************************************************************//
    // Method to remove session ID from active map
    public static void endSession(int sessionId) {
        activeSessions.remove(sessionId);
        ServerGUI.log("Session " + sessionId +
                " ended. Active sessions: " + activeSessions.keySet());
    }
}
//***************************************************************************//
