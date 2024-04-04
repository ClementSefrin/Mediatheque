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
                ServiceUtils.checkConnectionStatus(line, getClient(), out);

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

    private void retour(BufferedReader in, PrintWriter out) throws IOException, EmpruntException {
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


    private int emprunt(int numeroAdherent, BufferedReader in, PrintWriter out, boolean premierPassage)
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
