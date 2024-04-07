package serveur;

import app.IDocument;
import app.Data;
import bttp3.AudioPlayer;
import codage.Codage;
import doc.Abonne;
import doc.Document;
import doc.EmpruntException;
import doc.ReservationInterditeException;
import timer.AnnulerReservationTask;
import timer.TimerReservation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ServiceReservation extends Service {
    private Timer timer = new Timer();
    private TimerTask annulerReservationTask;

    public ServiceReservation(Socket socket) {
        super(socket);
    }

    @Override
    public void run() {
        try {
            try {
                System.out.println("Traitement du client : " + this.getClient().getInetAddress() + ","
                        + this.getClient().getPort());
            BufferedReader in = new BufferedReader(new InputStreamReader(this.getClient().getInputStream()));
            PrintWriter out = new PrintWriter(this.getClient().getOutputStream(), true);

            out.println(Codage.coder("Saisissez votre numero d'adherent > "));
            String line = Codage.decoder(in.readLine());
            int numeroAdherent;

            while ((numeroAdherent = ServiceUtils.numIsCorrect(line)) == -1 && Data.getAbonne(numeroAdherent) != null) {
                line = "Veuillez entrer un numero valide.";
                out.println(Codage.coder(line));
                line = Codage.decoder(in.readLine());
                ServiceUtils.checkConnectionStatus(line, getClient());
            }

            Abonne abonne = Data.getAbonne(numeroAdherent);
            boolean continuer = true;
            if (abonne.estBanni()) {
                out.print(Codage.coder("Le grand chef Geronimo vous a banni jusqu'au : "
                        + abonne.getDateBannissement() + "\n"));
                continuer = false;
            }

            while (continuer) {
                assert abonne != null;
                out.println(Codage.coder("Que voulez-vous reserver, " + abonne.getNom() + " ? > "));
                int numDocs = Integer.parseInt(Codage.decoder(in.readLine()));
                ServiceUtils.checkConnectionStatus(line, getClient());

                IDocument doc = Data.getDocument(numDocs);
                String message;
                if (doc == null)
                    message = "Ce document n'existe pas.";
                else {
                    if (Data.estReserve(doc)) {
                        TimerReservation timerReservation = Data.getTimerReservation(doc);
                        if (timerReservation.getTempsRestant() <= 30_000) {
                            out.println(Codage.coder("La reservation de ce document touche a sa fin. Veuillez " +
                                    "patienter quelques instants en ecoutant notre musique celeste."));
                            AudioPlayer.playAudio("../musique/waiting_song.wav");
                            Thread.sleep(90_000); // Faire patienter le client (1m30)
                            AudioPlayer.stopAudio();

                            if (Data.estReserve(doc)) {
                                message = "L'envoutement etait trop fort ! Vous auriez du faire une offrande plus" +
                                        " importante au grand chaman.";
                                break;
                            } else message = "Envoutement vaincu ! ";
                        } else message = "Ce document est deja reserve.";
                    }
                    else if (Data.estEmprunte(doc)) {
                        message = "Ce document est deja emprunte.";
                    } else {
                        if (Data.abonnePeutPasEmprunterDVD(doc, abonne)) {
                            message = "Le document est reserve aux personnes majeures";
                        } else {
                            out.println(Codage.coder("Etes-vous sur de vouloir reserver le document suivant : "
                                    + doc.getTitre() + " ? (oui/non) > "));

                            while (!line.equalsIgnoreCase("oui") && !line.equalsIgnoreCase("non")) {
                                line = "Veuillez entrer une reponse valide.";
                                out.println(Codage.coder(line));
                                line = Codage.decoder(in.readLine());
                                ServiceUtils.checkConnectionStatus(line, getClient());
                            }

                            if (line.equalsIgnoreCase("oui")) {
                                // try {
                                    Data.reserver(doc, abonne, timer);
                                    System.out.println(Data.adherentAReserve(doc, abonne));
                                    timer.schedule(new AnnulerReservationTask(doc, timer), 120_000); // 2min = 2h
                                    message = "Vous avez bien reserve " + doc.getTitre()
                                            + "\nVous avez deux heures pour venir l'emprunter";
                                //TODO : gérer avec exception la réservation d'un document pour adulte par un mineur
                                /*} catch (ReservationInterditeException e) {
                                    out.print(Codage.coder(e.getMessage()));
                                }*/
                            } else {
                                message = "Reservation annulee";
                            }
                        }
                    }
                }
                out.println(Codage.coder(message + "\nVoulez-vous continuer ? (oui/non) > "));
                continuer = Codage.decoder(in.readLine()).trim().equalsIgnoreCase("oui");
                ServiceUtils.checkConnectionStatus(line, getClient());
            }

            ServiceUtils.endConnection(this.getClient());
            } catch (IOException | InterruptedException e) {
                throw new FinConnexionException("Connexion terminee. Merci d'avoir utilise nos services.");
            }
        } catch (FinConnexionException e) {
            System.err.println(e.getMessage());
        }
    }
}
