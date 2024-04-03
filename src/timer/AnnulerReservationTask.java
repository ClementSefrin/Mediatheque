package timer;

import app.Data;
import app.IDocument;

import java.util.TimerTask;

public class AnnulerReservationTask extends TimerTask {
    private final IDocument document;

    public AnnulerReservationTask(IDocument document) {
        this.document = document;
    }

    @Override
    public void run() {
        // Annuler la réservation si l'abonné n'a pas encore emprunté le document
        Data.retirerReservation(document);
        System.out.println("La réservation du document " + document + " a été annulée");
    }
}
