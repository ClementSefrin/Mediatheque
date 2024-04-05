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
    private Abonne abonne;

    public ServiceEmpruntRetour(Socket socket) {
        super(socket);
        abonne = null;
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
                    emprunt(in, out);
                else if (line.equalsIgnoreCase("Retour"))
                    retour(in, out);
                line = "Vouler-vous continuer? (Oui/Non)";
                out.println(Codage.coder(line));
                quit = Codage.decoder(in.readLine()).equalsIgnoreCase("non");
            }

            ServiceUtils.endConnection(getClient(), out);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void retour(BufferedReader in, PrintWriter out) throws IOException {
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

        IDocument document = Data.getDocument(numDoc);
        document.retour();
        out.print(Codage.coder("Document retourne avec succès.\n"));
    }


    private void emprunt(BufferedReader in, PrintWriter out) throws IOException, InterruptedException {
        String line;
        int numeroAdherent;
        if (abonne == null) {
            out.println(Codage.coder("Veuillez entrer votre numero d'adherent : "));
            line = Codage.decoder(in.readLine());
            while ((numeroAdherent = ServiceUtils.numIsCorrect(line)) == -1 || !Data.abonneExiste(numeroAdherent)) {
                line = "Veuillez entrer un numero valide.";
                out.println(Codage.coder(line));
                line = Codage.decoder(in.readLine());
            }
            numeroAdherent = Integer.parseInt(line);
            abonne = Data.getAbonne(numeroAdherent);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(Data.afficherDocumentsReserves(abonne));
        sb.append("Entrez le numero du document que vous voulez emprunter : ");
        out.println(Codage.coder(sb.toString()));

        int numDocument;
        line = Codage.decoder(in.readLine());
        while ((numDocument = ServiceUtils.numIsCorrect(line)) == -1) {
            line = "Veuillez entrer un numero valide.";
            out.println(Codage.coder(line));
            line = Codage.decoder(in.readLine());
        }

        Document document = (Document) Data.getDocument(numDocument);
        while (!Data.documentExiste(document)) {
            line = "Veuillez entrer un numero valide.";
            out.println(Codage.coder(line));
            line = Codage.decoder(in.readLine());
            numDocument = ServiceUtils.numIsCorrect(line);
            document = (Document) Data.getDocument(numDocument);
        }

        try {
            document.empruntPar(abonne);
            out.print(Codage.coder("Emprunt effectue avec succès."));
        } catch (EmpruntException e) {
            out.print(Codage.coder(e.getMessage()));
        }
        out.print(Codage.coder("\n"));
    }
}
