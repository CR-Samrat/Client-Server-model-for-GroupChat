package abc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatServer {
	//it represent the server side of the chat application
    private ServerSocket serverSocket;
    
    //Map to keep track of connected clients and their print Writer object 
    private Map<String, PrintWriter> clients;

    public ChatServer(int port) {
    	//Listens for client connections on port 8080 using Server Socket
    	
        try {
            serverSocket = new ServerSocket(port);
            clients = new HashMap<>();
            System.out.println("Chat server started on port " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
    	//the server runs in a continuous loop
        while (true) {
            try {
            	//accepting client connections
                Socket socket = serverSocket.accept();
                
                //creating clientHandler thread for each client 
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandler.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void broadcast(String message, PrintWriter sender) {
        for (PrintWriter client : clients.values()) {
            if (client != sender) {
                client.println(message);
            }
        }
    }
    
    private synchronized void listOnlineClients(PrintWriter recipient) {
        recipient.println("Online Clients:");
        for (String client : clients.keySet()) {
            recipient.println("- " + client);
        }
    }

    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter clientWriter;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);

                //when a client connects the ClientHandler reads the clients username and add it to the clients map
                String username = reader.readLine();
                clients.put(username, clientWriter);

                System.out.println("New client connected: " + username);

                //to read messages from the client
                String message;
                while ((message = reader.readLine()) != null) {
                    if (message.startsWith("/private")) {
                    	//if start with 'private' then send the message to the specific user
                        String[] parts = message.split(" ", 3);
                        String recipient = parts[1];
                        String privateMessage = parts[2];
                        sendPrivateMessage(username, recipient, privateMessage);
                    }else if (message.equals("/online")) {
                        listOnlineClients(clientWriter);
                    }else {
                    	//broadcast the message to all clients if it is not private
                        broadcast(username + ": " + message, clientWriter);
                    }
                }

                //remove the client from the map
                clients.remove(username);
                clientSocket.close();

                System.out.println("Client disconnected: " + username);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendPrivateMessage(String sender, String recipient, String message) {
            PrintWriter recipientWriter = clients.get(recipient);
            if (recipientWriter != null) {
                recipientWriter.println("(Private) " + sender + ": " + message);
            }
        }
    }

    public static void main(String[] args) {
    	//start from here
        ChatServer server = new ChatServer(8080);
        server.start();
    }
}

