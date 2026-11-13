package com.linkshortener.ui;
import com.linkshortener.model.LinkShort;
import com.linkshortener.model.User;
import com.linkshortener.service.UserService;
import com.linkshortener.service.LinkService;
import com.linkshortener.service.LinkVisitResult;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Scanner;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.NoSuchElementException;

public class ConsoleMenu {

    private final UserService userService;
    private final LinkService linkService;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                    .withZone(ZoneId.systemDefault());
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

    private List<Map.Entry<String, LinkShort>> getCurrentUserLinks() {

        List<Map.Entry<String, LinkShort>> links = new ArrayList<>(
                linkService.getLinksByOwner(currentUser.getId()).entrySet()
        );

        // Сортируем по короткому коду для стабильного отображения
        links.sort((first, second) ->
                first.getKey().compareTo(second.getKey())
        );

        return links;
    }

    private void showMyLinks() {

        List<Map.Entry<String, LinkShort>> links = getCurrentUserLinks();

        if (links.isEmpty()) {
            System.out.println("\nУ вас пока нет коротких ссылок.");
            return;
        }

        System.out.println("\n========== МОИ ССЫЛКИ ==========");

        Instant now = Instant.now();

        for (int i = 0; i < links.size(); i++) {

            Map.Entry<String, LinkShort> entry = links.get(i);
            LinkShort link = entry.getValue();

            String status;

            if (link.isExpired(now)) {
                status = "Срок действия истёк";
            } else if (link.hasReachedLimit()) {
                status = "Лимит исчерпан";
            } else {
                status = "Активна";
            }

            System.out.println("\n" + (i + 1) + ". Короткий код: " + entry.getKey());
            System.out.println("   Исходный URL: " + link.getUrlOriginal());
            System.out.println("   Переходы: " + link.getUseCount()
                    + " / " + link.getUseMax());
            System.out.println("   Статус: " + status);
            System.out.println("   Создана: " + DATE_FORMAT.format(link.getCreatedAt()));
            System.out.println("   Истекает: " + DATE_FORMAT.format(link.getExpiresAt()));
        }

        System.out.println("\n================================");
    }

    private String selectMyLinkCode() {

        List<Map.Entry<String, LinkShort>> links = getCurrentUserLinks();

        if (links.isEmpty()) {
            System.out.println("\nУ вас пока нет коротких ссылок.");
            return null;
        }

        System.out.println("\nВыберите ссылку:");

        for (int i = 0; i < links.size(); i++) {

            Map.Entry<String, LinkShort> entry = links.get(i);

            System.out.println((i + 1) + ". "
                    + entry.getKey()
                    + " | "
                    + entry.getValue().getUrlOriginal());
        }

        System.out.println("\n0. Назад");

        while (true) {

            System.out.print("Ваш выбор: ");

            String choice = input.nextLine().trim();

            if (choice.equals("0")) {
                return null;
            }

            try {

                int selectedNumber = Integer.parseInt(choice);
                int selectedIndex = selectedNumber - 1;

                if (selectedIndex >= 0 && selectedIndex < links.size()) {
                    return links.get(selectedIndex).getKey();
                }

                System.out.println("Такого номера нет.");

            } catch (NumberFormatException e) {
                System.out.println("Введите корректный номер.");
            }
        }
    }

    private void changeLinkLimit() {

        String code = selectMyLinkCode();

        if (code == null) {
            return;
        }

        System.out.println("\nТекущий лимит: "
                + linkService.findByCode(code).orElseThrow().getUseMax());

        System.out.println("Введите новый лимит (0 - отмена):");

        String limitInput = input.nextLine().trim();

        if (limitInput.equals("0")) {
            System.out.println("Изменение отменено.");
            return;
        }

        int newMax;

        try {
            newMax = Integer.parseInt(limitInput);

        } catch (NumberFormatException e) {
            System.out.println("Введите целое число.");
            return;
        }

        try {

            linkService.updateUseMax(
                    code,
                    currentUser.getId(),
                    newMax
            );

            System.out.println("\nЛимит успешно изменён!");

        } catch (IllegalArgumentException e) {

            System.out.println("\nНе удалось изменить лимит.");
            System.out.println("Причина: " + e.getMessage());

        } catch (SecurityException | NoSuchElementException e) {

            System.out.println("\nОперация запрещена.");
            System.out.println("Причина: " + e.getMessage());
        }
    }

    private void deleteMyLink() {

        String code = selectMyLinkCode();

        if (code == null) {
            return;
        }

        System.out.println("\nВы действительно хотите удалить ссылку "
                + code + "?");
        System.out.println("Введите ДА для подтверждения или 0 для отмены.");

        String confirmation = input.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("да")) {
            System.out.println("Удаление отменено.");
            return;
        }

        try {

            linkService.deleteLink(
                    code,
                    currentUser.getId()
            );

            System.out.println("\nСсылка успешно удалена!");

        } catch (SecurityException | NoSuchElementException e) {

            System.out.println("\nНе удалось удалить ссылку.");
            System.out.println("Причина: " + e.getMessage());
        }
    }



    private void manageLinksMenu() {

        if (currentUser == null) {
            System.out.println("Сначала выберите пользователя.");
            return;
        }

        while (true) {

            System.out.println("\n========== МОИ ССЫЛКИ ==========");
            System.out.println("1. Просмотреть мои ссылки");
            System.out.println("2. Изменить лимит переходов");
            System.out.println("3. Удалить ссылку\n");
            System.out.println("0. Назад");
            System.out.println("================================");

            String command = input.nextLine().trim();

            switch (command) {

                case "1":
                    showMyLinks();
                    break;

                case "2":
                    changeLinkLimit();
                    break;

                case "3":
                    deleteMyLink();
                    break;

                case "0":
                    return;

                default:
                    System.out.println("Неизвестная команда.");
            }
        }
    }



    private void showHelp() {

        System.out.println("""
            
            ============================================================
                              LINK SHORTENER
                              СПРАВКА
            ============================================================

            НАЗНАЧЕНИЕ ПРОГРАММЫ

            Link Shortener позволяет создавать короткие ссылки,
            открывать исходные URL, ограничивать количество переходов
            и управлять собственными ссылками.

            Каждая ссылка принадлежит определённому пользователю.
            Данные пользователей и ссылок сохраняются в JSON-файлах.

            ------------------------------------------------------------
            1. ВЫБОР ПОЛЬЗОВАТЕЛЯ
            ------------------------------------------------------------

            Позволяет выбрать существующего пользователя или создать
            нового.

            При создании пользователя автоматически генерируется UUID.
            Он используется для идентификации владельца ссылок.

            Если пользователь ещё не выбран, программа автоматически
            создаст нового при попытке создать короткую ссылку.

            ------------------------------------------------------------
            2. СОЗДАНИЕ КОРОТКОЙ ССЫЛКИ
            ------------------------------------------------------------

            Для создания ссылки необходимо указать:

            1) Исходный URL.
            2) Максимальное количество переходов.

            Требования к URL:
            - Адрес не должен быть пустым.
            - Поддерживаются протоколы HTTP и HTTPS.
            - Адрес должен содержать корректное имя хоста.

            Требования к лимиту:
            - Значение должно быть положительным целым числом.
            - Оно не должно превышать максимальный лимит программы.

            После создания программа выдаёт уникальный короткий код.

            Пример исходного URL:
            https://example.com/article?id=15

            Пример результата:
            Короткий код: aB12xYz8
            Лимит переходов: 10

            Ввод 0 во время запроса URL или лимита отменяет создание.

            ------------------------------------------------------------
            3. ПЕРЕХОД ПО КОРОТКОЙ ССЫЛКЕ
            ------------------------------------------------------------

            Позволяет открыть исходный адрес по короткому коду.

            Для перехода необходимо ввести короткий код.

            Возможные результаты:

            OPENED:
            Ссылка найдена, переход учтён, браузер вызван.

            NOT_FOUND:
            Ссылка с указанным кодом не найдена.

            EXPIRED:
            Срок действия ссылки истёк. Ссылка удаляется.

            LIMIT_REACHED:
            Лимит переходов исчерпан. Использование заблокировано.

            Если браузер не удалось открыть, переход не засчитывается.

            Ввод 0 отменяет операцию.

            ------------------------------------------------------------
            4. УПРАВЛЕНИЕ МОИМИ ССЫЛКАМИ
            ------------------------------------------------------------

            Доступно только после выбора пользователя.

            1. ПРОСМОТРЕТЬ МОИ ССЫЛКИ

               Показывает список ссылок текущего пользователя:
               - Короткий код.
               - Исходный URL.
               - Количество переходов и установленный лимит.
               - Текущий статус.
               - Дату создания.
               - Дату истечения срока действия.

               Возможные статусы:
               Активна, Лимит исчерпан, Срок действия истёк.

            2. ИЗМЕНИТЬ ЛИМИТ ПЕРЕХОДОВ

               Позволяет изменить максимальное количество переходов
               выбранной ссылки.

               Новый лимит должен быть положительным числом,
               не превышающим установленный максимум программы.

               Нельзя установить лимит меньше уже совершённого
               количества переходов.

            3. УДАЛИТЬ ССЫЛКУ

               Удаляет выбранную ссылку после подтверждения.

               Для подтверждения необходимо ввести ДА.
               Любой другой ввод отменяет удаление.

            ------------------------------------------------------------
            5. ПОМОЩЬ
            ------------------------------------------------------------

            Отображает данную справочную информацию.

            ------------------------------------------------------------
            0. ВЫХОД
            ------------------------------------------------------------

            Завершает работу приложения.

            ------------------------------------------------------------
            ДОПОЛНИТЕЛЬНАЯ ИНФОРМАЦИЯ
            ------------------------------------------------------------

            UUID:
            Уникальный идентификатор пользователя. Используется для
            определения владельца ссылок и разграничения доступа.

            TTL (Time To Live):
            Время жизни короткой ссылки. После его истечения ссылка
            становится недействительной и удаляется.

            Лимит переходов:
            Максимальное количество успешных переходов по ссылке.
            После достижения лимита дальнейшее использование
            блокируется.

            Уведомления:
            Программа регистрирует события об истечении срока действия
            ссылок, исчерпании лимита переходов и ошибках планировщика.

            Хранение данных:
            Пользователи и ссылки сохраняются в JSON-файлах.
            При следующем запуске приложения данные восстанавливаются.

            ------------------------------------------------------------
            НАВИГАЦИЯ
            ------------------------------------------------------------

            Вводите номер команды, соответствующий пункту меню.
            Вложенные меню позволяют вернуться назад с помощью 0,
            если это предусмотрено текущей операцией.

            ============================================================
            """);
    }

}
