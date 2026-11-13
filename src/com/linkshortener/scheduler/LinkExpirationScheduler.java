
package com.linkshortener.scheduler;

import com.linkshortener.notification.NotificationEvent;
import com.linkshortener.notification.NotificationService;
import com.linkshortener.service.LinkService;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class LinkExpirationScheduler implements AutoCloseable {

    private final LinkService linkService;
    private final NotificationService notificationService;
    private final long intervalMillis;

    private final ScheduledExecutorService scheduler;

    private boolean started;
    private boolean closed;

    public LinkExpirationScheduler(
            LinkService linkService,
            NotificationService notificationService,
            Duration cleanupInterval
    ) {
        this.linkService = Objects.requireNonNull(linkService);
        this.notificationService = Objects.requireNonNull(notificationService);

        if (cleanupInterval == null
                || cleanupInterval.isZero()
                || cleanupInterval.isNegative()) {
            throw new IllegalArgumentException(
                    "Интервал очистки должен быть положительным."
            );
        }

        try {
            this.intervalMillis = cleanupInterval.toMillis();

        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "Интервал очистки слишком велик.",
                    e
            );
        }

        if (intervalMillis <= 0) {
            throw new IllegalArgumentException(
                    "Интервал очистки должен составлять не менее 1 мс."
            );
        }

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(
                    r,
                    "link-expiration-scheduler"
            );

            thread.setDaemon(true);

            return thread;
        });
    }

    public synchronized void start() {

        if (closed) {
            throw new IllegalStateException(
                    "Планировщик уже завершён."
            );
        }

        if (started) {
            throw new IllegalStateException(
                    "Планировщик уже запущен."
            );
        }

        started = true;

        scheduler.scheduleWithFixedDelay(
                this::cleanupExpiredLinks,
                intervalMillis,
                intervalMillis,
                TimeUnit.MILLISECONDS
        );
    }

    private void cleanupExpiredLinks() {

        try {
            linkService.cleanupExpiredLinks();

        } catch (RuntimeException e) {

            notificationService.publish(new NotificationEvent(
                    NotificationEvent.Type.SCHEDULER_ERROR,
                    Instant.now(),
                    null,
                    null,
                    "Не удалось выполнить очистку: " + e.getMessage()
            ));
        }
    }

    @Override
    public void close() {

        synchronized (this) {
            if (closed) {
                return;
            }

            closed = true;
            scheduler.shutdown();
        }

        try {

            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }

        } catch (InterruptedException e) {

            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
