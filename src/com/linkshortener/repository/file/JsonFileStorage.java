package com.linkshortener.repository.file;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import java.nio.file.AtomicMoveNotSupportedException;

import java.util.function.Supplier;

final class JsonFileStorage {

    private JsonFileStorage() {
    }

    public static <T> T readOrCreate(
            Path file,
            Gson gson,
            Class<T> dataType,
            Supplier<T> emptyDocument
    ) throws IOException {

        Path path = file.toAbsolutePath().normalize();

        // Создаём родительскую директорию, если её ещё нет
        Files.createDirectories(path.getParent());

        // Если файла нет, создаём пустой JSON-документ
        if (Files.notExists(path)) {

            T initialData = emptyDocument.get();

            writeAtomically(path, gson, initialData);

            return initialData;
        }

        // Пустой файл считаем повреждённым,
        // чтобы случайно не затереть существующие данные
        if (Files.size(path) == 0) {
            throw new IOException(
                    "JSON-файл пустой: " + path
            );
        }

        // Читаем существующий JSON
        try (Reader reader = Files.newBufferedReader(
                path,
                StandardCharsets.UTF_8
        )) {

            JsonReader jsonReader = new JsonReader(reader);

            jsonReader.setStrictness(Strictness.STRICT);

            T data = gson.fromJson(jsonReader, dataType);

            if (data == null) {
                throw new IOException(
                        "JSON-файл не содержит корректного документа: " + path
                );
            }

            return data;

        } catch (JsonParseException | IllegalStateException e) {

            throw new IOException(
                    "Не удалось прочитать JSON-файл: " + path,
                    e
            );
        }
    }

    public static void writeAtomically(
            Path file,
            Gson gson,
            Object document
    ) throws IOException {

        Path path = file.toAbsolutePath().normalize();

        Path parent = path.getParent();

        Files.createDirectories(parent);

        // Временный файл создаём в той же директории,
        // чтобы иметь возможность выполнить атомарное перемещение
        Path tempFile = Files.createTempFile(
                parent,
                "linkshortener-",
                ".tmp"
        );

        boolean moved = false;

        try {

            // Сначала записываем данные во временный файл
            try (Writer writer = Files.newBufferedWriter(
                    tempFile,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
            )) {

                gson.toJson(document, writer);
            }

            // Затем заменяем основной JSON-файл
            try {

                Files.move(
                        tempFile,
                        path,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                );

            } catch (AtomicMoveNotSupportedException e) {

                // Если файловая система не поддерживает
                // атомарное перемещение, используем обычное
                Files.move(
                        tempFile,
                        path,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            moved = true;

        } finally {

            // Удаляем временный файл, если запись не удалась
            if (!moved) {
                Files.deleteIfExists(tempFile);
            }
        }
    }
}