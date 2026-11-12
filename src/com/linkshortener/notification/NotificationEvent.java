package com.linkshortener.notification;

import java.time.Instant;
import java.util.UUID;

public class NotificationEvent {

    public enum Type {
        LINK_EXPIRED,
        CLICK_LIMIT_REACHED,
        SCHEDULER_ERROR
    }

    private final Type type;
    private final Instant occurredAt;
    private final UUID ownerId;
    private final String shortCode;
    private final String message;

    public NotificationEvent(
            Type type,
            Instant occurredAt,
            UUID ownerId,
            String shortCode,
            String message
    ) {
        this.type = type;
        this.occurredAt = occurredAt;
        this.ownerId = ownerId;
        this.shortCode = shortCode;
        this.message = message;
    }

    public Type getType() {
        return type;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getMessage() {
        return message;
    }
}