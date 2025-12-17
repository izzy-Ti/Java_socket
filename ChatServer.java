import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class ChatServer {
    private static final int PORT = 8888;
    public static Set<ClientHandler> clientHandlers = Collections.synchronizedSet(new HashSet<>());
    private static int clientCounter = 0;

    public static void main(String[] args) {
        System.out.println("Chat Server Starting...");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server listening on port " + PORT);
            System.out.println("Waiting for clients...\n");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client: " + socket.getInetAddress().getHostAddress());
                
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandlers.add(clientHandler);
                
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    public static void broadcast(String message, ClientHandler excludeClient) {
        synchronized (clientHandlers) {
            for (ClientHandler client : clientHandlers) {
                if (client != excludeClient) {
                    client.sendMessage(message);
                }
            }
        }
    }

    public static void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
        System.out.println("Client left: " + clientHandler.getUsername());
        System.out.println("Active clients: " + clientHandlers.size() + "\n");
    }

    public static synchronized int getNextClientId() {
        return ++clientCounter;
    }

    public static String getTimestamp() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String username;
    private int clientId;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.clientId = ChatServer.getNextClientId();
        this.username = "User" + clientId;
    }

    @Override
    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            output.println("Welcome to the chat!");
            output.println("Your ID: " + clientId);
            output.println("Commands: /name <newname>, /users, /quit\n");

            ChatServer.broadcast("[" + ChatServer.getTimestamp() + "] " + username + " joined", this);
            System.out.println(username + " joined (ID: " + clientId + ")");

            String message;
            while ((message = input.readLine()) != null) {
                if (message.trim().isEmpty()) continue;

                if (message.startsWith("/")) {
                    handleCommand(message);
                } else {
                    String msg = "[" + ChatServer.getTimestamp() + "] " + username + ": " + message;
                    System.out.println(msg);
                    ChatServer.broadcast(msg, this);
                    output.println("Sent");
                }
            }
        } catch (IOException e) {
            System.err.println("Error with " + username);
        } finally {
            disconnect();
        }
    }

    private void handleCommand(String command) {
        String[] parts = command.split(" ", 2);
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "/name":
                if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                    String oldName = username;
                    username = parts[1].trim();
                    output.println("Name changed to: " + username);
                    ChatServer.broadcast("[" + ChatServer.getTimestamp() + "] " + oldName + " is now " + username, this);
                    System.out.println(oldName + " -> " + username);
                } else {
                    output.println("Usage: /name <newname>");
                }
                break;

            case "/users":
                output.println("Active users: " + ChatServer.clientHandlers.size());
                synchronized (ChatServer.clientHandlers) {
                    for (ClientHandler client : ChatServer.clientHandlers) {
                        output.println("  " + client.getUsername() + (client == this ? " (you)" : ""));
                    }
                }
                break;

            case "/quit":
                output.println("Goodbye!");
                disconnect();
                break;

            default:
                output.println("Unknown command");
        }
    }

    public void sendMessage(String message) {
        if (output != null) {
            output.println(message);
        }
    }

    public String getUsername() {
        return username;
    }

    private void disconnect() {
        try {
            ChatServer.removeClient(this);
            ChatServer.broadcast("[" + ChatServer.getTimestamp() + "] " + username + " left", this);
            
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection");
        }
    }
}
