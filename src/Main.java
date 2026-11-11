//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;


void main(){
        HashMap<UUID, User> users = new HashMap<>();
        HashMap<String, LinkShort> linkData = new HashMap<>();
        User currentUser = null;
        Scanner input = new Scanner(System.in);

        while (true){

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
            System.out.println("4. Управление моими ссылками\n");
            System.out.println("0. Выход");
            System.out.println("================================");

            String command = input.nextLine().trim();

            switch (command) {

                case "1": {
                    while (true) {
                        System.out.println("\n========== ПОЛЬЗОВАТЕЛИ ==========");
                        System.out.println("1. Создать нового пользователя\n");
                        System.out.println("Существующие пользователи:");
                        ArrayList<UUID> userIds = new ArrayList<>();

                        int number = 2;

                        for (User user : users.values()) {
                            UUID userId = user.getId();
                            userIds.add(userId);
                            String id = userId.toString();
                            System.out.println(number + ". Пользователь "
                                    + id.substring(0, 8) + "..." + id.substring(28));
                            number++;
                        }

                        if (users.isEmpty()) {
                            System.out.println("Пользователей пока нет.");
                        }

                        System.out.println("\n0. Назад");
                        System.out.println("=================================");
                        System.out.print("Выберите пользователя: ");

                        String userChoice = input.nextLine().trim();

                        if (userChoice.equals("0")) {
                            break;
                        }

                        if (userChoice.equals("1")) {

                           // currentUser = User.createUser(users);

                            System.out.println("\nПользователь успешно создан!");
                            System.out.println("UUID: " + currentUser.getId());

                            break;
                        }

                        int selectedNumber;

                        try {
                            selectedNumber = Integer.parseInt(userChoice);
                        } catch (NumberFormatException e) {
                            System.out.println("Введите корректный номер.");
                            continue;
                        }

                        int selectedIndex = selectedNumber - 2;

                        if (selectedIndex >= 0 && selectedIndex < userIds.size()) {

                            UUID selectedId = userIds.get(selectedIndex);

                            currentUser = users.get(selectedId);

                            System.out.println("\nПользователь выбран!");

                            break;

                        } else {
                            System.out.println("Такого пользователя нет.");
                        }
                    }

                    break;
                }

                case "2": {
                    if (currentUser == null) {
                        System.out.println("Сначала выберите пользователя.");
                    } else {
                        System.out.println("Функция создания ссылки пока не реализована.");
                    }
                    break;
                }

                case "3": {
                    System.out.println("Функция перехода пока не реализована.");
                    break;
                }

                case "4": {
                    if (currentUser == null) {
                        System.out.println("Сначала выберите пользователя.");
                    } else {
                        System.out.println("Управление ссылками пока не реализовано.");
                    }
                    break;
                }

                case "0": {
                    System.out.println("Завершение программы.");
                    return;
                }

                default:
                    System.out.println("Неизвестная команда. Попробуйте ещё раз.");
                    break;
            }
        }
    }
