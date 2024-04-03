package serveur;

import codage.Codage;
import app.Data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.*;

public class ServiceReservation extends Service {
    private static final String DB_URL = "jdbc:mariadb://194.164.50.105:3306/MediaTech";
    private static final String USER = "Admin";
    private static final String PASS = "Admin";
    private Connection connection;

    public ServiceReservation(Socket socket) {
        super(socket);
    }

    @Override
    public void run() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e1) {
            System.err.print("ClassNotFoundException: ");
            System.err.println(e1.getMessage());
        }
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Traitement du client : " + this.getClient().getInetAddress() + "," + this.getClient().getPort());

            BufferedReader in = new BufferedReader(new InputStreamReader(this.getClient().getInputStream()));
            PrintWriter out = new PrintWriter(this.getClient().getOutputStream(), true);

            out.println(Codage.coder("Saisissez votre numero d'adherent > "));
            String numeroAdherent = in.readLine();
            String nomAdherent = "";

            try {
                if (numeroAdherent == null) {
                    System.err.println("Numero d'adherent inexistant ?");
                    try {
                        this.getClient().close();
                    } catch (IOException ignored) {
                    }
                    return;
                }

                String query = "SELECT Nom FROM Abonne WHERE Numero=?";
                PreparedStatement preparedStatement;
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, Integer.parseInt(numeroAdherent));
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    nomAdherent = resultSet.getString("Nom");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            boolean continuer = true;

            while (continuer) {
                out.println(Codage.coder("Que voulez-vous reserver, " + nomAdherent + " ? > "));
                int numDocs = Integer.parseInt(in.readLine());

                System.out.println(Data.getDocuments().get(numDocs - 1));
                //TODO: Le document existe dans notre hashmap ?
                //TODO : Le document est disponible ?

                out.println(Codage.coder("Voulez-vous continuer ? (y/n)"));
                continuer = in.readLine().equalsIgnoreCase("y");
            }

            out.println(Codage.coder("Connexion terminee. Merci d'avoir utilise nos services."));
        } catch (IOException e) {
            System.err.println("Client deconnecte ?");
            try {
                this.getClient().close();
            } catch (IOException ignored) {}
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            this.getClient().close();
        } catch (IOException ignored) {}
    }
}
