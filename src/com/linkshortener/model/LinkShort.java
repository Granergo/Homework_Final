package com.linkshortener.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class LinkShort {

    private final String urlOriginal;
    private final UUID ownerId;
    private final Instant createdAt;
    private final Instant expiresAt;
    private int useCount;
    private int useMax;

    public LinkShort(
            String urlOriginal,
            int useMax,
            UUID ownerId,
            Duration ttl,
            Instant createdAt
    ) {
        if (urlOriginal == null || urlOriginal.isBlank()) {
            throw new IllegalArgumentException("URL не может быть пустым.");
        }

        if (useMax <= 0) {
            throw new IllegalArgumentException("Лимит переходов должен быть положительным.");
        }

        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("TTL должен быть положительным.");
        }

        this.urlOriginal = urlOriginal;
        this.useMax = useMax;
        this.useCount = 0;

        this.ownerId = Objects.requireNonNull(ownerId);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.expiresAt = createdAt.plus(ttl);
    }

    public String getUrlOriginal() {
        return urlOriginal;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public int getUseCount() {
        return useCount;
    }

    public int getUseMax() {
        return useMax;
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public boolean hasReachedLimit() {
        return useCount >= useMax;
    }


    public synchronized boolean reserveUse(Instant now) {

        if (isExpired(now) || useCount >= useMax) {
            return false;
        }

        useCount++;

        return true;
    }

    public synchronized void rollbackUse() {

        if (useCount > 0) {
            useCount--;
        }
    }


    public void setUseMax(int newMax) {
        if (newMax <= 0) {
            throw new IllegalArgumentException("Лимит должен быть положительным.");
        }

        if (newMax < useCount) {
            throw new IllegalArgumentException(
                    "Новый лимит не может быть меньше уже совершённых переходов."
            );
        }

        this.useMax = newMax;
    }
}

