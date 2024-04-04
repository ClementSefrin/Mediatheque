package timer;

import app.Data;
import app.IDocument;
import doc.EmpruntException;

import java.util.Timer;
import java.util.TimerTask;

public class AnnulerReservationTask extends TimerTask {
    private final IDocument document;
    private final Timer timer;

    public AnnulerReservationTask(IDocument document, Timer timer) {
        this.document = document;
        this.timer = timer;
    }

    @Override
    public void run() {
        timer.cancel();
        try {
            Data.retirerReservation(document);
        } catch (EmpruntException e) {
            throw new RuntimeException(e);
        }
        System.out.println("La réservation du document " + document + " a été retirée");
    }
}
