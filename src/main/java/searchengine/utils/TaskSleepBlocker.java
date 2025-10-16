package searchengine.utils;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ManagedBlocker;

public class TaskSleepBlocker implements ManagedBlocker {
    private final long millis;
    private boolean releasable = false;

    public TaskSleepBlocker(long millis) {
        this.millis = millis;
    }

    @Override
    public boolean block() throws InterruptedException {
        if (!releasable) {
            Thread.sleep(millis);
            releasable = true;
        }
        return true;
    }

    @Override
    public boolean isReleasable() {
        return releasable;
    }

    public static void safeSleep(long millis) {
        try {
            ForkJoinPool.managedBlock(new TaskSleepBlocker(millis));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
