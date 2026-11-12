package com.linkshortener.repository.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.linkshortener.model.LinkShort;
import com.linkshortener.repository.LinkRepository;

import java.io.IOException;
import java.io.UncheckedIOException;

import java.nio.file.Path;

import java.time.Instant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import java.util.Objects;

public class FileLinkRepository implements LinkRepository {

    private static final int FILE_VERSION = 1;

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private final Path file;

    private final Map<String, LinkShort> links = new LinkedHashMap<>();

    public FileLinkRepository(Path file) throws IOException {

        this.file = Objects.requireNonNull(file)
                .toAbsolutePath()
                .normalize();

        // Загружаем ссылки из JSON
        LinkFile document = JsonFileStorage.readOrCreate(
                this.file,
                GSON,
                LinkFile.class,
                () -> new LinkFile(new ArrayList<>())
        );

        loadLinks(document);
    }

    private void loadLinks(LinkFile document) throws IOException {

        if (document.version != FILE_VERSION) {
            throw new IOException(
                    "Неподдерживаемая версия links.json."
            );
        }

        if (document.links == null) {
            throw new IOException(
                    "В links.json отсутствует список ссылок."
            );
        }

        for (LinkData data : document.links) {

            if (data == null || data.code == null || data.code.isBlank()) {
                throw new IOException(
                        "Обнаружена некорректная запись ссылки."
                );
            }

            LinkShort link;

            try {

                link = new LinkShort(
                        data.urlOriginal,
                        UUID.fromString(data.ownerId),
                        Instant.parse(data.createdAt),
                        Instant.parse(data.expiresAt),
                        data.useCount,
                        data.useMax
                );

            } catch (RuntimeException e) {

                throw new IOException(
                        "Некорректные данные ссылки: " + data.code,
                        e
                );
            }

            LinkShort previous = links.putIfAbsent(data.code, link);

            if (previous != null) {
                throw new IOException(
                        "Повторяющийся короткий код: " + data.code
                );
            }
        }
    }

    @Override
    public synchronized boolean insertIfAbsent(
            String code,
            LinkShort link
    ) {

        validateCode(code);

        Objects.requireNonNull(link);

        if (links.containsKey(code)) {
            return false;
        }

        links.put(code, link);

        try {

            persist();

        } catch (UncheckedIOException e) {

            links.remove(code);

            throw e;
        }

        return true;
    }

    @Override
    public synchronized void save(String code, LinkShort link) {

        validateCode(code);

        Objects.requireNonNull(link);

        LinkShort previous = links.put(code, link);

        try {

            persist();

        } catch (UncheckedIOException e) {

            if (previous == null) {
                links.remove(code);
            } else {
                links.put(code, previous);
            }

            throw e;
        }
    }

    @Override
    public synchronized Optional<LinkShort> findByCode(String code) {

        if (code == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(links.get(code));
    }

    @Override
    public synchronized Map<String, LinkShort> findByOwner(UUID ownerId) {

        Map<String, LinkShort> result = new LinkedHashMap<>();

        links.forEach((code, link) -> {

            if (link.getOwnerId().equals(ownerId)) {
                result.put(code, link);
            }
        });

        return Map.copyOf(result);
    }

    @Override
    public synchronized boolean deleteByCode(String code) {

        if (code == null) {
            return false;
        }

        LinkShort removed = links.remove(code);

        if (removed == null) {
            return false;
        }

        try {

            persist();

        } catch (UncheckedIOException e) {

            // Восстанавливаем ссылку при ошибке записи
            links.put(code, removed);

            throw e;
        }

        return true;
    }

    @Override
    public synchronized Map<String, LinkShort> deleteExpired(Instant now) {

        Objects.requireNonNull(now);

        Map<String, LinkShort> removed = new LinkedHashMap<>();

        Iterator<Map.Entry<String, LinkShort>> iterator =
                links.entrySet().iterator();

        while (iterator.hasNext()) {

            Map.Entry<String, LinkShort> entry = iterator.next();

            if (entry.getValue().isExpired(now)) {

                removed.put(entry.getKey(), entry.getValue());

                iterator.remove();
            }
        }

        if (removed.isEmpty()) {
            return Map.of();
        }

        try {

            persist();

        } catch (UncheckedIOException e) {

            // Возвращаем удалённые ссылки при ошибке записи
            links.putAll(removed);

            throw e;
        }

        return Map.copyOf(removed);
    }

    private void validateCode(String code) {

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "Короткий код не может быть пустым."
            );
        }
    }

    private void persist() {

        List<LinkData> data = new ArrayList<>();

        links.forEach((code, link) -> {

            data.add(new LinkData(
                    code,
                    link.getUrlOriginal(),
                    link.getOwnerId().toString(),
                    link.getCreatedAt().toString(),
                    link.getExpiresAt().toString(),
                    link.getUseCount(),
                    link.getUseMax()
            ));
        });

        LinkFile document = new LinkFile(data);

        try {

            JsonFileStorage.writeAtomically(
                    file,
                    GSON,
                    document
            );

        } catch (IOException e) {

            throw new UncheckedIOException(
                    "Не удалось сохранить короткие ссылки.",
                    e
            );
        }
    }

    // DTO всего JSON-документа
    private static final class LinkFile {

        private int version;

        private List<LinkData> links;

        private LinkFile() {
        }

        private LinkFile(List<LinkData> links) {
            this.version = FILE_VERSION;
            this.links = links;
        }
    }

    // DTO отдельной короткой ссылки
    private static final class LinkData {

        private String code;
        private String urlOriginal;
        private String ownerId;
        private String createdAt;
        private String expiresAt;

        private int useCount;
        private int useMax;

        private LinkData() {
        }

        private LinkData(
                String code,
                String urlOriginal,
                String ownerId,
                String createdAt,
                String expiresAt,
                int useCount,
                int useMax
        ) {
            this.code = code;
            this.urlOriginal = urlOriginal;
            this.ownerId = ownerId;
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
            this.useCount = useCount;
            this.useMax = useMax;
        }
    }
}