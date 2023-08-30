package abc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {
	//It represent the client side of the application
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    public ChatClient(String serverAddress, int serverPort) {
        try {
        	//connects to the server using socket 
            socket = new Socket(serverAddress, serverPort);
            
            //printWriter and buffered reader to send and receive messages
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
    	//this method prompts the user to enter a username and send it to the server 
        try {
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Enter your username: ");
            String username = consoleReader.readLine();
            writer.println(username);

            MessageReader messageReader = new MessageReader();
            messageReader.start();

            String message;
            while ((message = consoleReader.readLine()) != null) {
                writer.println(message);
            }

            //if  console is closed or input null then client will be disconnected
            messageReader.interrupt();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class MessageReader extends Thread {
        @Override
        public void run() {
            try {
                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        String serverAddress = "localhost"; 
        int serverPort = 8080; 

        ChatClient client = new ChatClient(serverAddress, serverPort);
        client.start();
    }
}
