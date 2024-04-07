package timer;

import app.Data;
import app.IDocument;
import doc.EmpruntException;

import java.util.Timer;
import java.util.TimerTask;

public class TimerReservation {
    private static final long delay = 30_000;
    private final IDocument doc;
    private final Timer timer;
    long heureDebut;

    public TimerReservation(IDocument doc, Timer timer) {
        this.doc = doc;
        this.timer = timer;
        this.heureDebut = System.currentTimeMillis();
        TimerTask annulerReservation = new AnnulerReservationTask(doc, timer);
        timer.schedule(annulerReservation, delay);
    }

    public void arreterReservation() throws EmpruntException {
        doc.annulerReservation();
    }

    public long getTempsRestant() {
        long tempsEcoule = System.currentTimeMillis() - heureDebut;
        return delay - tempsEcoule;
    }
}
