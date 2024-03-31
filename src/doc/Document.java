package doc;

import app.IDocument;
import doc.types.DVD;

public class Document implements IDocument {
    private Abonne reservePar = null;
    private Abonne empruntePar = null;
    private final int numero;
    private final String titre;

    //TODO : equals
    public Document(int numero, String titre, Abonne abonne, EtatDemande etat) {
        this.numero = numero;
        this.titre = titre;
        if (abonne != null || etat != EtatDemande.DISPONIBLE) {
            if (etat == EtatDemande.RESERVE) {
                reservePar = abonne;
            } else if (etat == EtatDemande.EMPRUNTE) {
                empruntePar = abonne;
            }
        }
    }

    public Document(int numero, String titre) {
        this.numero = numero;
        this.titre = titre;
    }
    @Override
    public int getNumero() {
        return this.numero;
    }

    public String getTitre() {
        return titre;
    }

    @Override
    public int numero() {
        return numero;
    }

    public Abonne emprunteur() {
        return empruntePar;
    }

    @Override
    public Abonne reserveur() {
        return reservePar;
    }

    @Override
    public void reservationPour(Abonne ab) throws EmpruntException {
        if (reservePar == null && empruntePar == null) {
            reservePar = ab;
        } else {
            throw new EmpruntException("Le document est déjà réservé ou emprunté.");
        }
    }

    @Override
    public void empruntPar(Abonne ab) throws EmpruntException {
        if (empruntePar == null || reservePar == ab) {
            if (this instanceof DVD && ((DVD) this).estAdulte() && !ab.estMajeur()){
                System.out.println("On léve la première exception.");
                throw new EmpruntException("Les mineurs ne peuvent pas réserver de DVD pour adultes.");
            }
            empruntePar = ab;
            reservePar = null;
        } else {
            System.out.println("On léve la deuxième exception.");
            throw new EmpruntException("Le document est déjà emprunté ou réservé par quelqu'un d'autre.");
        }
    }

    @Override
    public void retour() {
        if (empruntePar != null) {
            //TODO: envoyer le retour à la BD
            empruntePar = null;
        } else if (reservePar != null) {
            // TODO: envoyer le retour à la BD
            reservePar = null;
        }
    }

    @Override
    public String toString() {
        return "Numero : " + numero + " | Titre : " + titre;
    }
}

