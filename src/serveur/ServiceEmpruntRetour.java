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
import java.util.LinkedList;

public class ServiceEmpruntRetour extends Service {
    private boolean premierPassage = true;
    private int numeroAdherent = -1;
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
            while (!quit) {
                String demandeService = "A quel service souhaitez-vous acceder? (Emprunt/Retour)";
                out.println(Codage.coder(demandeService));
                line = Codage.decoder(in.readLine());

                while (!line.equalsIgnoreCase("Emprunt") && !line.equalsIgnoreCase("Retour")) {
                    out.println(Codage.coder("Ce service n'est pas disponible. \n" + demandeService));
                    line = Codage.decoder(in.readLine());
                }

                if (line.equalsIgnoreCase("Emprunt"))
                    numeroAdherent = emprunt(numeroAdherent, in, out);
                else if (line.equalsIgnoreCase("Retour"))
                    retour(in, out);
                line = "Vouler-vous continuer? (Oui/Non)";
                out.println(Codage.coder(line));
                if(quit = Codage.decoder(in.readLine()).equalsIgnoreCase("oui")){
                    quit = false;
                }
                else {
                    quit = true;
                    premierPassage = true;
                }
            }

            line = "Connexion terminee. Merci d'avoir utilise nos services.";
            out.println(Codage.coder(line));
            System.err.println("Un client a termine la connexion.");
            getClient().close();
        } catch (IOException | EmpruntException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void retour(BufferedReader in, PrintWriter out) throws IOException, EmpruntException {
        int numDoc;
        String line = "Entrez le numero du document que vous voulez retouner : ";
        out.println(Codage.coder(line));
        line = Codage.decoder(in.readLine());
        // ServiceUtils.checkConnectionStatus(line, getClient(), out);
        while ((numDoc = ServiceUtils.numIsCorrect(line)) == -1) {
            line = "Veuillez entrer un numero valide.";
            out.println(Codage.coder(line));
            line = Codage.decoder(in.readLine());
           // ServiceUtils.checkConnectionStatus(line, getClient(), out);
        }

        Document document = (Document) Data.getDocument(numDoc);

        assert document != null;
        if(Data.empruntOuReservation(document)){
            out.print(Codage.coder("Le document n'est pas emprunter ou reserve\n"));
            return;
        }

        if (Data.estEmprunte(document)) {
            document.retour();
            out.print(Codage.coder("Le document a bien ete retourne.\n"));
        }
        else{
            out.print(Codage.coder("Le document n'est pas emprunte.\n"));
        }
    }


    private int emprunt(int numeroAdherent, BufferedReader in, PrintWriter out)
            throws IOException, EmpruntException, InterruptedException {
        String line;
        if (premierPassage) {
            out.println(Codage.coder("Veuillez entrer votre numero d'adherent : "));
            line = Codage.decoder(in.readLine());
            while ((numeroAdherent = numIsCorrect(line)) == -1 || !Data.abonneExiste(numeroAdherent)) {
                line = "Veuillez entrer un numero valide.";
                out.println(Codage.coder(line));
                line = Codage.decoder(in.readLine());
            }
            premierPassage = false;
            numeroAdherent = Integer.parseInt(line);
        }

        Abonne abonne = Data.getAbonne(numeroAdherent);
        StringBuilder sb = new StringBuilder();

        if (Data.afficherDocumentsEmpruntes(abonne).isEmpty())
            sb.append("Vous n'avez aucun document reserve.\n");
        else
            sb.append("Vous avez un document reserve.\n").append(Data.afficherDocumentsEmpruntes(abonne)).append("\n");
        assert abonne != null;
        sb.append(abonne.getNom()).append(" entrez le numero du document que vous voulez emprunter : ");
        out.println(Codage.coder(sb.toString()));

        int numDocument;
        line = Codage.decoder(in.readLine());
        while ((numDocument = numIsCorrect(line)) == -1) {
            line = "Veuillez entrer un numero valide.";
            out.println(Codage.coder(line));
            line = Codage.decoder(in.readLine());
        }

        Document document = (Document) Data.getDocument(numDocument);
        while (!Data.documentExiste(document)) {
            line = "Veuillez entrer un numero valide.";
            out.println(Codage.coder(line));
            line = Codage.decoder(in.readLine());
            numDocument = numIsCorrect(line);
            document = (Document) Data.getDocument(numDocument);
        }

        try {
            assert document != null;
            document.empruntPar(abonne);
            out.print(Codage.coder("Emprunt effectue avec succes. Vous avez emprunte : " + document.getTitre() + "\n"));
        } catch (EmpruntException e) {
            out.print(Codage.coder( e + "\n"));
            out.flush();
            return numeroAdherent;
        }
        return numeroAdherent;
    }

    public static int numIsCorrect(String str) {
        try {
            int n = Integer.parseInt(str);
            if (n < 1) throw new NumberFormatException();
            return n;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
