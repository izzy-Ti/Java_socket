import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;
    
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private Scanner scanner;
    private volatile boolean running = true;

    public ChatClient() {
        scanner = new Scanner(System.in);
    }

    public void start() {
        try {
            System.out.println("Connecting to " + SERVER_HOST + ":" + SERVER_PORT);
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            System.out.println("Connected!\n");

            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            Thread listenerThread = new Thread(new ServerListener());
            listenerThread.start();

            System.out.println("Start typing (or /quit to exit):\n");
            while (running) {
                if (scanner.hasNextLine()) {
                    String message = scanner.nextLine();
                    
                    if (message.equalsIgnoreCase("/quit")) {
                        running = false;
                        output.println("/quit");
                        break;
                    }
                    
                    if (!message.trim().isEmpty()) {
                        output.println(message);
                    }
                }
            }

        } catch (UnknownHostException e) {
            System.err.println("Server not found");
        } catch (IOException e) {
            System.err.println("Connection error");
        } finally {
            cleanup();
        }
    }

    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                String serverMessage;
                while (running && (serverMessage = input.readLine()) != null) {
                    System.out.println(serverMessage);
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("Lost connection");
                }
            } finally {
                running = false;
            }
        }
    }

    private void cleanup() {
        running = false;
        try {
            System.out.println("\nDisconnecting...");
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
            if (scanner != null) scanner.close();
            System.out.println("Goodbye!");
        } catch (IOException e) {
            System.err.println("Error closing connection");
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Chat Client ===\n");
        
        ChatClient client = new ChatClient();
        client.start();
    }
}
