package com.unsa.proposed;

import java.net.*;
import java.io.*;
import java.util.*;

//The Client that can be run as a console

public class Client {
    // Notification
    private String notif = " *** ";
    // For I/O
    private ObjectInputStream sInput; // To read from the socket
    private ObjectOutputStream sOutput; // To write on the socket
    private Socket socket; // Socket Object
    private String server, username;// Server and Username
    private int port; //Port
    // Recover the User's name
    public String getUsername() {
        return username;
    }
    // Inicialize the User's name
    public void setUsername(String username) {
        this.username = username;
    }
    /*
    * Constructor to set below things
    * Server: The server address
    * Port: The port number
    * Username: The username
    */
    Client(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }
    /*
    * To start the chat
    */
    public boolean start() {
        // try to connect to the server
        try {
            socket = new Socket(server, port);
        }
        catch(Exception ec) { // exception handler if it failed
            display("Error connectiong to server:" + ec);
            return false;
        }
        String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
        display(msg);
        /* Creating both Data Stream */
        try
        {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (IOException eIO) {
            display("Exception creating new Input/output Streams: " + eIO);
            return false;
        }
        // Creates the Thread to listen from the server
        new ListenFromServer().start();
        // Send our username to the server this is the only message that we
        // will send as a String. All other messages will be ChatMessage objects
        try {
            sOutput.writeObject(username);
        }
        catch (IOException eIO) {
            display("Exception doing login : " + eIO);
            disconnect();
            return false;
        }
        // Success we inform the caller that it worked
        return true;
    }
    /*
    * To send a message to the console
    */
    private void display(String msg) {
        System.out.println(msg);
    }
    /*
    * To send a message to the server
    */
    void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        }
        catch(IOException e) {
            display("Exception writing to server: " + e);
        }
    }
    /*
    * When something goes wrong
    * Close the Input/Output streams and disconnect
    */
    private void disconnect() {
        try {
            if (sInput != null) sInput.close();
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            if(sOutput != null) sOutput.close();
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
        try{
            if(socket != null) socket.close();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /*
    * To start the Client in console mode use one of the following command
    * > java Client
    * > java Client username
    * > java Client username portNumber
    * > java Client username portNumber serverAddress
    * at the console prompt
    * If the portNumber is not specified 1500 is used
    * If the serverAddress is not specified "localHost" is used
    * If the username is not specified "Anonymous" is used
    */
    public static void main(String[] args) {
        // Default values if not entered
        int portNumber = 1500;
        String serverAddress = "localhost";
        String userName = "Anonymous";
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter the username: ");
        userName = scan.nextLine();
        // Different case according to the length of the arguments.
        switch(args.length) {
            case 3:
                serverAddress = args[2]; // For > javac Client username portNumber serverAddr
            case 2:
                try {
                    portNumber = Integer.parseInt(args[1]); // For > javac Client username portNumber
                }
                catch(Exception e) {
                    System.out.println("Invalid port number.");
                    System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
                    scan.close();
                    return;
                }
            case 1:
                userName = args[0]; // For > javac Client username
            case 0:
                break; // For > java Client ; If number of arguments are invalid
            default:
                System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
                scan.close();
                return;
        }
        // Create the Client object
        Client client = new Client(serverAddress, portNumber, userName);
        if(!client.start()) { // Try to connect to the server and return if not connected
            scan.close();
            return;
        }
        System.out.println("\nHello.! Welcome to the chatroom.");
        System.out.println("Instructions:");
        System.out.println("1. Simply type the message to send broadcast to all active clients");
        System.out.println("2. Type '@username<space>yourmessage' without quotes to send message to desired client");
        System.out.println("3. Type 'WHOISIN' without quotes to see list of active clients");
        System.out.println("4. Type 'LOGOUT' without quotes to logoff from server");
        while(true) { // Infinite loop to get the input from the user
            System.out.print("> "); // Prompt line
            String msg = scan.nextLine(); // Read message from user
            if (msg.equalsIgnoreCase("LOGOUT")) {
                // Logout if message is LOGOUT
                client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
                break;
            } else if (msg.equalsIgnoreCase("WHOISIN")) {
                // Message to check who are present in chatroom
                client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));
            } else {
                // Regular text message
                client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
            }
        }
        // Close resource
        scan.close();
        // Client completed its job. disconnect client.
        client.disconnect();
    }
    /*
     * A class that waits for the message from the server
    */
    class ListenFromServer extends Thread {
        public void run() {
            while(true) { // Busy waiting
                try {
                // Read the message form the input datastream
                String msg = (String) sInput.readObject();
                // Print the message
                System.out.println(msg);
                System.out.print("> ");
                }
                catch(IOException e) {
                    display(notif + "Server has closed the connection: " + e + notif);
                    break;
                }
                catch(ClassNotFoundException e2) {
                    System.out.println(e2.getMessage());
                }
            }
        }
    }
}