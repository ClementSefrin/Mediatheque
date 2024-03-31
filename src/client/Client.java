package client;

import codage.Codage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private final static int PORT = 4000;
    private final static String HOST = "localhost";

    public static void main(String[] args) {
        Socket socket = null;
        try {
            socket = new Socket(HOST, PORT);
            BufferedReader sin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter sout = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader clavier = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Connecte au serveur " + socket.getInetAddress() + ":" + socket.getPort());
            String line = "";
            while (!line.equals("Connexion terminee. Merci d'avoir utilise nos services.")) {
                line = sin.readLine();
                System.out.println(Codage.decoder(line));

                line = clavier.readLine();
                sout.println(Codage.coder(line));
            }

            socket.close();
        } catch (IOException e) {
            System.err.println(e);
        }
        // Refermer dans tous les cas la socket
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
    }
}
