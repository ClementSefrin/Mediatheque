package doc;

import app.Data;
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
        synchronized (this){
            // TODO : à faire
        }
    }

    @Override
    public void empruntPar(Abonne ab) throws EmpruntException {
        synchronized (this) {
            if(this instanceof DVD && !Data.abonnePeutPasEmprunterDVD(this, ab)){
                throw new EmpruntException("Desole, vous ne pouvez pas emprunter ce DVD, car vous êtes mineur.");
            }
            if (empruntePar == null && reservePar == null) {
                if(Data.emprunt(this, ab)) {
                    empruntePar = null;
                    reservePar = ab;
                    Data.ajoutEmprunt(this, ab);
                }
            } else {
                throw new EmpruntException("Le document est deja reserve ou emprunte.");
            }

        }
    }

    @Override
    public void retour() {
        synchronized (this){
            Data.retour(this);
            if (empruntePar != null) {
                empruntePar = null;
            } else if (reservePar != null) {
                Data.retirerReservation(this);
                reservePar = null;
            }

        }
    }

    @Override
    public String toString() {
        return "Numero : " + numero + " | Titre : " + titre;
    }
}

