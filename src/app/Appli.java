package app;

import serveur.Serveur;
import serveur.Service;
import serveur.ServiceEmpruntRetour;
import serveur.ServiceReservation;

import java.io.IOException;


public class Appli {

    public static void main(String[] args) {

        new Thread(new Data()).start();

        Class<? extends Service> service = null;
        int port = -1;

        if (args[0].equals("3000")) {
            service = ServiceReservation.class;
            port = 3000;
        } else if (args[0].equals("4000")) {
            service = ServiceEmpruntRetour.class;
            port = 4000;
        }

        if (service != null) {
            try {

                new Thread(new Serveur(service, port)).start();
                System.out.println("Serveur lance avec succès sur le port " + port);
            } catch (IOException e) {
                System.err.println("Probleme lors de la création du serveur : " + e);
            }
        } else {
            System.err.println("Probleme lors de la création du serveur");
        }
    }


}
