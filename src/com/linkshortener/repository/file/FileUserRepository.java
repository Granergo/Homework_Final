package com.linkshortener.repository.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.linkshortener.model.User;
import com.linkshortener.repository.UserRepository;

import java.io.IOException;
import java.io.UncheckedIOException;

import java.nio.file.Path;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import java.util.Objects;

public class FileUserRepository implements UserRepository {

    private static final int FILE_VERSION = 1;

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private final Path file;

    private final Map<UUID, User> users = new LinkedHashMap<>();

    public FileUserRepository(Path file) throws IOException {

        this.file = Objects.requireNonNull(file)
                .toAbsolutePath()
                .normalize();

        // Загружаем пользователей из JSON
        UserFile document = JsonFileStorage.readOrCreate(
                this.file,
                GSON,
                UserFile.class,
                () -> new UserFile(new ArrayList<>())
        );

        loadUsers(document);
    }

    private void loadUsers(UserFile document) throws IOException {

        if (document.version != FILE_VERSION) {
            throw new IOException(
                    "Неподдерживаемая версия users.json."
            );
        }

        if (document.users == null) {
            throw new IOException(
                    "В users.json отсутствует список пользователей."
            );
        }

        for (UserData data : document.users) {

            if (data == null || data.id == null || data.id.isBlank()) {
                throw new IOException(
                        "Обнаружена некорректная запись пользователя."
                );
            }

            UUID id;

            try {
                id = UUID.fromString(data.id);

            } catch (IllegalArgumentException e) {

                throw new IOException(
                        "Некорректный UUID пользователя: " + data.id,
                        e
                );
            }

            User previous = users.putIfAbsent(id, new User(id));

            if (previous != null) {
                throw new IOException(
                        "Обнаружен повторяющийся UUID: " + id
                );
            }
        }
    }

    @Override
    public synchronized void save(User user) {

        Objects.requireNonNull(user);

        UUID id = user.getId();

        User previous = users.put(id, user);

        try {

            persist();

        } catch (UncheckedIOException e) {

            // Возвращаем предыдущее состояние,
            // если запись на диск завершилась ошибкой
            if (previous == null) {
                users.remove(id);
            } else {
                users.put(id, previous);
            }

            throw e;
        }
    }

    @Override
    public synchronized Optional<User> findById(UUID id) {

        return Optional.ofNullable(users.get(id));
    }

    @Override
    public synchronized List<User> findAll() {

        return List.copyOf(users.values());
    }

    private void persist() {

        List<UserData> data = new ArrayList<>();

        for (User user : users.values()) {
            data.add(new UserData(user.getId().toString()));
        }

        UserFile document = new UserFile(data);

        try {

            JsonFileStorage.writeAtomically(
                    file,
                    GSON,
                    document
            );

        } catch (IOException e) {

            throw new UncheckedIOException(
                    "Не удалось сохранить пользователей.",
                    e
            );
        }
    }

    // DTO для JSON-документа
    private static final class UserFile {

        private int version;

        private List<UserData> users;

        private UserFile() {
        }

        private UserFile(List<UserData> users) {
            this.version = FILE_VERSION;
            this.users = users;
        }
    }

    // DTO отдельного пользователя
    private static final class UserData {

        private String id;

        private UserData() {
        }

        private UserData(String id) {
            this.id = id;
        }
    }
}