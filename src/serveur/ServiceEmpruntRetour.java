package serveur;

import codage.Codage;
import app.Data;
import doc.Abonne;
import doc.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;

public class ServiceEmpruntRetour extends Service {

    /* TODO :
        - vérifier que l'abonné/le document existe à la récupération du numéro
        - vérifier que le client ne coupe pas la connexion à chaque échange
    */
    private Abonne abonne;

    public ServiceEmpruntRetour(Socket socket) {
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
            BufferedReader in = new BufferedReader(new InputStreamReader(this.getClient().getInputStream()));
            PrintWriter out = new PrintWriter(this.getClient().getOutputStream(), true);
            String line = "Saisissez votre numero d'adherent : ";
            int numeroAdherent;
            out.println(Codage.coder(line));
            line = Codage.decoder(in.readLine());
            while ((numeroAdherent = numIsCorrect(line)) == -1 && Data.getAbonne(numeroAdherent) != null) {
                line = "Veuillez entrer un numero valide.";
                out.println(Codage.coder(line));
                line = Codage.decoder(in.readLine());
            }

            abonne = Data.getAbonne(numeroAdherent);

            boolean quit = false;
            while (!quit) {
                String demandeService = "A quel service souhaitez-vous acceder? (Emprunt/Retour)";
                out.println(Codage.coder(demandeService));

                line = Codage.decoder(in.readLine());

                while (!line.equalsIgnoreCase("Emprunt") && !line.equalsIgnoreCase("Retour")) {
                    out.println(Codage.coder("Ce service n'est pas disponible. \n" + demandeService));
                    line = Codage.decoder(in.readLine());
                }

                if (line.equalsIgnoreCase("Emprunt")) {
                    emprunt(numeroAdherent, in, out);
                } else if (line.equalsIgnoreCase("Retour")) {
                    retour(in, out);
                }
                line = "Vouler-vous continuer? (Oui/Non)";
                out.println(Codage.coder(line));
                quit = Codage.decoder(in.readLine()).equalsIgnoreCase("oui") ? false : true;
            }

            line = "Connexion terminee. Merci d'avoir utilise nos services.";
            out.println(Codage.coder(line));
            System.err.println("Un client a termine la connexion.");
            getClient().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void retour(BufferedReader in, PrintWriter out) throws IOException {
        int numDoc;
        String line = "Entrez le numero du document que vous voulez retouner : ";
        out.println(Codage.coder(line));
        line = Codage.decoder(in.readLine());
        while ((numDoc = numIsCorrect(line)) == -1) {
            line = "Veuillez entrer un numero valide.";
            out.println(Codage.coder(line));
            line = Codage.decoder(in.readLine());
        }

        Document document = Data.getDocument(numDoc);
        if (Data.estEmprunter(document)) {
            Data.retour(document);
            out.print(Codage.coder("Le document a bien ete retourne.\n"));
        } else {
            out.print(Codage.coder("Le document n'est pas emprunte.\n"));
        }
    }


    private void emprunt(int numeroAdherent, BufferedReader in, PrintWriter out) throws IOException {
        Abonne abonne = Data.getAbonne(numeroAdherent);

        LinkedList<Document> documentsReserves = new LinkedList<>();
        StringBuilder sb = new StringBuilder();
        boolean empty = true;
        for (Document doc : Data.getReservations().keySet()) {
            if (Data.adherentAReserver(doc, abonne)) {
                if (empty) {
                    empty = false;
                    sb.append("Liste des documents reserves : \n");
                }
                documentsReserves.add(doc);
                sb.append("\t" + doc.toString() + "\n");
            }
        }

        if (empty)
            sb.append("Vous n'avez aucun document reserve.\n");
        sb.append("Entrez le numero du document que vous voulez emprunter : ");
        out.println(Codage.coder(sb.toString()));

        int numDocument;
        String line = Codage.decoder(in.readLine());
        while ((numDocument = numIsCorrect(line)) == -1) {
            line = "Veuillez entrer un numero valide.";
            out.println(Codage.coder(line));
            line = Codage.decoder(in.readLine());
        }

        Document document = Data.getDocument(numDocument);
        if (Data.estReserver(document) && !Data.adherentAReserver(document, abonne)) {
            out.print(Codage.coder("Le document est reserve par une autre personne.\n"));
        } else if (Data.estEmprunter(document)) {
            out.print(Codage.coder("Le document est deja emprunte.\n"));
        } else {
            Data.emprunter(document, abonne);
            Data.retirerReservation(document);
            out.print(Codage.coder("Emprunt effectue avec succes.\n"));
        }
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
