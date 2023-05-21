package com.unsa.proposed;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

// The server that can be run as a console
public class Server {
    // A unique ID for each connection
    private static int uniqueId;
    // An ArrayList to keep the list of the Client
    private ArrayList<ClientThread> al;
    // To display time
    private SimpleDateFormat sdf;
    // The port number to listen for connection
    private int port;
    // To check if server is running
    private boolean keepGoing;
    // Notification
    private String notif = " *** ";
    //Constructor that receive the port to listen to for connection as parameter
    public Server(int port) {
        // The port
        this.port = port;
        // To display time as format hh:mm:ss
        sdf = new SimpleDateFormat("HH:mm:ss");
        // An ArrayList to keep the list of the Client
        al = new ArrayList<ClientThread>();
    }
    public void start() {
        keepGoing = true;
        // Create socket server and wait for connection requests
        try {
            // The socket used by the server
            ServerSocket serverSocket = new ServerSocket(port);
            // Infinite loop to wait for connections (till server is active)
            while (keepGoing) {
                display("Server waiting for Clients on port " + port + ".");
                // Accept connection if requested from client
                Socket socket = serverSocket.accept();
                // Break if server stoped
                if (!keepGoing) break;
                // If client is connected, create its thread
                ClientThread t = new ClientThread(socket);
                // Add this client to arraylist
                al.add(t);
                t.start();
            }
            // Try to stop the server
            try {
                serverSocket.close();
                for (int i = 0 ; i < al.size() ; ++i) {
                    ClientThread tc = al.get(i);
                    try {
                        // Close all data streams and socket
                        tc.sInput.close();
                        tc.sOutput.close();
                        tc.socket.close();
                    } catch (IOException ioE) {
                        display(ioE.getMessage());
                    }
                } 
            } catch (Exception e) {
                display("Exception closing the server and clients: " + e);
            }
        } catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
            display(msg);
        }
    }
    // To stop the server
    protected void stop() {
        keepGoing = false;
        try {
            Socket s = new Socket("localhost", port);
            s.close();
        } catch (Exception e) {
            display(e.getMessage());
        }
    }
    // Display an event to the console
    private void display(String msg) {
        String time = sdf.format(new Date()) + " " + msg;
        System.out.println(time);
    }
    // To broadcast a message to all Clients
    private synchronized boolean broadcast(String message) {
        // Add timestamp to the message
        String time = sdf.format(new Date());
        // To check if message is private i.e. Client to Client message
        String[] w = message.split(" ",3);
        boolean isPrivate = false;
        if (w[1].charAt(0) == '@')
            isPrivate = true;
        // If private message, send message to mentioned username only
        if (isPrivate == true) {
            String tocheck = w[1].substring(1, w[1].length());
            message = w[0] + w[2];
            String messageLf = time + " " + message + "\n";
            boolean found = false;
            // We loop in reverse order to find the mentioned username
            for (int y = al.size() ; --y >= 0 ; ) {
                ClientThread ct1 = al.get(y);
                String check = ct1.getUsername();
                if (check.equals(tocheck)) {
                    // Try to write to the Client if it fails remove it from the list
                    if (!ct1.writeMsg(messageLf)) {
                        al.remove(y);
                        display("Disconnected Client " + ct1.username + " removed from list.");
                    }
                    // Username found and delivered the message
                    found = true;
                    break;
                }
            }
            // Mentioned user not found, return false
            if (found != true) {
                return false;
            }
        } else { // If message is a broadcast message
            String messageLf = time + " " + message + "\n";
            // Display message
            System.out.print(messageLf);
            // We loop in reverse order in case we would have to remove a Client
            // Because it has disconnected
            for (int i = al.size() ; --i >= 0 ; ) {
                ClientThread ct = al.get(i);
                // Try to write to the Client if it fails remove it from the list
                if (!ct.writeMsg(messageLf)) {
                    al.remove(i);
                    display("Disconnected Client " + ct.username + " removed from list.");
                }
            }
        }
        return true;
    }
    // If client sent LOGOUT message to exit
    synchronized void remove(int id) {
        String disconnectedClient = "";
        // Scan the array list until we found the Id
        for (int i = 0 ; i < al.size() ; ++i) {
            ClientThread ct = al.get(i);
            // If found remove it
            if (ct.id == id) {
                disconnectedClient = ct.getUsername();
                al.remove(i);
                break;
            }
        }
        broadcast(notif + disconnectedClient + " has left the chat room." + notif);
    }
    /*
    * To run as a console application
    * > java Server
    * > java Server portNumber
    * If the port number is not specified 1500 is used
    */
    public static void main(String[] args) {
        // Start server on port 1500 unless a PortNumber is specified
        int portNumber = 1500;
        switch (args.length) {
            case 1:
                try {
                    portNumber = Integer.parseInt(args[0]);
                } catch (Exception e) {
                    System.out.println("Invalid port number.");
                    System.out.println("Usage is: > java Server [portNumber]");
                    return;
                }
            case 0:
                break;
            default:
                System.out.println("Usage is: > java Server [portNumber]");
                return;
        }
        // Create a server object and start it
        Server server = new Server(portNumber);
        server.start();
    }
    // One instance of this thread will run for each client
    class ClientThread extends Thread {
        // The socket to get messages from client
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        // My unique id (easier for deconnection)
        int id;
        // The Username of the Client
        String username;
        // Message object to recieve message and its type
        ChatMessage cm;
        // Timestamp
        String date;
        // Constructor
        ClientThread(Socket socket) {
            // A unique id
            id = ++uniqueId;
            this.socket = socket;
            // Creating both Data Stream
            System.out.println("Thread trying to create Object Input/Output Streams");
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                // Read the username
                username = (String) sInput.readObject();
                broadcast(notif + username + " has joined the chat room." + notif);
            } catch (IOException e) {
                display("Exception creating new Input/output Streams: " + e);
                return;
            } catch (ClassNotFoundException e) {
                System.out.println(e.getMessage());
            }
            date = new Date().toString() + "\n";
        }
        public String getUsername() {
            return username;
        }
        public void setUsername(String username) {
            this.username = username;
        }
        // Infinite loop to read and forward message
        public void run() {
            // To loop until LOGOUT
            boolean keepGoing = true;
            while (keepGoing) {
                // Read a String (which is an object)
                try {
                    cm = (ChatMessage) sInput.readObject();
                } catch (IOException e) {
                    display(username + " Exception reading Streams: " + e);
                    break;
                } catch (ClassNotFoundException e2) {
                    System.out.println(e2);
                    break;
                }
                // Get the message from the ChatMessage object received
                String message = cm.getMessage();
                // Different actions based on type message
                switch (cm.getType()) {
                    case ChatMessage.MESSAGE:
                        boolean confirmation = broadcast(username + ": " + message);
                        if (confirmation == false) {
                            String msg = notif + "Sorry. No such user exists." + notif;
                            writeMsg(msg);
                        }
                        break;
                    case ChatMessage.LOGOUT:
                        display(username + " disconnected with a LOGOUT message.");
                        keepGoing = false;
                        break;
                    case ChatMessage.WHOISIN:
                        writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");         
                        // Send list of active clients
                        for (int i = 0 ; i < al.size() ; ++i) {
                            ClientThread ct = al.get(i);
                            writeMsg((i+1) + ") " + ct.username + " since " + ct.date);
                        }
                        break;
                }
            }
            // If out of the loop then disconnected and remove from client list
            remove(id);
            close();
        }
        // Close everything
        private void close() {
            try {
                if(sOutput != null) sOutput.close();
            } catch(Exception e) {
                System.out.println(e);
            }
            try {
                if(sInput != null) sInput.close();
            } catch(Exception e) {
                System.out.println(e);
            };
            try {
                if(socket != null) socket.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        // Write a String to the Client output stream
        private boolean writeMsg(String msg) {  
            // If Client is still connected send the message to it
            if (!socket.isConnected()) {
                close();
                return false;
            }
            // write the message to the stream
            try {
                sOutput.writeObject(msg);
            } catch(IOException e) { // if an error occurs, do not abort just inform the user
                display(notif + "Error sending message to " + username + notif);
                display(e.toString());
            }
            return true;
        }
    }
}