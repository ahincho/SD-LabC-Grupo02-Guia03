package com.unsa.solved;

import java.io.*;
import java.net.*;

public class Cliente {
    static final String HOST = "localhost"; // Hostname
    static final int PORT = 5000; // Port which runs the program
    public Cliente() {
        try {
            Socket skCliente = new Socket(HOST , PORT); // Create a new Client Socket
            InputStream msg = skCliente.getInputStream(); // Client is asking for an Input
            DataInputStream stdIn = new DataInputStream(msg); // Get data from the Server
            System.out.println(stdIn.readUTF()); // Print the message or data in UTF-8
            skCliente.close(); // Close the Client Socket created before
        } catch (Exception e) {
            System.out.println(e.getMessage()); // Print an Exception
        }
    }
    public static void main(String[] arg) {
        new Cliente();
    }
}