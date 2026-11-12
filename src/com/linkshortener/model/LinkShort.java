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

    // Создание новой ссылки
    public LinkShort(
            String urlOriginal,
            int useMax,
            UUID ownerId,
            Duration ttl,
            Instant createdAt
    ) {
        if (urlOriginal == null || urlOriginal.isBlank()) {
            throw new IllegalArgumentException(
                    "URL не может быть пустым."
            );
        }

        if (useMax <= 0) {
            throw new IllegalArgumentException(
                    "Лимит переходов должен быть положительным."
            );
        }

        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException(
                    "TTL должен быть положительным."
            );
        }

        this.urlOriginal = urlOriginal;
        this.useMax = useMax;
        this.useCount = 0;

        this.ownerId = Objects.requireNonNull(ownerId);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.expiresAt = createdAt.plus(ttl);
    }

    // Восстановление существующей ссылки из JSON
    public LinkShort(
            String urlOriginal,
            UUID ownerId,
            Instant createdAt,
            Instant expiresAt,
            int useCount,
            int useMax
    ) {
        if (urlOriginal == null || urlOriginal.isBlank()) {
            throw new IllegalArgumentException(
                    "URL не может быть пустым."
            );
        }

        this.urlOriginal = urlOriginal;
        this.ownerId = Objects.requireNonNull(ownerId);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.expiresAt = Objects.requireNonNull(expiresAt);

        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException(
                    "Дата истечения должна быть позже даты создания."
            );
        }

        if (useMax <= 0) {
            throw new IllegalArgumentException(
                    "Лимит переходов должен быть положительным."
            );
        }

        if (useCount < 0 || useCount > useMax) {
            throw new IllegalArgumentException(
                    "Некорректное количество переходов."
            );
        }

        this.useCount = useCount;
        this.useMax = useMax;
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

        if (isExpired(now) || hasReachedLimit()) {
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
            throw new IllegalArgumentException(
                    "Лимит должен быть положительным."
            );
        }

        if (newMax < useCount) {
            throw new IllegalArgumentException(
                    "Новый лимит не может быть меньше уже совершённых переходов."
            );
        }

        this.useMax = newMax;
    }
}