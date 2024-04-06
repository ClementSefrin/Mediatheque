package serveur;

import app.IDocument;
import doc.Abonne;

import java.io.IOException;
import java.net.Socket;

public class ServiceUtils {

    public static void checkConnectionStatus(String str,Socket client) throws FinConnexionException {
        if (str.equalsIgnoreCase("quit")) {
            endConnection(client);
        }
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
            if (n < 1)
                throw new NumberFormatException();
            return n;
        } catch (NumberFormatException e) {
            return -1;
        }
    }



    public static boolean abonneARenduEnretard(IDocument doc, Abonne a) {
        if(doc.dateEmprunt() == null){
            return false;
        }
        else{
           // LocalDate dateLimite = doc.dateEmprunt().plusDays(15);
           // if(LocalDate.now().isAfter(dateLimite)){
                a.bannir();
                a.putDateBannissement();
                return true;
            }
    }
}
