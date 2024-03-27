package serveur;

import codage.Codage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ServiceUtils {

    public static boolean checkConnectionStatus(String str) {
        return str.equalsIgnoreCase("quit");
    }

    public static void endConnection(Socket client, PrintWriter out) throws IOException {
        String line = "Connexion terminee. Merci d'avoir utilise nos services.";
        out.println(Codage.coder(line));
        System.err.println("Un client a termine la connexion.");
        client.close();
    }

    public static int numIsCorrect(String str) {
        try {
            int n = Integer.parseInt(str);
            if (n < 1)
                throw new NumberFormatException();
            return n;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
