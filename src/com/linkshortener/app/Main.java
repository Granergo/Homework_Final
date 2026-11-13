
package com.linkshortener.app;

import com.linkshortener.browser.DesktopBrowserOpener;
import com.linkshortener.config.AppConfig;
import com.linkshortener.notification.NotificationService;
import com.linkshortener.repository.LinkRepository;
import com.linkshortener.repository.UserRepository;
import com.linkshortener.repository.file.FileLinkRepository;
import com.linkshortener.repository.file.FileUserRepository;
import com.linkshortener.scheduler.LinkExpirationScheduler;
import com.linkshortener.service.LinkService;
import com.linkshortener.service.ShortCodeGenerator;
import com.linkshortener.service.UserService;
import com.linkshortener.ui.ConsoleMenu;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {

        try {
            startApplication();

        } catch (IOException | RuntimeException e) {
            System.err.println(
                    "Не удалось запустить Link Shortener: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }

    private static void startApplication() throws IOException {

        // Загружаем настройки приложения
        AppConfig config = AppConfig.load(
                Path.of("config.properties")
        );

        // Инициализируем файловые репозитории
        UserRepository userRepository =
                new FileUserRepository(config.getUsersFile());

        LinkRepository linkRepository =
                new FileLinkRepository(config.getLinksFile());

        // Создаём сервис уведомлений
        NotificationService notificationService =
                new NotificationService();

        // Создаём бизнес-сервисы
        UserService userService =
                new UserService(userRepository);

        LinkService linkService = new LinkService(
                linkRepository,
                userRepository,
                new ShortCodeGenerator(config.getCodeLength()),
                config.getLinkTtl(),
                config.getMaxAllowedClicks(),
                Clock.systemUTC(),
                new DesktopBrowserOpener(),
                notificationService
        );

        // Удаляем ссылки, истёкшие во время отсутствия приложения
        linkService.cleanupExpiredLinks();

        // Запускаем периодическую очистку
        try (LinkExpirationScheduler scheduler =
                     new LinkExpirationScheduler(
                             linkService,
                             notificationService,
                             config.getCleanupInterval()
                     )) {

            scheduler.start();

            ConsoleMenu menu = new ConsoleMenu(
                    userService,
                    linkService
            );

            menu.run();
        }
    }
}
