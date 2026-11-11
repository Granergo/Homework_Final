
package com.linkshortener.model;

import java.util.Objects;
import java.util.UUID;

public class User {

    private final UUID id;

    public User() {
        this(UUID.randomUUID());
    }

    public User(UUID id) {
        this.id = Objects.requireNonNull(id);
    }

    public UUID getId() {
        return id;
    }
}
