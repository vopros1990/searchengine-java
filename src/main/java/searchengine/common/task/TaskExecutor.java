package searchengine.common.task;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
public class TaskExecutor {
    private ForkJoinPool forkJoinPool;

    @Value("${task-executor.shutdown-await-termination-seconds: 15}")
    private long shutdownAwaitTerminationSeconds;

    public TaskExecutor() {
        createDefaultForkJoinPool();
    }

    public void runAsync(Runnable task) {
        new Thread(task).start();
    }

    public <T> void submitAsync(ForkJoinTask<T> task,
                                Consumer<T> callback,
                                Consumer<Throwable> onFailure) {
        CompletableFuture
                .supplyAsync(
                        task::invoke,
                        getDefaultForkJoinPool()
                ).thenAccept(callback)
                .exceptionally(exception -> {
                    onFailure.accept(exception);
                    return null;
                });
    }

    public <T> T invoke(ForkJoinTask<T> task) {
        return getDefaultForkJoinPool().invoke(task);
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

    public ForkJoinPool getDefaultForkJoinPool() {
        if (forkJoinPool.isShutdown()) createDefaultForkJoinPool();
        return forkJoinPool;
    }

    private void createDefaultForkJoinPool() {
        forkJoinPool =  new ForkJoinPool(
                Runtime.getRuntime().availableProcessors(),
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                null,
                false
        );
    }
}
