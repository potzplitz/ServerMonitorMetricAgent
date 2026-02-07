package de.potzplitz.core;

import de.potzplitz.core.Job;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Scheduler implements AutoCloseable {

    private final ScheduledExecutorService executor;
    private final ConcurrentMap<String, ScheduledFuture<?>> futures = new ConcurrentHashMap<>();
    private final AtomicBoolean started = new AtomicBoolean(false);

    public Scheduler(int threads) {
        this.executor = Executors.newScheduledThreadPool(threads, r -> {
            Thread t = new Thread(r);
            t.setName("agent-scheduler-" + t.getId());
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        System.out.println("Scheduler gestartet");
        started.set(true);
    }

    public void scheduleFixedDelay(Job job, Duration initialDelay, Duration delay) {
        ensureStarted();

        Runnable safe = () -> {
            try {
                job.runOnce();
            } catch (Exception e) {
                // wichtig: Exceptions dürfen NICHT den Scheduler killen
                System.err.println("[job:" + job.name() + "] failed: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        };

        ScheduledFuture<?> f = executor.scheduleWithFixedDelay(
                safe,
                initialDelay.toMillis(),
                delay.toMillis(),
                TimeUnit.MILLISECONDS
        );

        ScheduledFuture<?> prev = futures.put(job.name(), f);
        if (prev != null) prev.cancel(false);
    }

    public void cancel(String jobName) {
        ScheduledFuture<?> f = futures.remove(jobName);
        if (f != null) f.cancel(false);
    }

    public boolean isScheduled(String jobName) {
        ScheduledFuture<?> f = futures.get(jobName);
        return f != null && !f.isCancelled();
    }

    private void ensureStarted() {
        if (!started.get()) throw new IllegalStateException("Scheduler not started. Call start() first.");
    }

    @Override
    public void close() {
        for (ScheduledFuture<?> f : futures.values()) {
            f.cancel(false);
        }
        futures.clear();
        executor.shutdownNow();
    }
}
