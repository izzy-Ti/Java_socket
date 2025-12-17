# Multi-threaded Chat Application

A Java-based TCP socket chat application demonstrating client-server architecture with concurrent client handling.

## 🎯 Features

### Architecture
- **TCP Protocol**: Reliable connection-oriented communication
- **Client-Server Model**: Separate `ChatServer.java` and `ChatClient.java`
- **Multi-threading**: Server handles multiple clients simultaneously using threads

### Functionality
- ✅ **Two-way Communication**: Clients send messages, server responds and broadcasts
- ✅ **Concurrent Clients**: Multiple users can chat simultaneously
- ✅ **Real-time Broadcasting**: Messages are instantly delivered to all connected clients
- ✅ **User Commands**: 
  - `/name <newname>` - Change your username
  - `/users` - List all active users
  - `/quit` - Disconnect from server
- ✅ **Timestamps**: All messages include time stamps
- ✅ **Connection Notifications**: Users are notified when others join/leave

## 🚀 How to Run

### Step 1: Compile the Java files
```bash
javac ChatServer.java ChatClient.java
```

### Step 2: Start the Server
```bash
java ChatServer
```
The server will start listening on port **8888**.

### Step 3: Start Client(s)
Open new terminal windows and run:
```bash
java ChatClient
```

You can start multiple clients to test concurrent connections!

## 📋 Technical Requirements Met

| Requirement | Implementation |
|-------------|----------------|
| **Architecture** | ✅ TCP sockets with separate Client & Server scripts |
| **Concurrency** | ✅ Server uses threading (`ClientHandler` per client) |
| **Two-way Flow** | ✅ Client sends → Server responds & broadcasts |

## 🔧 How It Works

### Server Side (`ChatServer.java`)
1. Creates a `ServerSocket` on port 8888
2. Accepts incoming client connections in a loop
3. For each client, creates a new `ClientHandler` thread
4. `ClientHandler` manages:
   - Reading client messages
   - Broadcasting to other clients
   - Handling commands
   - Managing disconnections

### Client Side (`ChatClient.java`)
1. Connects to server using `Socket`
2. Creates two threads:
   - **Main thread**: Reads user input and sends to server
   - **Listener thread**: Receives and displays server messages
3. Enables simultaneous sending and receiving (two-way communication)

## 💬 Example Usage

**Client 1:**
```
> Hello everyone!
✓ Message sent
[10:30:15] User2: Hi there!
```

**Client 2:**
```
[10:30:15] User1: Hello everyone!
> Hi there!
✓ Message sent
```

## 🛠️ Configuration

To change the server port, modify these constants:
- In `ChatServer.java`: `private static final int PORT = 8888;`
- In `ChatClient.java`: `private static final int SERVER_PORT = 8888;`

To connect to a remote server, change in `ChatClient.java`:
- `private static final String SERVER_HOST = "localhost";`

## 📝 Code Structure

```
socket/
├── ChatServer.java      # Server with multi-threading
├── ChatClient.java      # Client with two-way communication
└── README.md           # This file
```

## 🎓 Learning Points

- **Socket Programming**: TCP connection establishment
- **Threading**: Concurrent client handling with `Thread` and `Runnable`
- **Synchronization**: Thread-safe collections (`Collections.synchronizedSet`)
- **I/O Streams**: `BufferedReader`, `PrintWriter` for network communication
- **Client-Server Pattern**: Request-response and broadcasting patterns
