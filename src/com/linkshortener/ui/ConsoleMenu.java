package com.linkshortener.ui;

import com.linkshortener.model.User;
import com.linkshortener.service.UserService;
import com.linkshortener.service.LinkService;
import com.linkshortener.service.LinkVisitResult;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Scanner;

public class ConsoleMenu {

    private final UserService userService;
    private final LinkService linkService;

    private final Scanner input = new Scanner(System.in);

    private User currentUser = null;

    public ConsoleMenu(UserService userService, LinkService linkService) {
        this.userService = userService;
        this.linkService = linkService;
    }

    public void run() {

        while (true) {

            System.out.println("================================");
            System.out.println("        LINK SHORTENER");
            System.out.println("================================");

            if (currentUser == null) {
                System.out.println("Текущий пользователь: не выбран\n");
            } else {
                String id = currentUser.getId().toString();

                System.out.println("Текущий пользователь: "
                        + id.substring(0, 8) + "..." + id.substring(28) + "\n");
            }

            System.out.println("1. Выбор пользователя");
            System.out.println("2. Создать короткую ссылку");
            System.out.println("3. Перейти по короткой ссылке");
            System.out.println("4. Управление моими ссылками");
            System.out.println("5. Помощь\n");
            System.out.println("0. Выход");
            System.out.println("================================");

            String command = input.nextLine().trim();

            switch (command) {

                case "1":
                    selectUser();
                    break;

                case "2":
                    createLinkMenu();
                    break;

                case "3":
                    openLinkMenu();
                    break;

                case "4":
                    manageLinksMenu();
                    break;

                case "5":
                    showHelp();
                    break;

                case "0":
                    System.out.println("Завершение программы.");
                    return;

                default:
                    System.out.println("Неизвестная команда. Попробуйте ещё раз.");
            }
        }
    }

    private void selectUser() {

        while (true) {

            System.out.println("\n========== ПОЛЬЗОВАТЕЛИ ==========");
            System.out.println("1. Создать нового пользователя\n");
            System.out.println("Существующие пользователи:");

            // Получаем актуальный список пользователей
            List<User> availableUsers = userService.findAll();

            for (int i = 0; i < availableUsers.size(); i++) {

                User user = availableUsers.get(i);
                String id = user.getId().toString();

                System.out.println((i + 2) + ". Пользователь "
                        + id.substring(0, 8) + "..." + id.substring(28));
            }

            if (availableUsers.isEmpty()) {
                System.out.println("Пользователей пока нет.");
            }

            System.out.println("\n0. Назад");
            System.out.println("=================================");
            System.out.print("Выберите пользователя: ");

            String userChoice = input.nextLine().trim();

            if (userChoice.equals("0")) {
                return;
            }

            if (userChoice.equals("1")) {

                currentUser = userService.createUser();

                System.out.println("\nПользователь успешно создан!");
                System.out.println("UUID: " + currentUser.getId());

                return;
            }

            try {

                int selectedNumber = Integer.parseInt(userChoice);
                int selectedIndex = selectedNumber - 2;

                if (selectedIndex >= 0 && selectedIndex < availableUsers.size()) {

                    currentUser = availableUsers.get(selectedIndex);

                    System.out.println("\nПользователь выбран!");
                    System.out.println("UUID: " + currentUser.getId());

                    return;

                } else {
                    System.out.println("Такого пользователя нет.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Введите корректный номер.");
            }
        }
    }


    private void createLinkMenu() {

        // Автоматическое создание пользователя при необходимости
        if (currentUser == null) {

            currentUser = userService.createUser();

            System.out.println("\nДля вас создан новый пользователь.");
            System.out.println("UUID: " + currentUser.getId());
        }

        System.out.println("\n========== СОЗДАНИЕ ССЫЛКИ ==========");
        System.out.println("Введите исходный URL.");
        System.out.println("Для отмены введите 0.");
        System.out.print("URL: ");

        String url = input.nextLine().trim();

        if (url.equals("0")) {
            System.out.println("Создание ссылки отменено.");
            return;
        }

        // Ввод максимального количества переходов
        int useMax;

        while (true) {

            System.out.print("Максимальное количество переходов (0 - отмена): ");

            String limitInput = input.nextLine().trim();

            if (limitInput.equals("0")) {
                System.out.println("Создание ссылки отменено.");
                return;
            }

            try {
                useMax = Integer.parseInt(limitInput);

                if (useMax <= 0) {
                    System.out.println("Лимит должен быть положительным числом.");
                    continue;
                }

                break;

            } catch (NumberFormatException e) {
                System.out.println("Введите целое положительное число.");
            }
        }

        // Создание ссылки через сервис
        try {

            String shortCode = linkService.createLink(
                    url,
                    useMax,
                    currentUser.getId()
            );

            System.out.println("\nСсылка успешно создана!");
            System.out.println("Исходный URL: " + url);
            System.out.println("Короткий код: " + shortCode);
            System.out.println("Лимит переходов: " + useMax);

        } catch (IllegalArgumentException | IllegalStateException e) {

            System.out.println("\nНе удалось создать ссылку.");
            System.out.println("Причина: " + e.getMessage());
        }
    }



    private void openLinkMenu() {

        System.out.println("\n========== ПЕРЕХОД ПО ССЫЛКЕ ==========");
        System.out.println("Введите короткий код.");
        System.out.println("Для отмены введите 0.");
        System.out.print("Короткий код: ");

        String code = input.nextLine().trim();

        if (code.equals("0")) {
            System.out.println("Переход отменён.");
            return;
        }

        try {

            LinkVisitResult result = linkService.visitLink(code);

            switch (result) {

                case OPENED:
                    System.out.println("\nБраузер успешно вызван.");
                    System.out.println("Переход учтён.");
                    break;

                case NOT_FOUND:
                    System.out.println("\nКороткая ссылка не найдена.");
                    break;

                case EXPIRED:
                    System.out.println("\nСрок действия ссылки истёк.");
                    System.out.println("Ссылка автоматически удалена.");
                    break;

                case LIMIT_REACHED:
                    System.out.println("\nЛимит переходов исчерпан.");
                    System.out.println("Использование ссылки заблокировано.");
                    break;
            }

        } catch (IOException | URISyntaxException |
                 UnsupportedOperationException | SecurityException e) {

            System.out.println("\nНе удалось открыть исходную ссылку.");
            System.out.println("Причина: " + e.getMessage());
            System.out.println("Переход не засчитан.");
        }
    }


    private void manageLinksMenu() {

        if (currentUser == null) {
            System.out.println("Сначала выберите пользователя.");
            return;
        }

        System.out.println("Управление ссылками пока не реализовано.");
    }

    private void showHelp() {

        System.out.println("\n========== ПОМОЩЬ ==========");
        System.out.println("1. Выбор пользователя");
        System.out.println("2. Создание короткой ссылки");
        System.out.println("3. Переход по короткой ссылке");
        System.out.println("4. Управление своими ссылками");
        System.out.println("5. Просмотр этой справки");
        System.out.println("0. Выход");
        System.out.println("============================\n");
    }
}
