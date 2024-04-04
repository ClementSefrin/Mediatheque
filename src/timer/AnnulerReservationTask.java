package timer;

import app.Data;
import app.IDocument;

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
        Data.retirerReservation(document);
        System.out.println("La réservation du document " + document + " a été retirée");
    }
}
