package serveur;

import app.IDocument;
import codage.Codage;
import app.Data;
import doc.Abonne;
import doc.Document;
import doc.EmpruntException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;

public class ServiceEmpruntRetour extends Service {
    private Abonne abonne;

    private final static int DUREE_MAX_RENDU_SEMAINE = 2; // temps pour rendre un document

    public ServiceEmpruntRetour(Socket socket) {
        super(socket);
        abonne = null;
    }

    @Override
    public void run() {
        try {
            try {
                System.out.println("Traitement du client : " + this.getClient().getInetAddress() + ","
                        + this.getClient().getPort());
                BufferedReader in = new BufferedReader(new InputStreamReader(this.getClient().getInputStream()));
                PrintWriter out = new PrintWriter(this.getClient().getOutputStream(), true);

                boolean continuer = true;
                String line = "";

                while (continuer) {
                    String demandeService = "A quel service souhaitez-vous acceder? (Emprunt/Retour)";
                    out.println(Codage.coder(demandeService));
                    line = Codage.decoder(in.readLine());
                    ServiceUtils.checkConnectionStatus(line, getClient());

                    while (!line.equalsIgnoreCase("Emprunt") && !line.equalsIgnoreCase("Retour")) {
                        out.println(Codage.coder("Ce service n'est pas disponible.\n" + demandeService));
                        line = Codage.decoder(in.readLine());
                        ServiceUtils.checkConnectionStatus(line, getClient());
                    }

                    if (line.equalsIgnoreCase("Emprunt"))
                        emprunt(in, out);
                    else if (line.equalsIgnoreCase("Retour"))
                        retour(in, out);

                    out.println(Codage.coder("Voulez-vous continuer? (Oui/Non)"));
                    line = Codage.decoder(in.readLine());
                    ServiceUtils.checkConnectionStatus(line, getClient());

                    while (!line.equalsIgnoreCase("oui") && !line.equals("non")) {
                        line = "Veuillez entrer une reponse valide.";
                        out.println(Codage.coder(line));
                        line = Codage.decoder(in.readLine());
                        ServiceUtils.checkConnectionStatus(line, getClient());
                    }

                    continuer = line.equalsIgnoreCase("oui");
                }

                ServiceUtils.endConnection(this.getClient());
            } catch (IOException | InterruptedException e) {
                throw new FinConnexionException("Connexion terminee. Merci d'avoir utilise nos services.");
            }
        } catch (FinConnexionException e) {
            System.err.println(e.getMessage());
        }
    }

    private void retour(BufferedReader in, PrintWriter out) throws IOException, FinConnexionException {
        int numDoc;
        out.println(Codage.coder("Entrez le numero du document que vous voulez retouner : "));
        String line = Codage.decoder(in.readLine());
        ServiceUtils.checkConnectionStatus(line, getClient());
        while ((numDoc = ServiceUtils.numIsCorrect(line)) == -1) {
            out.println(Codage.coder("Veuillez entrer un numero valide."));
            line = Codage.decoder(in.readLine());
            ServiceUtils.checkConnectionStatus(line, getClient());
        }

        IDocument document = Data.getDocument(numDoc);
        String message;
        if(document == null)
            message = "Le document n'existe pas.\n";
        else if(document.emprunteur() == null)
            message = "Le document n'est pas emprunte.\n";
        else {
            abonne = document.emprunteur();
            LocalDateTime date = document.dateEmprunt();
            document.ramdomDocumentAbime();
            if (document.getDocumentAbime()) {
                abonne.bannir();
                message = "Le document a bien ete retourne. Cependant je constate, " + abonne.getNom()
                        + ", que vous avez abime le document.\n Geronimo est intransigeant sur l'etat de retour des" +
                        " documents empruntes !\n Vous etes donc banni jusqu'au : " + abonne.getDateBannissement()
                        + " et la sentence est irrevoquable !\n";
            }
            else {
                document.retour();
                // ligne de test en seconde
                if (LocalDateTime.now().isAfter(date.plusSeconds(DUREE_MAX_RENDU_SEMAINE))) {
              //  if(LocalDateTime.now().isAfter(date.plusWeeks(DUREE_MAX_RENDU_SEMAINE))){
                    abonne.bannir();
                    message = "Document retourne avec succes, mais en retard.\n" + abonne.getNom()
                            + ", le grand chef Geronimo a decide de vous bannir pour 1 mois, soit jusqu'au :"
                            + abonne.getDateBannissement() + "\n";
                } else {
                    message = "Document retourne avec succes.\n";
                }
            }
        }
        out.print(Codage.coder(message));
    }

    private void emprunt(BufferedReader in, PrintWriter out) throws IOException, InterruptedException, FinConnexionException {
        String line;
        int numeroAdherent;

        if (abonne == null) {
            out.println(Codage.coder("Saisissez votre numero d'adherent > "));
            line = Codage.decoder(in.readLine());
            ServiceUtils.checkConnectionStatus(line, getClient());

            while ((numeroAdherent = ServiceUtils.numIsCorrect(line)) == -1 || !Data.abonneExiste(numeroAdherent)) {
                line = "Veuillez entrer un numero valide.";
                out.println(Codage.coder(line));
                line = Codage.decoder(in.readLine());
                ServiceUtils.checkConnectionStatus(line, getClient());
            }

            numeroAdherent = Integer.parseInt(line);
            abonne = Data.getAbonne(numeroAdherent);
        }

        if (abonne.estBanni()){
            out.print(Codage.coder("Bonjour "+ abonne.getNom() + "! \n N'oubliez pas que le grand chef Geronimo" +
                    " vous a banni jusqu'au : " + abonne.getDateBannissement() + "\n"));
            return;
        }

        String listeReservations = Data.afficherDocumentsEmpruntes(abonne);
        out.println(Codage.coder(listeReservations + abonne.getNom() + ", quel est le numero"
                + " du document que vous voulez emprunter ? > "));

        int numDocument;
        Document document;
        line = Codage.decoder(in.readLine());
        ServiceUtils.checkConnectionStatus(line, getClient());

        while ((numDocument = ServiceUtils.numIsCorrect(line)) == -1
                || !Data.documentExiste(document = (Document) Data.getDocument(numDocument))) {
            out.println(Codage.coder("Veuillez entrer un numero valide."));
            line = Codage.decoder(in.readLine());
            ServiceUtils.checkConnectionStatus(line, getClient());
        }

        numDocument = Integer.parseInt(line);
        document = (Document) Data.getDocument(numDocument);

        out.println(Codage.coder("Etes-vous sur de vouloir emprunter le document suivant : \n" + document.getTitre()
                + " ? (oui/non) > "
                ));
        line = Codage.decoder(in.readLine());
        ServiceUtils.checkConnectionStatus(line, getClient());

        while (!line.equalsIgnoreCase("oui") && !line.equalsIgnoreCase("non")) {
            line = "Veuillez entrer une reponse valide.";
            out.println(Codage.coder(line));
            line = Codage.decoder(in.readLine());
            ServiceUtils.checkConnectionStatus(line, getClient());
        }

        if (line.equalsIgnoreCase("oui")) {
            try {
                Data.emprunt(document, abonne);
                out.print(Codage.coder("Emprunt effectue avec succes. Ce jour : " + Document.dateEmpruntFormat()
                        + "\n Vous avez jusqu'au : " + Document.dateFinEmpruntFormat() + " pour rendre le document.\n"));
            } catch (EmpruntException e) {
                out.print(Codage.coder(e.getMessage()));
            }
        } else {
            try {
                Data.retirerReservation(document);
                out.print(Codage.coder("Emprunt annule."));
            } catch (EmpruntException e) {
                throw new RuntimeException(e);
            }
        }

        out.print(Codage.coder("\n"));
    }
}
