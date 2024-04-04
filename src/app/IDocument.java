package app;

import doc.Abonne;

public interface IDocument {
    int getNumero();

    // return null si pas emprunté ou pas réservé
    Abonne emprunteur(); // Abonné qui a emprunté ce document

    Abonne reserveur(); // Abonné qui a réservé ce document

    // precondition : ni réservé ni emprunté
    void reservationPour(Abonne ab);

    // precondition : libre ou réservé par l’abonné qui vient emprunter
    void empruntPar(Abonne ab);

    // retour d’un document ou annulation d'une réservation
    void retour() throws EmpruntException;

}
