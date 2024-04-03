package timer;

import java.util.Timer;
import java.util.TimerTask;


public class TimerManager {
    private final Timer timer;

    public TimerManager() {
        this.timer = new Timer();
    }

    public void scheduleTask(TimerTask task, long delay) {
        timer.schedule(task, delay);
    }

    public void cancelTask(TimerTask task) {
        task.cancel();
    }
}