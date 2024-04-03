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

    private static LinkedList<IDocument> documents = new LinkedList<>();
    private static LinkedList<Abonne> abonnes = new LinkedList<>();
    private static HashMap<IDocument, Abonne> reservations = new HashMap<>();
    private static HashMap<IDocument, Abonne> emprunts = new HashMap<>();

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

    public static HashMap<IDocument, Abonne> getEmprunts() {
        return emprunts;
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

    public static String NomAbonne(int numero) {
        for (Abonne a : abonnes) {
            if (a.getNumero() == numero) {
                return a.getNom();
            }
        }
        return null;
    }

    public static boolean estEmprunter(IDocument d) {
        return emprunts.containsKey(d);
    }

    public static void retour(IDocument d) {
        synchronized (emprunts) {
            emprunts.remove(d);
        }
    }

    public static void reserver(IDocument d, Abonne a) {
        synchronized (reservations) {
            reservations.put(d, a);
        }
    }

    public static boolean estReserver(IDocument d) {
        return reservations.containsKey(d);
    }

    public static void emprunter(IDocument d, Abonne a) {
        synchronized (emprunts) {
            emprunts.put(d, a);
        }
    }

    public static boolean adherentAReserver(IDocument d, Abonne a) {
        return reservations.get(d).equals(a);
    }

    public static void retirerReservation(IDocument d) {
        synchronized (reservations) {
            reservations.remove(d);
        }
    }

    public static boolean AbonnePeutEmprunter(IDocument d, Abonne a) {
        return !(d instanceof DVD && ((DVD) d).estAdulte() && !a.estMajeur());
    }

    public static boolean AbboneExiste(int numero) {
        for (Abonne a : abonnes) {
            if (a.getNumero() == numero) {
                return true;
            }
        }
        return false;
    }
}
