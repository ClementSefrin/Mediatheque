package app;

import serveur.Serveur;
import serveur.ServiceEmpruntRetour;
import serveur.ServiceReservation;

import java.io.IOException;


public class Appli {
    private final static int PORT_RESERVATION = 3000;
    private final static int PORT_EMPRUNT_RETOUR = 4000;

    public static void main(String[] args) {
        new Thread(new Data()).start();

        try {
            new Thread(new Serveur(ServiceReservation.class, PORT_RESERVATION)).start();
            System.out.println("Serveur lance avec succes sur le port " + PORT_RESERVATION);
            new Thread(new Serveur(ServiceEmpruntRetour.class, PORT_EMPRUNT_RETOUR)).start();
            System.out.println("Serveur lance avec succes sur le port " + PORT_EMPRUNT_RETOUR);
        } catch (IOException e) {
            System.err.println("Probleme lors de la creation du serveur : " + e);
        }
    }
}
