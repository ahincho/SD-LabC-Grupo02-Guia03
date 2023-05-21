package com.unsa.solved;

import java.io.*;
import java.net.*;

public class Servidor {
    static final int PORT = 5000; // Port where Server is gonna run
    static final int MAX_CLIENTS = 3; // Max Clients Sockets to serve
    public Servidor() {
        try {
            ServerSocket skServidor = new ServerSocket(PORT); // Create the new Server Socket
            System.out.println("Listening in Port: " + PORT); // Where Server is running
            for (int numCli = 0 ; numCli < MAX_CLIENTS ; numCli++) { // For to serve Clients
                Socket skCliente = skServidor.accept(); // Accept a new Client Socket
                System.out.println("Serving to Client " + numCli); // Print the number of Client
                OutputStream out = skCliente.getOutputStream(); // Getting the Client Output
                DataOutputStream stdOut = new DataOutputStream(out); // Object to write in Client Output
                stdOut.writeUTF("Hi Client: " + numCli + "!"); // Greetings to the Client in UTF8 encode
            }
            System.out.println("Too many Clients today. Need a break!"); // Server isnt allowed to serve more!
            skServidor.close(); // Close the Server Socket
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    public static void main(String[] arg) {
        new Servidor();
    }
}