package com.unsa.solved;

import java.io.*;
import java.net.*;

public class Servidor {
    static final int PUERTO = 5000;
    static final int MAX_CLIENTES = 3;
    public Servidor() {
        try {
            ServerSocket skServidor = new ServerSocket(PUERTO);
            System.out.println("Escucho en el puerto: " + PUERTO);
            for (int numCli = 0 ; numCli < MAX_CLIENTES ; numCli++) {
                Socket skCliente = skServidor.accept(); // Crea objeto
                System.out.println("Sirvo al Cliente " + numCli);
                OutputStream aux = skCliente.getOutputStream();
                DataOutputStream flujo = new DataOutputStream(aux);
                flujo.writeUTF("Hola Cliente: " + numCli + "!");
            }
            System.out.println("Demasiados clientes por hoy");
            skServidor.close();
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
    public static void main(String[] arg) {
        new Servidor();
    }
}