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

public class ServiceReservation extends Service {
    private Abonne abonne;

    public ServiceReservation(Socket socket) {
        super(socket);
    }

    @Override
    public void run() {
        try {
            System.out.println("Traitement du client : " + this.getClient().getInetAddress() + "," + this.getClient().getPort());

            BufferedReader in = new BufferedReader(new InputStreamReader(this.getClient().getInputStream()));
            PrintWriter out = new PrintWriter(this.getClient().getOutputStream(), true);

            out.println(Codage.coder("Saisissez votre numero d'adherent > "));
            String line = in.readLine();
            int numeroAdherent;

            while ((numeroAdherent = numIsCorrect(line)) == -1 && Data.getAbonne(numeroAdherent) != null) {
                line = "Veuillez entrer un numero valide.";
                out.println(Codage.coder(line));
                line = Codage.decoder(in.readLine());
            }

            abonne = Data.getAbonne(numeroAdherent);
            boolean continuer = true;

            while (continuer) {
                out.println(Codage.coder("Que voulez-vous reserver, " + abonne.getNom() + " ? > "));
                int numDocs = Integer.parseInt(in.readLine());

                IDocument doc = Data.getDocument(numDocs - 1);
                if (doc == null)
                    out.println(Codage.coder("Ce document n'existe pas."));
                else {
                    if (Data.estReserver(doc) || Data.estEmprunter(doc))
                        out.println(Codage.coder("Ce document est deja reserve ou emprunte."));
                    else {
                        if(Data.abonnePeutPasEmprunterDVD(doc, abonne)){
                            out.println(Codage.coder("Le document est reserve aux personnes majeures"));
                        }
                        else {
                            // TODO : enregistrer la reservation
                            out.print(Codage.coder("Vous avez bien reserve " + doc + "\n"));
                        }
                    }
                }

                out.println(Codage.coder("Voulez-vous continuer ? (oui/non)"));
                continuer = in.readLine().equalsIgnoreCase("oui") ? true : false;
            }

            out.println(Codage.coder("Connexion terminee. Merci d'avoir utilise nos services."));
        } catch (IOException e) {
            System.err.println("Client deconnecte ?");
            try {
                this.getClient().close();
            } catch (IOException ignored) {}
        }
        try {
            this.getClient().close();
        } catch (IOException ignored) {}
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
