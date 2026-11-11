
package com.linkshortener.app;

import com.linkshortener.repository.UserRepository;
import com.linkshortener.repository.LinkRepository;
import com.linkshortener.repository.memory.InMemoryUserRepository;
import com.linkshortener.repository.memory.InMemoryLinkRepository;
import com.linkshortener.service.UserService;
import com.linkshortener.service.LinkService;
import com.linkshortener.service.ShortCodeGenerator;
import com.linkshortener.ui.ConsoleMenu;

import java.time.Clock;
import java.time.Duration;

public class Main {

    public static void main(String[] args) {

        // Создание хранилищ
        UserRepository userRepository = new InMemoryUserRepository();
        LinkRepository linkRepository = new InMemoryLinkRepository();

        // Создание сервисов
        UserService userService = new UserService(userRepository);

        LinkService linkService = new LinkService(
                linkRepository,
                userRepository,
                new ShortCodeGenerator(8),
                Duration.ofHours(24),
                1000,
                Clock.systemUTC()
        );

        // Запуск консольного меню
        ConsoleMenu menu = new ConsoleMenu(userService, linkService);
        menu.run();
    }
}
