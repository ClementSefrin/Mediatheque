package doc;

import app.Data;
import app.IDocument;
import doc.types.DVD;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Random;


public class Document implements IDocument {
    private Abonne reservePar = null;
    private Abonne empruntePar = null;
    private LocalDateTime dateEmprunt = null;
    private LinkedList<String> alerteDisponibilite;
    private final int numero;
    private final String titre;
    private static boolean estDocumentAbime = false;
    private static final int PROBA_DOC_ABIME = 100; // probabilite de 1/100
    private static final int DOC_EST_ABIME = 0;
    private static final String senderEmail = "clement.sefrin@gmail.com";
    private static final String appPassword = "jzsh zjyq snmg farl";
    private static final String mailSub = "Nouveau document disponible";

    public Document(int numero, String titre) {
        this.numero = numero;
        this.titre = titre;
        this.alerteDisponibilite = new LinkedList<>();
    }

    public Document(int numero, String titre, Abonne abonne, EtatDemande etat) {
        this(numero, titre);
        if (abonne != null || etat != EtatDemande.DISPONIBLE) {
            if (etat == EtatDemande.RESERVE)
                reservePar = abonne;
            else if (etat == EtatDemande.EMPRUNTE)
                empruntePar = abonne;
        }
    }

    @Override
    public boolean ajoutAlerteDisponibilite(String mail) {
        if (alerteDisponibilite.contains(mail))
            return false;
        alerteDisponibilite.add(mail);
        return true;
    }

    @Override
    public LocalDateTime dateEmprunt() {
        return dateEmprunt;
    }

    public static String dateEmpruntFormat() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    public static String dateFinEmpruntFormat() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return LocalDateTime.now().plusWeeks(2).format(formatter);
    }

    @Override
    public void ramdomDocumentAbime() {
        Random rand = new Random();
        int randomNum = rand.nextInt(PROBA_DOC_ABIME);
        estDocumentAbime = (randomNum == DOC_EST_ABIME);
    }

    @Override
    public boolean getDocumentAbime() {
        return estDocumentAbime;
    }

    @Override
    public int getNumero() {
        return numero;
    }

    @Override
    public String getTitre() {
        return titre;
    }

    @Override
    public Abonne emprunteur() {
        return empruntePar;
    }

    @Override
    public Abonne reserveur() {
        return reservePar;
    }

    @Override
    public void reservationPour(Abonne ab) throws EmpruntException {
        synchronized (this) {
            if (reservePar == null && empruntePar == null)
                reservePar = ab;
        }
    }

    @Override
    public void empruntPar(Abonne ab) throws EmpruntException {
        synchronized (this) {
            if (this.emprunteur() != null)
                throw new EmpruntException("Le document est deja emprunte.");
            if (this.reserveur() != null && !this.reserveur().equals(ab))
                throw new EmpruntException("Le document est reserve par une autre personne.");
            if (this instanceof DVD && Data.abonnePeutPasEmprunterDVD(this, ab))
                throw new EmpruntException("Vous ne pouvez pas emprunter ce DVD car vous etes mineur.");
            reservePar = null;
            empruntePar = ab;
            dateEmprunt = LocalDateTime.now();
        }
    }

    @Override
    public void retour() {
        synchronized (this) {
            if (empruntePar != null) {
                empruntePar = null;
                dateEmprunt = null;
                String message = "Le document suivant est de nouveau disponible : \n" + this.toString();
                for (String mail : alerteDisponibilite) {
                    send(senderEmail, appPassword, mail, mailSub, message);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Numero : " + numero + " | Titre : " + titre;
    }

    public void send(String from, String password, String to, String sub, String msg) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getDefaultInstance(props,
            new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(from, password);
                }
            });

        try {
            MimeMessage message = new MimeMessage(session);
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(sub);
            message.setText(msg);
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}

