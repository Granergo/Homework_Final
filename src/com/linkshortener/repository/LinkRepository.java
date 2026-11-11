package com.linkshortener.repository;

import com.linkshortener.model.LinkShort;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface LinkRepository {

    boolean insertIfAbsent(String code, LinkShort link);

    void save(String code, LinkShort link);

    Optional<LinkShort> findByCode(String code);

    Map<String, LinkShort> findByOwner(UUID ownerId);

    boolean deleteByCode(String code);

    Map<String, LinkShort> deleteExpired(Instant now);
}
