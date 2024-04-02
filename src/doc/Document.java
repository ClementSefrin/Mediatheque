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
            if(this instanceof DVD && Data.AbonnePeutPasEmprunterDVD(this, ab)){
                throw new EmpruntException("DVD réservé pour les majeurs uniquement.");
            }
            if (empruntePar == null && reservePar == null) {
                if(Data.reservation(this, ab)){
                    empruntePar = null;
                    reservePar = ab;
                }else{
                    throw new EmpruntException("Problème avec la base de données lors de la réservation.");
                }
            } else {
                throw new EmpruntException("Le document est déjà réservé ou emprunté.");
            }
        }
    }

    @Override
    public void empruntPar(Abonne ab) throws EmpruntException {
        synchronized (this) {
            if(this instanceof DVD && !Data.AbonnePeutPasEmprunterDVD(this, ab)){
                throw new EmpruntException("L'abonné ne peut pas emprunter ce document.");
            }
            if (empruntePar == null && reservePar == null) {
                if(Data.reservation(this, ab)) {
                    empruntePar = null;
                    reservePar = ab;
                }
            } else {
                throw new EmpruntException("Le document est déjà réservé ou emprunté.");
            }

        }
    }

    @Override
    public void retour() {
        synchronized (this){
            if (empruntePar != null) {
                empruntePar = null;
            } else if (reservePar != null) {
                reservePar = null;
            }
        }
    }

    @Override
    public String toString() {
        return "Numero : " + numero + " | Titre : " + titre;
    }
}

