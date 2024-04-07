package timer;

import app.IDocument;

import java.util.Timer;
import java.util.TimerTask;

public class TimerReservation {
    private static final long TPS_RESERVATION_MAX = 7_200_000;
    private final long heureDebut;

    public TimerReservation(IDocument doc, Timer timer) {
        this.heureDebut = System.currentTimeMillis();
        TimerTask annulerReservation = new AnnulerReservationTask(doc, timer);
        timer.schedule(annulerReservation, TPS_RESERVATION_MAX);
    }

    public long getTempsRestant() {
        long tempsEcoule = System.currentTimeMillis() - heureDebut;
        return TPS_RESERVATION_MAX - tempsEcoule;
    }
}
