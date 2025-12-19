import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatClientGUI extends JFrame {
    // UI Components
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JTextField usernameField;
    private JButton changeNameButton;
    
    // Networking
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private volatile boolean connected = false;
    
    // Colors
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private static final Color SECONDARY_COLOR = new Color(240, 248, 255);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);
    
    public ChatClientGUI() {
        setTitle("Chat Application");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initializeComponents();
        setupLayout();
        setupListeners();
        
        setVisible(true);
        
        // Auto-connect on startup
        connect();
    }
    
    private void initializeComponents() {
        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setMargin(new Insets(10, 10, 10, 10));
        chatArea.setBackground(Color.WHITE);
        chatArea.setForeground(TEXT_COLOR);
        
        // Message field
        messageField = new JTextField();
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        // Send button
        sendButton = new JButton("Send");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setBackground(PRIMARY_COLOR);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Username field
        usernameField = new JTextField(15);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        // Change name button
        changeNameButton = new JButton("Change Name");
        changeNameButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        changeNameButton.setBackground(PRIMARY_COLOR);
        changeNameButton.setForeground(Color.WHITE);
        changeNameButton.setFocusPainted(false);
        changeNameButton.setBorderPainted(false);
        changeNameButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Top panel - Username
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(SECONDARY_COLOR);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel nameLabel = new JLabel("Your Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        topPanel.add(nameLabel);
        topPanel.add(usernameField);
        topPanel.add(changeNameButton);
        
        // Center panel - Chat area
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 15, 0, 15),
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1)
        ));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        // Bottom panel - Message input
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        bottomPanel.setBackground(Color.WHITE);
        
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        bottomPanel.add(inputPanel, BorderLayout.CENTER);
        
        // Add panels to frame
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void setupListeners() {
        // Change name button
        changeNameButton.addActionListener(e -> changeName());
        
        // Enter key in username field
        usernameField.addActionListener(e -> changeName());
        
        // Send button
        sendButton.addActionListener(e -> sendMessage());
        
        // Enter key in message field
        messageField.addActionListener(e -> sendMessage());
        
        // Window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnect();
            }
        });
    }
    
    private void connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            
            connected = true;
            
            // Start listener thread
            Thread listenerThread = new Thread(new ServerListener());
            listenerThread.setDaemon(true);
            listenerThread.start();
            
            messageField.requestFocus();
            
        } catch (IOException ex) {
            appendToChat("Connection failed. Please make sure the server is running.\n\n");
            JOptionPane.showMessageDialog(this, 
                "Could not connect to server. Please make sure the server is running.", 
                "Connection Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void disconnect() {
        if (connected) {
            try {
                connected = false;
                if (output != null) {
                    output.println("/quit");
                }
                if (input != null) input.close();
                if (output != null) output.close();
                if (socket != null) socket.close();
            } catch (IOException ex) {
                // Ignore errors during disconnect
            }
        }
    }
    
    private void changeName() {
        String newName = usernameField.getText().trim();
        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a name", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (connected) {
            output.println("/name " + newName);
        }
    }
    
    private void sendMessage() {
        if (!connected) {
            JOptionPane.showMessageDialog(this, "Not connected to server", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            return;
        }
        
        output.println(message);
        messageField.setText("");
        messageField.requestFocus();
    }
    
    private void appendToChat(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message);
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                String serverMessage;
                while (connected && (serverMessage = input.readLine()) != null) {
                    appendToChat(serverMessage + "\n");
                }
            } catch (IOException e) {
                if (connected) {
                    appendToChat("\nLost connection to server\n\n");
                }
            }
        }
    }
    
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create and show GUI
        SwingUtilities.invokeLater(() -> new ChatClientGUI());
    }
}
