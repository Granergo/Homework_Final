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

public class LinkExpirationScheduler implements AutoCloseable {

    private final LinkService linkService;
    private final NotificationService notificationService;
    private final Duration cleanupInterval;

    private final ScheduledExecutorService scheduler;

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

        this.cleanupInterval = cleanupInterval;

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "link-expiration-scheduler");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void start() {

        scheduler.scheduleWithFixedDelay(
                this::cleanupExpiredLinks,
                cleanupInterval.toMillis(),
                cleanupInterval.toMillis(),
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

        scheduler.shutdown();

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