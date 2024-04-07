package app;

import doc.Abonne;
import doc.EmpruntException;
import timer.TimerReservation;

import java.time.LocalDateTime;
import java.util.Timer;


public interface IDocument {
    int getNumero();

    Abonne emprunteur(); // Abonne qui a emprunte le document

    Abonne reserveur(); // Abonne qui a reserve le document

    LocalDateTime getDateEmprunt();

    String getTitre();

    boolean getDocumentAbime();

    void ramdomDocumentAbime();

    // precondition : ni reserve ni emprunte
    // EmpruntException si ab n’a pas le droit de reserver le document
    void reservationPour(Abonne ab, Timer timer) throws EmpruntException;

    void annulerReservation();

    // precondition : libre ou reserve par l’abonne qui vient emprunter
    // EmpruntException si ab n’a pas le droit d’emprunter le document
    void empruntPar(Abonne ab) throws EmpruntException;

    // retour d’un document ou annulation d'une reservation
    void retour();

    boolean ajoutAlerteDisponibilite(String mail);

    TimerReservation getTimerReservation();
}
