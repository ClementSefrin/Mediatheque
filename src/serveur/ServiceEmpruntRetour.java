package serveur;

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

import static java.lang.Thread.sleep;

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
            BufferedReader in = new BufferedReader(new InputStreamReader(this.getClient().getInputStream()));
            PrintWriter out = new PrintWriter(this.getClient().getOutputStream(), true);
            boolean quit = false;
            String line = "";
            boolean premierPassage = true;
            int numeroAdherent = -1;
            while (!quit) {
                String demandeService = "A quel service souhaitez-vous acceder? (Emprunt/Retour)";
                out.println(Codage.coder(demandeService));
                line = Codage.decoder(in.readLine());

                while (!line.equalsIgnoreCase("Emprunt") && !line.equalsIgnoreCase("Retour")) {
                    out.println(Codage.coder("Ce service n'est pas disponible. \n" + demandeService));
                    line = Codage.decoder(in.readLine());
                }

                if (line.equalsIgnoreCase("Emprunt")) {
                    System.out.println("On appelle emprunt");
                    numeroAdherent = emprunt(numeroAdherent, in, out, premierPassage);
                } else if (line.equalsIgnoreCase("Retour")) {
                    retour(in, out);
                }
                line = "Vouler-vous continuer? (Oui/Non)";
                out.println(Codage.coder(line));
                if(quit = Codage.decoder(in.readLine()).equalsIgnoreCase("oui")){
                    quit = false;
                    premierPassage = false;
                }
                else {
                    quit = true;
                    premierPassage = true;
                }
             //   quit = Codage.decoder(in.readLine()).equalsIgnoreCase("oui") ? false : true;
            }

            line = "Connexion terminee. Merci d'avoir utilise nos services.";
            out.println(Codage.coder(line));
            System.err.println("Un client a termine la connexion.");
            getClient().close();
        } catch (IOException | EmpruntException | InterruptedException e) {
            e.printStackTrace();
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


    private int emprunt(int numeroAdherent, BufferedReader in, PrintWriter out, boolean premierPassage) throws IOException, EmpruntException, InterruptedException {
        String line;
        if (premierPassage) {
            out.println(Codage.coder("Veuillez entrer votre numero d'adherent : "));
            line = Codage.decoder(in.readLine());
            while ((numeroAdherent = numIsCorrect(line)) == -1 || !Data.AbboneExiste(numeroAdherent)) {
                line = "Veuillez entrer un numero valide.";
                out.println(Codage.coder(line));
                line = Codage.decoder(in.readLine());
            }
            numeroAdherent = Integer.parseInt(line);
        }
        Abonne abonne = Data.getAbonne(numeroAdherent);
         StringBuilder sb = new StringBuilder();
            if (Data.AbonneAEmpreunter(abonne).isEmpty()) {
                sb.append("Vous n'avez aucun document reserve.\n");
            } else {
                sb.append("Liste des documents reserves : \n" + Data.AbonneAEmpreunter(abonne) + "\n");
            }
            sb.append(abonne.getNom() + " entrez le numero du document que vous voulez emprunter : ");
            out.println(Codage.coder(sb.toString()));
            int numDocument;
            line = Codage.decoder(in.readLine());
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
                if (Data.AbonnePeutEmprunterDVD(document, abonne)) {
                    out.print(Codage.coder("le DVD est pour personne majeur\n"));
                } else {
                    Data.emprunter(document, abonne);
                    Data.retirerReservation(document);
                    out.print(Codage.coder("Emprunt effectue avec succes.\n"));
                }
            }
            return numeroAdherent;
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
