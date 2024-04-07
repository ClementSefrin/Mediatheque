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
    private final static int DUREE_MAX_RENDU_SEMAINE = 2;

    public ServiceEmpruntRetour(Socket socket) {
        super(socket);
        abonne = null;
    }

    @Override
    public void run() {
        try {
            try {
                System.out.println("Traitement du client : " + this.getClient().getInetAddress() + "," + this.getClient().getPort());

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
                        out.println(Codage.coder("Ce service n'est pas disponible. \n" + demandeService));
                        line = Codage.decoder(in.readLine());
                        ServiceUtils.checkConnectionStatus(line, getClient());
                    }

                    if (line.equalsIgnoreCase("Emprunt"))
                        emprunt(in, out);
                    else if (line.equalsIgnoreCase("Retour"))
                        retour(in, out);
                    line = "Vouler-vous continuer? (Oui/Non)";
                    out.println(Codage.coder(line));

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
        String line = "Entrez le numero du document que vous voulez retouner : ";
        out.println(Codage.coder(line));
        line = Codage.decoder(in.readLine());
        ServiceUtils.checkConnectionStatus(line, getClient());
        while ((numDoc = ServiceUtils.numIsCorrect(line)) == -1) {
            line = "Veuillez entrer un numero valide.";
            out.println(Codage.coder(line));
            line = Codage.decoder(in.readLine());
            ServiceUtils.checkConnectionStatus(line, getClient());
        }

        IDocument document = Data.getDocument(numDoc);
        if(document == null){
            out.print(Codage.coder("Le document n'existe pas.\n"));
        }
        else if(document.emprunteur() == null){
            out.print(Codage.coder("Le document n'est pas emprunte.\n"));
        }
        else {
            abonne = document.emprunteur();
            LocalDateTime date = document.dateEmprunt();
            document.RamdomDocumentAbime();
            if(document.getDocumentAbime()){
                abonne.bannir();
                out.print(Codage.coder("Le document a été retourné, cependant je constate " + abonne.getNom()+ ", que vous avez abîmer le document.\n " +
                        "Geronimo est intransigeant sur l'état de retour des documents empruntés !\n" +
                        " Vous êtes banni jusqu'au : " + abonne.getDateBanissement() + " et la sentance est irrévoquable !\n"));
            }
            else {
                document.retour();
                // ligne de test en seconde
                //if (LocalDateTime.now().isAfter(date.plusSeconds(DUREE_MAX_RENDU_SEMAINE))) {
                     if(LocalDateTime.now().isAfter(date.plusWeeks(DUREE_MAX_RENDU_SEMAINE))){
                    abonne.bannir();
                    out.print(Codage.coder("Document retourne avec succes, cependant pour des raisons de retard, "
                            + abonne.getNom() +
                            " le grand chef Géronimo a décidé de vous a banni pour 1 mois.\n" + "Jusqu'au :" + abonne.getDateBanissement() + "\n"));
                } else {
                    out.print(Codage.coder("Document retourne avec succes.\n"));
                }
            }
        }
    }

    private void emprunt(BufferedReader in, PrintWriter out) throws IOException, InterruptedException, FinConnexionException {
        String line;
        int numeroAdherent;
        if (abonne == null) {
            out.println(Codage.coder("Veuillez entrer votre numero d'adherent : "));
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

        if(abonne.estBanni() == true){
            out.print(Codage.coder("Bonjour "+ abonne.getNom() +
                    ", le grand chef Géronimo vous a banni jusqu'au : " + abonne.getDateBanissement() + "\n"));
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(Data.afficherDocumentsReserves(abonne));
        //sb.append(Data.afficherDocumentsEmpruntes(abonne));
        sb.append(abonne.getNom() + " quel numero du document que vous voulez emprunter : ");
        out.println(Codage.coder(sb.toString()));

        int numDocument;
        line = Codage.decoder(in.readLine());
        ServiceUtils.checkConnectionStatus(line, getClient());
        while ((numDocument = ServiceUtils.numIsCorrect(line)) == -1) {
            line = "Veuillez entrer un numero valide.";
            out.println(Codage.coder(line));
            line = Codage.decoder(in.readLine());
            ServiceUtils.checkConnectionStatus(line, getClient());
        }

        Document document = (Document) Data.getDocument(numDocument);
        while (!Data.documentExiste(document)) {
            line = "Veuillez entrer un numero valide.";
            out.println(Codage.coder(line));
            line = Codage.decoder(in.readLine());
            ServiceUtils.checkConnectionStatus(line, getClient());
            numDocument = ServiceUtils.numIsCorrect(line);
            document = (Document) Data.getDocument(numDocument);
        }

        out.println(Codage.coder("Etes-vous sur de vouloir emprunter le document suivant? (Oui/Non)\n" + document.getTitre()));
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
                out.print(Codage.coder("Emprunt effectue avec succes. Ce jour : " + Document.dateEmpruntFormat()));
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
