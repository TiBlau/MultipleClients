import java.io.*;
import java.util.ArrayList;
import java.net.*;

public class ClientHandler implements Runnable{
    // keeps track of all Clients to send message to all
    // of them
    public static ArrayList<ClientHandler> clientHandlers =
            new ArrayList<>();

    // Socket passed form Server Class to establish connection
    // between Client and Server
    private Socket socket;

    // read data that has been sent from the client
    private BufferedReader bufferedReader;

    // write data sent from client
    private BufferedWriter bufferedWriter;

    private String clientUsername;

    // accept passing
    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            /*
                In Java there are two types of Streams existing
                1. Byte Stream
                2. Character Stream
                We want to get the messages as readable texts, so we need
                to wrap the byte Stream with a OutputStreamWriter to see the text
                as we want to.
                We Buffer the stream to make it more efficient.
             */
            //send messages
            this.bufferedWriter =
                    new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            //receive messages
            this.bufferedReader =
                    new BufferedReader(new InputStreamReader(socket.getInputStream()));

            this.clientUsername = bufferedReader.readLine();
            // add new User to array List
            clientHandlers.add(this);
            // message to everyone that new user has entered the chat
            broadcastMessage("Server: " + clientUsername + " has entered the Chat.");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }


    /*
        Everything in here is what is run on a seperate Thread
        We want to listen for messages in here, so the main thread doesn't get stuck.
        Also we don't want to wait for messages until we can send one.
     */
    @Override
    public void run() {
        // holds message received from a client
        String messageFromClient;

        while (socket.isConnected()) {
            try {
                // read from buffered reader for messages whilst listening
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }

    }

    /*
        Send message quiet road to everyone in the Group Chat
     */
    public void broadcastMessage(String messageToSend) {
        //Loop through array list
        // goes through each clientHandler for each iteration
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                // broadcast to everyone except the one who sent this
                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    // send message that is passed in
                    clientHandler.bufferedWriter.write(messageToSend);
                    // gives new line character
                    clientHandler.bufferedWriter.newLine();
                    // sending messages before the buffer is actually full
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }
    // send disconnection
    public void removeClientHandler() {
        // remove clientHandler from arrayList
        clientHandlers.remove(this);
        broadcastMessage("Server: " + clientUsername + " has left the chat." );
    }

    // End connection
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
