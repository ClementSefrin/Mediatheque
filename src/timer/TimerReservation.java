package timer;

import app.Data;
import app.IDocument;
import doc.EmpruntException;

import java.util.Timer;

public class TimerReservation {
    private final IDocument doc;
    private final Timer timer;
    long heureDebut;
    
    public TimerReservation(IDocument doc, Timer timer) {
        this.doc = doc;
        this.timer = timer;
        this.heureDebut = System.currentTimeMillis();
    }

    public IDocument getDoc() {
        return doc;
    }

    public void arreterReservation() throws EmpruntException {
        Data.arreterReservation(doc, timer);
    }

    public long getTempsRestant() {
        long tempsEcoule = System.currentTimeMillis() - heureDebut;
        return 120_000 - tempsEcoule;
    }
}
