package serveur;

import app.IDocument;
import codage.Codage;
import app.Data;
import doc.Abonne;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;

public class ServiceEmpruntRetour extends Service {
    private Abonne abonne;

    public ServiceEmpruntRetour(Socket socket) {
        super(socket);
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(this.getClient().getInputStream()));
            PrintWriter out = new PrintWriter(this.getClient().getOutputStream(), true);
            String line = "";
            int numeroAdherent = -1;
            boolean quit = false;
            while (!quit) {
                String demandeService = "A quel service souhaitez-vous acceder? (Emprunt/Retour)";
                out.println(Codage.coder(demandeService));

                line = Codage.decoder(in.readLine());

                while (!line.equalsIgnoreCase("Emprunt") && !line.equalsIgnoreCase("Retour")) {
                    out.println(Codage.coder("Ce service n'est pas disponible. \n" + demandeService));
                    line = Codage.decoder(in.readLine());
                    ServiceUtils.checkConnectionStatus(line, getClient(), out);
                }

                if (line.equalsIgnoreCase("Emprunt")) {
                    emprunt(numeroAdherent, in, out);
                } else if (line.equalsIgnoreCase("Retour")) {
                    retour(in, out);
                }
                line = "Vouler-vous continuer? (Oui/Non)";
                out.println(Codage.coder(line));
                line = Codage.decoder(in.readLine());
                ServiceUtils.checkConnectionStatus(line, getClient(), out);
                while (!line.equalsIgnoreCase("oui") && !line.equals("non")) {
                    line = "Veuillez entrer une réponse valide.";
                    out.println(Codage.coder(line));
                    line = Codage.decoder(in.readLine());
                    ServiceUtils.checkConnectionStatus(line, getClient(), out);
                }
                quit = Codage.decoder(line).equalsIgnoreCase("non");
            }

            ServiceUtils.endConnection(getClient(), out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void retour(BufferedReader in, PrintWriter out) throws IOException {
        int numDoc;
        String line = "Entrez le numero du document que vous voulez retouner : ";
        out.println(Codage.coder(line));
        line = Codage.decoder(in.readLine());
        ServiceUtils.checkConnectionStatus(line, getClient(), out);
        while ((numDoc = ServiceUtils.numIsCorrect(line)) == -1) {
            line = "Veuillez entrer un numero valide.";
            out.println(Codage.coder(line));
            line = Codage.decoder(in.readLine());
            ServiceUtils.checkConnectionStatus(line, getClient(), out);
        }

        IDocument document = Data.getDocument(numDoc);
        if (document == null) {
            out.print(Codage.coder("Le document n'existe pas.\n"));
        } else if (Data.estEmprunte(document)) {
            Data.retour(document);
            out.print(Codage.coder("Le document a bien ete retourne.\n"));
        } else {
            out.print(Codage.coder("Le document n'est pas emprunte.\n"));
        }
    }


    private void emprunt(int numeroAdherent, BufferedReader in, PrintWriter out) throws IOException {
        String line = "Saisissez votre numero d'adherent : ";
        out.println(Codage.coder(line));
        line = Codage.decoder(in.readLine());
        ServiceUtils.checkConnectionStatus(line, getClient(), out);

        while ((numeroAdherent = ServiceUtils.numIsCorrect(line)) == -1 || !Data.abonneExiste(numeroAdherent)){
            line = "Veuillez entrer un numero valide.";
            out.println(Codage.coder(line));
            line = Codage.decoder(in.readLine());
            ServiceUtils.checkConnectionStatus(line, getClient(), out);
        }

        Abonne abonne = Data.getAbonne(numeroAdherent);

        LinkedList<IDocument> documentsReserves = new LinkedList<>();
        StringBuilder sb = new StringBuilder();
        boolean empty = true;
        for (IDocument doc : Data.getReservations().keySet()) {
            if (Data.adherentAReserve(doc, abonne)) {
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
        line = Codage.decoder(in.readLine());
        ServiceUtils.checkConnectionStatus(line, getClient(), out);
        while ((numDocument = ServiceUtils.numIsCorrect(line)) == -1) {
            line = "Veuillez entrer un numero valide.";
            out.println(Codage.coder(line));
            line = Codage.decoder(in.readLine());
            ServiceUtils.checkConnectionStatus(line, getClient(), out);
        }

        IDocument document = Data.getDocument(numDocument);
        System.out.println(document.getNumero());
        if (document == null)
            out.print(Codage.coder("Le document n'existe pas.\n"));
        else if (Data.estReserve(document) && !Data.adherentAReserve(document, abonne))
            out.print(Codage.coder("Le document est reserve par une autre personne.\n"));
        else if (Data.estEmprunte(document))
            out.print(Codage.coder("Le document est deja emprunte.\n"));
        else if (Data.abonnePeutPasEmprunterDVD(document, abonne))
            out.print(Codage.coder("Le DVD est pour personne majeur\n"));
        else {
            synchronized (document) {
                out.println(Codage.coder("Etes-vous sur de vouloir emprunter le document suivant? (Oui/Non)\n" + document.toString()));
                line = Codage.decoder(in.readLine());
                ServiceUtils.checkConnectionStatus(line, getClient(), out);

                while (!line.equalsIgnoreCase("oui") && !line.equals("non")) {
                    line = "Veuillez entrer une réponse valide.";
                    out.println(Codage.coder(line));
                    line = Codage.decoder(in.readLine());
                    ServiceUtils.checkConnectionStatus(line, getClient(), out);
                }

                if (line.equalsIgnoreCase("oui")) {
                    Data.emprunt(document, abonne);
                    out.print(Codage.coder("Emprunt effectue avec succes.\n"));
                } else {
                    out.print(Codage.coder("Emprunt annule.\n"));
                }
            }

        }
    }
}
