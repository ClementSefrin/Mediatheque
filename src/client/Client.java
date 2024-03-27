package client;

import codage.Codage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private final static int PORT = 3000;
    private final static String HOST = "localhost";

    public static void main(String[] args) {
        Socket socket = null;
        try {
            socket = new Socket(HOST, PORT);
            BufferedReader sin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter sout = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader clavier = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Connecte au serveur " + socket.getInetAddress() + ":" + socket.getPort());

            String inLine, outLine;

            while (true) {
                inLine = sin.readLine();
                if (inLine.equalsIgnoreCase("quit")) {
                    break;
                }
                System.out.println(Codage.decoder(inLine));

                outLine = clavier.readLine();
                if (outLine.equalsIgnoreCase("quit")) {
                    break;
                }
                sout.println(Codage.coder(outLine));
            }
            System.err.println("Fin de la connexion. Merci d'avoir utilisé nos services.");
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
