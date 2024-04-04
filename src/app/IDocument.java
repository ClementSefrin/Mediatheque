package app;

import doc.Abonne;
import doc.EmpruntException;


public interface IDocument {
    int numero();
    int getNumero();
    // return null si pas emprunté ou pas réservé
    Abonne emprunteur() ; // Abonné qui a emprunté ce document
    Abonne reserveur() ; // Abonné qui a réservé ce document

    String getTitre();
    // precondition : ni réservé ni emprunté
    // EmpruntException si ab n’a pas le droit de réserver CE document
    void reservationPour(Abonne ab) throws EmpruntException;

    // precondition : libre ou réservé par l’abonné qui vient emprunter
    // EmpruntException si ab n’a pas le droit d’emprunter CE document
    void empruntPar(Abonne ab) throws EmpruntException;

    // retour d’un document ou annulation d'une réservation
    void retour() throws EmpruntException;

}
