package com.linkshortener.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.Properties;

public final class AppConfig {

    private static final int MIN_CODE_LENGTH = 4;
    private static final int MAX_CODE_LENGTH = 32;

    private final Duration linkTtl;
    private final int maxAllowedClicks;
    private final int codeLength;
    private final Duration cleanupInterval;

    private final Path usersFile;
    private final Path linksFile;

    private AppConfig(
            Duration linkTtl,
            int maxAllowedClicks,
            int codeLength,
            Duration cleanupInterval,
            Path usersFile,
            Path linksFile
    ) {
        this.linkTtl = linkTtl;
        this.maxAllowedClicks = maxAllowedClicks;
        this.codeLength = codeLength;
        this.cleanupInterval = cleanupInterval;
        this.usersFile = usersFile;
        this.linksFile = linksFile;
    }

    public static AppConfig load(Path configPath) throws IOException {

        Objects.requireNonNull(configPath, "Путь к конфигурации не может быть null.");

        Path absoluteConfigPath = configPath.toAbsolutePath().normalize();

        Properties properties = new Properties();

        // Читаем конфигурационный файл
        try (Reader reader = Files.newBufferedReader(
                absoluteConfigPath,
                StandardCharsets.UTF_8
        )) {
            properties.load(reader);
        }

        // Получаем параметры
        long ttlSeconds = readLong(properties, "link.ttl.seconds");

        int maxAllowedClicks = readInt(properties, "link.max-clicks");

        int codeLength = readInt(properties, "link.code.length");

        long cleanupIntervalSeconds = readLong(
                properties,
                "link.cleanup.interval.seconds"
        );

        // Проверяем TTL
        if (ttlSeconds <= 0) {
            throw new IllegalArgumentException(
                    "link.ttl.seconds должен быть положительным."
            );
        }

        // Проверяем максимальный лимит
        if (maxAllowedClicks <= 0) {
            throw new IllegalArgumentException(
                    "link.max-clicks должен быть положительным."
            );
        }

        // Проверяем длину короткого кода
        if (codeLength < MIN_CODE_LENGTH || codeLength > MAX_CODE_LENGTH) {
            throw new IllegalArgumentException(
                    "link.code.length должен находиться в диапазоне "
                            + MIN_CODE_LENGTH + "-" + MAX_CODE_LENGTH + "."
            );
        }

        // Проверяем интервал очистки
        if (cleanupIntervalSeconds <= 0) {
            throw new IllegalArgumentException(
                    "link.cleanup.interval.seconds должен быть положительным."
            );
        }

        Duration linkTtl = Duration.ofSeconds(ttlSeconds);

        Duration cleanupInterval = Duration.ofSeconds(
                cleanupIntervalSeconds
        );

        // Получаем базовую директорию конфигурационного файла
        Path baseDirectory = absoluteConfigPath.getParent();

        // Преобразуем пути к файлам в абсолютные
        Path usersFile = resolvePath(
                properties,
                "storage.users-file",
                baseDirectory
        );

        Path linksFile = resolvePath(
                properties,
                "storage.links-file",
                baseDirectory
        );

        // Проверяем, что файлы пользователей и ссылок не совпадают
        if (usersFile.equals(linksFile)) {
            throw new IllegalArgumentException(
                    "Файлы пользователей и ссылок должны иметь разные пути."
            );
        }

        return new AppConfig(
                linkTtl,
                maxAllowedClicks,
                codeLength,
                cleanupInterval,
                usersFile,
                linksFile
        );
    }

    private static String readRequired(
            Properties properties,
            String key
    ) {

        String value = properties.getProperty(key);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Отсутствует обязательный параметр: " + key
            );
        }

        return value.trim();
    }

    private static int readInt(
            Properties properties,
            String key
    ) {

        String value = readRequired(properties, key);

        try {
            return Integer.parseInt(value);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Параметр " + key + " должен быть целым числом.",
                    e
            );
        }
    }

    private static long readLong(
            Properties properties,
            String key
    ) {

        String value = readRequired(properties, key);

        try {
            return Long.parseLong(value);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Параметр " + key + " должен быть целым числом.",
                    e
            );
        }
    }

    private static Path resolvePath(
            Properties properties,
            String key,
            Path baseDirectory
    ) {

        String value = readRequired(properties, key);

        try {

            Path path = Path.of(value);

            if (!path.isAbsolute()) {
                path = baseDirectory.resolve(path);
            }

            return path.toAbsolutePath().normalize();

        } catch (InvalidPathException e) {

            throw new IllegalArgumentException(
                    "Некорректный путь в параметре " + key + ".",
                    e
            );
        }
    }

    public Duration getLinkTtl() {
        return linkTtl;
    }

    public int getMaxAllowedClicks() {
        return maxAllowedClicks;
    }

    public int getCodeLength() {
        return codeLength;
    }

    public Duration getCleanupInterval() {
        return cleanupInterval;
    }

    public Path getUsersFile() {
        return usersFile;
    }

    public Path getLinksFile() {
        return linksFile;
    }
}
