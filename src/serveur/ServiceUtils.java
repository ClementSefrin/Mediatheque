package serveur;

import app.Data;
import app.IDocument;
import codage.Codage;
import doc.Abonne;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ServiceUtils {

    public static void checkConnectionStatus(String str,Socket client) throws FinConnexionException {
        if (str.equalsIgnoreCase("quit")) endConnection(client);
    }

    public static void endConnection(Socket client) throws FinConnexionException {
        try {
            client.close();
        } catch (IOException e) {
            throw new FinConnexionException("Connexion terminee.");
        }
        throw new FinConnexionException("Un client a termine la connexion.");
    }

    public static int numIsCorrect(String str) {
        try {
            int n = Integer.parseInt(str);
            if (n < 1) throw new NumberFormatException();
            return n;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}

