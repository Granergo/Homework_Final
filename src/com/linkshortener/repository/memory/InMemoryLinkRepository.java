package com.linkshortener.repository.memory;

import com.linkshortener.repository.LinkRepository;
import com.linkshortener.model.LinkShort;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryLinkRepository implements LinkRepository {

    private final Map<String, LinkShort> links = new ConcurrentHashMap<>();

    @Override
    public boolean insertIfAbsent(String code, LinkShort link) {
        return links.putIfAbsent(code, link) == null;
    }

    @Override
    public void save(String code, LinkShort link) {
        links.put(code, link);
    }

    @Override
    public Optional<LinkShort> findByCode(String code) {
        if (code == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(links.get(code));
    }

    @Override
    public Map<String, LinkShort> findByOwner(UUID ownerId) {
        Map<String, LinkShort> result = new HashMap<>();

        links.forEach((code, link) -> {
            if (link.getOwnerId().equals(ownerId)) {
                result.put(code, link);
            }
        });

        return Map.copyOf(result);
    }

    @Override
    public boolean deleteByCode(String code) {
        return links.remove(code) != null;
    }

    @Override
    public Map<String, LinkShort> deleteExpired(Instant now) {
        Map<String, LinkShort> removed = new HashMap<>();

        for (Map.Entry<String, LinkShort> entry : links.entrySet()) {

            String code = entry.getKey();
            LinkShort link = entry.getValue();

            if (link.isExpired(now) && links.remove(code, link)) {
                removed.put(code, link);
            }
        }

        return Map.copyOf(removed);
    }
}
