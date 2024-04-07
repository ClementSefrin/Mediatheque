package doc;

import app.Data;
import app.IDocument;
import doc.types.DVD;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;


public class Document implements IDocument {
    private Abonne reservePar = null;
    private Abonne empruntePar = null;
    private LocalDateTime dateEmprunt = null;
    private final int numero;
    private final String titre;
    private static boolean documentAbime = false;
    private static final int PROBA_DOC_ABIME = 100; // probabilité de 1/100
    private static final int DOC_EST_ABIME = 0;


    public Document(int numero, String titre, Abonne abonne, EtatDemande etat) {
        this.numero = numero;
        this.titre = titre;
        if (abonne != null || etat != EtatDemande.DISPONIBLE) {
            if (etat == EtatDemande.RESERVE)
                reservePar = abonne;
            else if (etat == EtatDemande.EMPRUNTE)
                empruntePar = abonne;
        }
    }

    public Document(int numero, String titre) {
        this.numero = numero;
        this.titre = titre;
    }

    @Override
    public LocalDateTime dateEmprunt() {
        return dateEmprunt;
    }

    public static String dateEmpruntFormat(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    @Override
    public void RamdomDocumentAbime(){
        Random rand = new Random();
        int randomNum = rand.nextInt(PROBA_DOC_ABIME);
        documentAbime = (randomNum == DOC_EST_ABIME);
    }


    @Override
    public boolean getDocumentAbime() {
        return documentAbime;
    }
    @Override
    public int numero() {
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
        if (reservePar == null && empruntePar == null)
            reservePar = ab;
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
            }
        }
    }

    @Override
    public String toString() {
        return "Numero : " + numero + " | Titre : " + titre;
    }
}

