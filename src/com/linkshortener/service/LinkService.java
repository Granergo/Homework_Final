package com.linkshortener.service;

import com.linkshortener.model.LinkShort;
import com.linkshortener.repository.LinkRepository;
import com.linkshortener.repository.UserRepository;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.io.IOException;
import java.time.Instant;

public class LinkService {

    private static final int MAX_GENERATION_ATTEMPTS = 100;

    private final LinkRepository linkRepository;
    private final UserRepository userRepository;
    private final ShortCodeGenerator codeGenerator;


    private final Duration ttl;
    private final int maxAllowedClicks;
    private final Clock clock;
    private final BrowserOpener browserOpener;

    public LinkService(
            LinkRepository linkRepository,
            UserRepository userRepository,
            ShortCodeGenerator codeGenerator,
            Duration ttl,
            int maxAllowedClicks,
            Clock clock,
            BrowserOpener browserOpener
    ) {
        this.linkRepository = Objects.requireNonNull(linkRepository);
        this.userRepository = Objects.requireNonNull(userRepository);
        this.codeGenerator = Objects.requireNonNull(codeGenerator);
        this.browserOpener = Objects.requireNonNull(browserOpener);

        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("TTL должен быть положительным.");
        }

        if (maxAllowedClicks <= 0) {
            throw new IllegalArgumentException("Максимальный лимит должен быть положительным.");
        }

        this.ttl = ttl;
        this.maxAllowedClicks = maxAllowedClicks;
        this.clock = Objects.requireNonNull(clock);
    }

    public LinkVisitResult visitLink(String code)
            throws IOException, URISyntaxException {

        if (code == null || code.isBlank()) {
            return LinkVisitResult.NOT_FOUND;
        }

        LinkShort link = linkRepository.findByCode(code)
                .orElse(null);

        if (link == null) {
            return LinkVisitResult.NOT_FOUND;
        }

        // Проверяем срок действия
        Instant now = clock.instant();

        if (link.isExpired(now)) {

            linkRepository.deleteByCode(code);

            return LinkVisitResult.EXPIRED;
        }

        // Резервируем один переход
        if (!link.reserveUse(now)) {

            // Повторно проверяем TTL на случай изменения времени
            if (link.isExpired(clock.instant())) {

                linkRepository.deleteByCode(code);

                return LinkVisitResult.EXPIRED;
            }

            return LinkVisitResult.LIMIT_REACHED;
        }

        // Пытаемся открыть браузер
        try {

            browserOpener.open(link.getUrlOriginal());

        } catch (IOException | URISyntaxException | RuntimeException e) {

            // Если браузер не открылся, отменяем учёт перехода
            link.rollbackUse();

            linkRepository.save(code, link);

            throw e;
        }

        // Сохраняем успешный переход
        linkRepository.save(code, link);

        return LinkVisitResult.OPENED;
    }

    public String createLink(String url, int useMax, UUID ownerId) {

        if (ownerId == null || userRepository.findById(ownerId).isEmpty()) {
            throw new IllegalArgumentException("Пользователь не найден.");
        }

        String validatedUrl = validateUrl(url);

        if (useMax <= 0 || useMax > maxAllowedClicks) {
            throw new IllegalArgumentException("Недопустимый лимит переходов.");
        }

        LinkShort link = new LinkShort(
                validatedUrl,
                useMax,
                ownerId,
                ttl,
                clock.instant()
        );

        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {

            String code = codeGenerator.generate();

            if (linkRepository.insertIfAbsent(code, link)) {
                return code;
            }
        }

        throw new IllegalStateException(
                "Не удалось сгенерировать уникальный короткий код."
        );
    }

    public Optional<LinkShort> findByCode(String code) {
        return linkRepository.findByCode(code);
    }

    public Map<String, LinkShort> getLinksByOwner(UUID ownerId) {
        return linkRepository.findByOwner(ownerId);
    }

    public void updateUseMax(String code, UUID requesterId, int newMax) {

        LinkShort link = findOwnedLink(code, requesterId);

        if (newMax > maxAllowedClicks) {
            throw new IllegalArgumentException(
                    "Лимит превышает максимально допустимое значение."
            );
        }

        link.setUseMax(newMax);
        linkRepository.save(code, link);
    }

    public void deleteLink(String code, UUID requesterId) {

        findOwnedLink(code, requesterId);

        linkRepository.deleteByCode(code);
    }

    public Map<String, LinkShort> cleanupExpiredLinks() {
        return linkRepository.deleteExpired(clock.instant());
    }

    private LinkShort findOwnedLink(String code, UUID requesterId) {

        Objects.requireNonNull(requesterId, "UUID пользователя не может быть null.");

        LinkShort link = linkRepository.findByCode(code)
                .orElseThrow(() ->
                        new NoSuchElementException("Короткая ссылка не найдена.")
                );

        if (!link.getOwnerId().equals(requesterId)) {
            throw new SecurityException("Вы не являетесь владельцем этой ссылки.");
        }

        return link;
    }

    private String validateUrl(String url) {

        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL не может быть пустым.");
        }

        String normalizedUrl = url.trim();

        try {
            URI uri = new URI(normalizedUrl);

            String scheme = uri.getScheme();

            boolean validScheme = scheme != null
                    && (scheme.equalsIgnoreCase("http")
                    || scheme.equalsIgnoreCase("https"));

            if (!validScheme || uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException("Введите корректный HTTP или HTTPS URL.");
            }

        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Некорректный формат URL.", e);
        }

        return normalizedUrl;
    }
}
