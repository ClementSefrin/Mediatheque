package app;

import doc.*;
import doc.types.*;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;

public class Data implements Runnable {
    private static final String DB_URL = "jdbc:mariadb://194.164.50.105:3306/MediaTech";
    private static final String USER = "Admin";
    private static final String PASS = "Admin";

    private static final LinkedList<IDocument> documents = new LinkedList<>();
    private static final LinkedList<Abonne> abonnes = new LinkedList<>();
    private static final HashMap<IDocument, Abonne> reservations = new HashMap<>();
    // private static HashMap<IDocument, Abonne> emprunts = new HashMap<>();

    @Override
    public void run() {
        loadData();
    }

    public static void loadData() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);

            String query;
            PreparedStatement preparedStatement;
            ResultSet resultSet;

            //Récupération des abonnées
            query = "SELECT * FROM Abonne";
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                abonnes.add(new Abonne(resultSet.getInt("Numero"), resultSet.getString("Nom"),
                        resultSet.getDate("DateNaissance")));
            }

            //Récupération des documents
            query = "SELECT * FROM Document";
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int numero = resultSet.getInt("Numero");
                String titre = resultSet.getString("Titre");

                query = "SELECT * FROM Livre WHERE Numero=?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, numero);
                ResultSet resultSet2 = preparedStatement.executeQuery();
                if (resultSet2.next()) {
                    documents.add(new Livre(numero, titre, resultSet2.getInt("NbPages")));
                } else {
                    query = "SELECT * FROM DVD WHERE Numero=?";
                    preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setInt(1, numero);
                    resultSet2 = preparedStatement.executeQuery();

                    if (resultSet2.next())
                        documents.add(new DVD(numero, titre, resultSet2.getBoolean("Adulte")));
                }
            }

            //Récupération des réservations
            //Récupération des emprunts
            for (int i = 1; i <= 15; i++) {
                reservations.put(getDocument(i), getAbonne(1));
            }

        } catch (ClassNotFoundException e1) {
            System.err.print("ClassNotFoundException: ");
            System.err.println(e1.getMessage());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static LinkedList<IDocument> getDocuments() {
        return documents;
    }

    public static LinkedList<Abonne> getAbonnes() {
        return abonnes;
    }

    public static HashMap<IDocument, Abonne> getReservations() {
        return reservations;
    }

    public static Abonne getAbonne(int numero) {
        for (Abonne a : abonnes) {
            if (a.getNumero() == numero) {
                return a;
            }
        }
        return null;
    }

    public static IDocument getDocument(int numero) {

        for (IDocument d : documents) {
            if (d.getNumero() == numero) {
                return d;
            }
        }
        return null;
    }


    public static IDocument getDocumentAbonne(Abonne a) {
        for (IDocument d : documents) {
            if (d.emprunteur() != null && d.emprunteur().equals(a)) {
                return d;
            }
        }
        return null;
    }
;
    public static void emprunt(IDocument d, Abonne a) {
        d.empruntPar(a);
    }

    public static void retour(IDocument d) {
        d.retour();
    }

    public static boolean estEmprunte(IDocument d) {
        return d.emprunteur() != null;
    }

    public static boolean estReserve(IDocument d) {
        return d.emprunteur() != null;
    }

    public static String nomAbonne(int numero) {
        for (Abonne a : abonnes) {
            if (a.getNumero() == numero) {
                return a.getNom();
            }
        }
        return null;
    }

    public static void reserver(IDocument d, Abonne a) {
        synchronized (reservations){
            reservations.put(d, a);
        }
    }

    public static boolean adherentAReserve(IDocument d, Abonne a) {
        return d.reserveur()!= null && d.reserveur().equals(a);
    }

    public static void retirerReservation(IDocument d) {
        d.reservationPour(null);
    }

    public static boolean DVDPourMajeur(IDocument d) {
        if (d instanceof DVD)
            return ((DVD) d).estAdulte();
        return false;
    }

    public static boolean abonnePeutPasEmprunterDVD(IDocument d, Abonne a) {
        return DVDPourMajeur(d) && !a.estMajeur();
    }

    public static boolean abonneExiste(int numero) {
        for (Abonne a : abonnes) {
            if (a.getNumero() == numero) {
                return true;
            }
        }
        return false;
    }
}