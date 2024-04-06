package serveur;

import app.IDocument;
import app.Data;
import bttp3.AudioPlayer;
import codage.Codage;
import doc.Abonne;
import timer.AnnulerReservationTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ServiceReservation extends Service {
    private Timer timer = new Timer(); // TODO : mettre en partagé
    private TimerTask annulerReservationTask;

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

            while ((numeroAdherent = ServiceUtils.numIsCorrect(line)) == -1 && Data.getAbonne(numeroAdherent) != null) {
                line = "Veuillez entrer un numero valide.";
                out.println(Codage.coder(line));
                line = Codage.decoder(in.readLine());
            }


            Abonne abonne = Data.getAbonne(numeroAdherent);
            boolean continuer = true;
            if(abonne.estBanni()){
                out.println(Codage.coder("Vous etes banni jusqu'au " + abonne.getDateBanissement()));
                continuer = false;
            }

            while (continuer) {
                assert abonne != null;
                out.println(Codage.coder("Que voulez-vous reserver, " + abonne.getNom() + " ? > "));
                int numDocs = Integer.parseInt(in.readLine());

                IDocument doc = Data.getDocument(numDocs);
                String message;
                if (doc == null)
                    message = "Ce document n'existe pas.";
                else {
                    if (Data.estReserve(doc) || Data.estEmprunte(doc)) { // TODO : séparer les deux
                        if (true) { // TODO :  Trouver un moyen de connaître le temps restant
                            AudioPlayer.playAudio("../musique/waiting_song.wav");
                            // Faire patienter le client (30 sec)
                            Thread.sleep(30_000);
                            AudioPlayer.stopAudio();

                            if (Data.estReserve(doc)) {
                                message = "Envoûtement vaincu ! ";
                                break;
                            } else message = "L'envoûtement était trop fort ! Vous avez manqué le document.";
                        } else message = "Ce document est deja reserve ou emprunte.";
                    } else {
                        if (Data.abonnePeutPasEmprunterDVD(doc, abonne)) {
                            message = "Le document est reserve aux personnes majeures";
                        } else {
                            Data.reserver(doc, abonne);
                            System.out.println(Data.adherentAReserve(doc, abonne)); // TODO : renvoie false…
                            timer.schedule(new AnnulerReservationTask(doc, timer), 120_000); // 2min = 2h
                            message = "Vous avez bien reserve " + doc + "\n";
                        }
                    }
                }
                out.println(Codage.coder(message + "\nVoulez-vous continuer ? (oui/non)"));
                continuer = in.readLine().trim().equalsIgnoreCase("oui");
            }

            out.println(Codage.coder("Connexion terminee. Merci d'avoir utilise nos services."));
        } catch (IOException | InterruptedException e) {
            System.err.println("Client deconnecte ?");
            try {
                this.getClient().close();
            } catch (IOException ignored) {
            }
        }
        try {
            this.getClient().close();
        } catch (IOException ignored) {
        }
    }
}
