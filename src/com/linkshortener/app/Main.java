package com.linkshortener.app;

import com.linkshortener.repository.UserRepository;
import com.linkshortener.repository.LinkRepository;
import com.linkshortener.repository.memory.InMemoryUserRepository;
import com.linkshortener.repository.memory.InMemoryLinkRepository;

import com.linkshortener.service.UserService;
import com.linkshortener.service.LinkService;
import com.linkshortener.service.ShortCodeGenerator;

import com.linkshortener.ui.ConsoleMenu;
import com.linkshortener.browser.DesktopBrowserOpener;

import com.linkshortener.notification.NotificationService;
import com.linkshortener.scheduler.LinkExpirationScheduler;

import java.time.Clock;
import java.time.Duration;

public class Main {

    public static void main(String[] args) {

        // Создание хранилищ
        UserRepository userRepository = new InMemoryUserRepository();
        LinkRepository linkRepository = new InMemoryLinkRepository();

        // Создание уведомлений
        NotificationService notificationService =
                new NotificationService();

        // Создание сервисов
        UserService userService =
                new UserService(userRepository);

        LinkService linkService = new LinkService(
                linkRepository,
                userRepository,
                new ShortCodeGenerator(8),
                Duration.ofHours(24),
                1000,
                Clock.systemUTC(),
                new DesktopBrowserOpener(),
                notificationService
        );

        // Создание планировщика
        LinkExpirationScheduler expirationScheduler =
                new LinkExpirationScheduler(
                        linkService,
                        notificationService,
                        Duration.ofMinutes(1)
                );

        // Запуск приложения
        try (expirationScheduler) {

            expirationScheduler.start();

            ConsoleMenu menu = new ConsoleMenu(
                    userService,
                    linkService
            );

            menu.run();
        }
    }
}