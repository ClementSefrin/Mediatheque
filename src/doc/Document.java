package doc;

import app.IDocument;

public class Document implements IDocument {
    private Abonne reservePar = null;
    private Abonne empruntePar = null;
    private final int numero;
    private final String titre;

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

    //TODO : les fonctions qui sont en-dessous ne changent pas l'état du document
    @Override
    public Abonne emprunteur() {
        return empruntePar;
    }

    @Override
    public Abonne reserveur() {
        return reservePar;
    }

    @Override
    public void reservationPour(Abonne ab) {
        if (reservePar == null && empruntePar == null) {
            reservePar = ab;
        }

    }

    @Override
    public void empruntPar(Abonne ab) {
        if (empruntePar == null || reservePar == ab) {
            empruntePar = ab;
            reservePar = null;
        }
    }

    @Override
    public void retour() {
            empruntePar = null;
            reservePar = null;
    }

    @Override
    public String toString() {
        return "Numero : " + numero + " | Titre : " + titre;
    }
}

