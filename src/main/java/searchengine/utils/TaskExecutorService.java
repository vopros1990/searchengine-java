package searchengine.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;

@Component
public class TaskExecutorService {
    private ForkJoinPool forkJoinPool;

    @Value("${task-executor.shutdown-await-termination-seconds}")
    private long shutdownAwaitTerminationSeconds;

    public TaskExecutorService() {
        this.forkJoinPool = new ForkJoinPool(
                Runtime.getRuntime().availableProcessors(),
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                null,
                false
        );
    }

    public void execute (ForkJoinTask<?> task) {
        getDefaultForkJoinPool().execute(task);
    }

    public void shutdown() {
        forkJoinPool.shutdown();

        try {
            if (!forkJoinPool.awaitTermination(
                    shutdownAwaitTerminationSeconds,
                    TimeUnit.SECONDS)) {
                forkJoinPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            forkJoinPool.shutdownNow();
        }
    }

    private ForkJoinPool getDefaultForkJoinPool() {
        if (forkJoinPool.isShutdown()) {
            this.forkJoinPool = new ForkJoinPool(
                    Runtime.getRuntime().availableProcessors(),
                    ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                    null,
                    false
            );
        }

        return this.forkJoinPool;
    }
}
