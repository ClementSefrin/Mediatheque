package serveur;

import app.IDocument;
import app.Data;
import bttp3.AudioPlayer;
import codage.Codage;
import doc.Abonne;
import doc.Document;
import doc.EmpruntException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceReservation extends Service {
    private Timer timer = new Timer();
    private Abonne abonne;

    public ServiceReservation(Socket socket) {
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

                int numeroAdherent;
                String line = "";
                if (abonne == null) {
                    out.println(Codage.coder("Saisissez votre numero d'adherent > "));
                    line = Codage.decoder(in.readLine().trim());
                    ServiceUtils.checkConnectionStatus(line, getClient());

                    while ((numeroAdherent = ServiceUtils.numIsCorrect(line)) == -1 || !Data.abonneExiste(numeroAdherent)) {
                        line = "Veuillez entrer un numero valide.";
                        out.println(Codage.coder(line));
                        line = Codage.decoder(in.readLine().trim());
                        ServiceUtils.checkConnectionStatus(line, getClient());
                    }

                    numeroAdherent = Integer.parseInt(line);
                    abonne = Data.getAbonne(numeroAdherent);
                }


                boolean continuer = true;
                if (abonne.estBanni()) {
                    out.print(Codage.coder("Le grand chef Geronimo vous a banni jusqu'au : "
                        + abonne.getDateBannissement() + "\n"));
                    continuer = false;
                }

                while (continuer) {
                    assert abonne != null;
                    out.println(Codage.coder("Que voulez-vous reserver, " + abonne.getNom() + " ? > "));


                    int numDocs;
                    IDocument doc;

                    line = Codage.decoder(in.readLine().trim());
                    ServiceUtils.checkConnectionStatus(line, getClient());
                    while ((numDocs = ServiceUtils.numIsCorrect(line)) == -1
                        || !Data.documentExiste(doc = Data.getDocument(numDocs))) {
                        out.println(Codage.coder("Veuillez entrer un numero valide."));
                        line = Codage.decoder(in.readLine().trim());
                        ServiceUtils.checkConnectionStatus(line, getClient());
                    }

                    numDocs = Integer.parseInt(line);
                    doc = Data.getDocument(numDocs);

                    String message;
                    if (doc == null)
                        message = "Ce document n'existe pas.";
                    else {
                        if (Data.estReserve(doc)) {
                            TimerReservation timerReservation = doc.getTimerReservation();
                            if (timerReservation != null && timerReservation.getTempsRestant() <= 15_000) {
                                out.println(Codage.coder("La reservation de ce document touche a sa fin. Veuillez " +
                                    "patienter quelques instants en ecoutant notre musique celeste. A la fin de musique," +
                                    " entrez oui ou non pour valider la reservation du document"));
                                AudioPlayer.playAudio("../musique/waiting_song.wav");
                                Thread.sleep(timerReservation.getTempsRestant());
                                AudioPlayer.stopAudio();

                                if (Data.estReserve(doc)) {
                                    message = "L'envoutement etait trop fort ! Vous auriez du faire une offrande plus" +
                                        " importante au grand chaman.";
                                    break;
                                } else {
                                    line = Codage.decoder(in.readLine().trim());
                                    ServiceUtils.checkConnectionStatus(line, getClient());
                                    while (!line.equalsIgnoreCase("oui")
                                        && !line.equalsIgnoreCase("non")) {
                                        line = "Veuillez entrer une reponse valide.";
                                        out.println(Codage.coder(line));
                                        line = Codage.decoder(in.readLine().trim());
                                        ServiceUtils.checkConnectionStatus(line, getClient());
                                    }

                                    if (line.equalsIgnoreCase("oui")) {
                                        try {
                                            doc.reservationPour(abonne, timer);
                                            message = "Envoutement vaincu ! Vous avez bien reserve " + doc + "\n";
                                        } catch (EmpruntException e) {
                                            message = e.getMessage();
                                        }
                                    } else {
                                        message = "Reservation annule.";
                                    }
                                }
                            } else message = "Ce document est deja reserve.";
                        } else if (Data.estEmprunte(doc)) {
                            out.println(Codage.coder("Ce document est déjà emprunté. Voulez-vous recevoir une" +
                                " alerte lorsque le document sera de nouveau disponible? (Oui/Non)"));

                            line = Codage.decoder(in.readLine().trim());
                            ServiceUtils.checkConnectionStatus(line, getClient());
                            while (!line.equalsIgnoreCase("oui")
                                && !line.equalsIgnoreCase("non")) {
                                line = "Veuillez entrer une reponse valide.";
                                out.println(Codage.coder(line));
                                line = Codage.decoder(in.readLine().trim());
                                ServiceUtils.checkConnectionStatus(line, getClient());
                            }

                            if (line.equalsIgnoreCase("oui")) {
                                out.println(Codage.coder("Entrez l'adresse mail à laquelle vous souhaitez être" +
                                    " alerté."));
                                line = Codage.decoder(in.readLine().trim());
                                ServiceUtils.checkConnectionStatus(line, getClient());
                                while (!checkEmail(line)) {
                                    line = "Veuillez rentrer un mail au format valide.";
                                    out.println(Codage.coder(line));
                                    line = Codage.decoder(in.readLine().trim());
                                    ServiceUtils.checkConnectionStatus(line, getClient());
                                }

                                boolean ajout = doc.ajoutAlerteDisponibilite(line);
                                message = ajout ? "Vous recevrez une alerte lorsque le document sera de nouveau" +
                                    " disponible." : "Vous êtes déjà inscrit pour recevoir une alerte.";
                            } else {
                                message = "";
                            }

                        } else {
                            try {
                                doc.reservationPour(abonne, timer);
                                message = "Vous avez bien reserve " + doc;
                            } catch (EmpruntException e) {
                                message = e.getMessage();
                            }
                        }
                    }
                    out.println(Codage.coder(message + "\nVoulez-vous continuer ? (oui/non) > "));
                    line = Codage.decoder(in.readLine().trim());
                    ServiceUtils.checkConnectionStatus(line, getClient());

                    while (!line.equalsIgnoreCase("oui") && !line.equals("non")) {
                        line = "Veuillez entrer une reponse valide.";
                        out.println(Codage.coder(line));
                        line = Codage.decoder(in.readLine().trim());
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

    public boolean checkEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
