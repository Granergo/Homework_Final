package com.linkshortener.test;

import com.linkshortener.model.LinkShort;
import com.linkshortener.model.User;

import com.linkshortener.repository.file.FileUserRepository;
import com.linkshortener.repository.file.FileLinkRepository;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.time.Duration;
import java.time.Instant;

import java.util.UUID;

public class PersistenceSmokeTest {

    public static void main(String[] args) throws IOException {

        // Создаём временную директорию для проверки
        Path directory = Files.createTempDirectory(
                "linkshortener-test-"
        );

        Path usersFile = directory.resolve("users.json");
        Path linksFile = directory.resolve("links.json");

        UUID ownerId = UUID.randomUUID();

        Instant createdAt = Instant.now();

        Duration ttl = Duration.ofHours(24);

        // Создаём хранилища
        FileUserRepository userRepository =
                new FileUserRepository(usersFile);

        FileLinkRepository linkRepository =
                new FileLinkRepository(linksFile);

        // Создаём пользователя
        User user = new User(ownerId);

        userRepository.save(user);

        // Создаём короткую ссылку
        LinkShort link = new LinkShort(
                "https://example.com",
                10,
                ownerId,
                ttl,
                createdAt
        );

        String code = "Test1234";

        linkRepository.insertIfAbsent(code, link);

        // Имитируем два успешных перехода
        link.reserveUse(createdAt.plusSeconds(5));
        link.reserveUse(createdAt.plusSeconds(10));

        // Сохраняем обновлённое состояние
        linkRepository.save(code, link);

        // ------------------------------------------------
        // Имитируем перезапуск приложения
        // Создаём новые экземпляры репозиториев
        // ------------------------------------------------

        FileUserRepository restoredUsers =
                new FileUserRepository(usersFile);

        FileLinkRepository restoredLinks =
                new FileLinkRepository(linksFile);

        // Проверяем восстановление пользователя
        User restoredUser = restoredUsers.findById(ownerId)
                .orElseThrow(() ->
                        new AssertionError("Пользователь не восстановлен.")
                );

        if (!restoredUser.getId().equals(ownerId)) {
            throw new AssertionError("UUID пользователя изменился.");
        }

        // Проверяем восстановление ссылки
        LinkShort restoredLink = restoredLinks.findByCode(code)
                .orElseThrow(() ->
                        new AssertionError("Ссылка не восстановлена.")
                );

        // Проверяем владельца
        if (!restoredLink.getOwnerId().equals(ownerId)) {
            throw new AssertionError("UUID владельца изменился.");
        }

        // Проверяем исходный URL
        if (!restoredLink.getUrlOriginal().equals("https://example.com")) {
            throw new AssertionError("Исходный URL изменился.");
        }

        // Проверяем количество переходов
        if (restoredLink.getUseCount() != 2) {
            throw new AssertionError("Количество переходов не восстановлено.");
        }

        // Проверяем максимальный лимит
        if (restoredLink.getUseMax() != 10) {
            throw new AssertionError("Лимит переходов изменился.");
        }

        // Проверяем дату создания
        if (!restoredLink.getCreatedAt().equals(createdAt)) {
            throw new AssertionError("Дата создания изменилась.");
        }

        // Проверяем точную дату окончания TTL
        if (!restoredLink.getExpiresAt().equals(createdAt.plus(ttl))) {
            throw new AssertionError("Дата истечения TTL изменилась.");
        }

        // Проверяем поиск ссылок владельца
        if (!restoredLinks.findByOwner(ownerId).containsKey(code)) {
            throw new AssertionError("Ссылка владельца не найдена.");
        }

        System.out.println(
                "Проверка файлового хранения успешно завершена!"
        );

        System.out.println(
                "Временные файлы находятся в: " + directory
        );
    }
}