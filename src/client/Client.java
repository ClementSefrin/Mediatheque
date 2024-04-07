package client;

import codage.Codage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private final static String HOST = "localhost";

    public static void main(String[] args) {
        Socket socket = null;
        try {
            socket = new Socket(HOST, Integer.parseInt(args[0]));
            BufferedReader sin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter sout = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader clavier = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Connecte au serveur " + socket.getInetAddress() + ":" + socket.getPort());

            String inLine, outLine;

            while (socket.isConnected()) {
                inLine = sin.readLine();
                if (inLine == null || inLine.equalsIgnoreCase("quitter"))
                    break;
                System.out.println(Codage.decoder(inLine));

                outLine = clavier.readLine();
                sout.println(Codage.coder(outLine));
                if (outLine.equalsIgnoreCase("quitter")) {
                    break;
                }
            }

            System.err.println("Fin de la connexion. Merci d'avoir utilise nos services.");
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
