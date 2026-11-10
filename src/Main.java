//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

void main() {
    HashMap<String, LinkShort> linkData = new HashMap<>();
    LinkShort link1 = new LinkShort("https://rutube.ru",10);
    LinkShort link2 = new LinkShort("https://",11);
    linkData.put("123456789", link1);
    linkData.put("123456790", link2);
    for (Map.Entry<String, LinkShort> entry : linkData.entrySet()) {
        String shortCode = entry.getKey();
        LinkShort links = entry.getValue();
        System.out.println("Код: " + shortCode);
        System.out.println("URL: " + links.getUrlOriginal());
    }
    //String inputUrlShort="";
    String nextCommand_main="";
    //Scanner inputUrl = new Scanner(System.in);
   // do{

       // System.out.println("Введите короткий код (S для выхода)");
       // inputUrlShort = inputUrl.next();

       // if (inputUrlShort.equalsIgnoreCase("S")) {
         //   System.out.println("Завершение программы");

       // }else{

           // LinkShort link = linkData.get(inputUrlShort);

          //  if (link != null) {
           //     link.useLink();
         //   } else {
          //      System.out.println("Ссылка не существует");
          //  }
      //  }
   // }while (!inputUrlShort.equalsIgnoreCase("S"));


    Scanner inputCommand = new Scanner(System.in);
    do{
        System.out.println("================================\n        LINK SHORTENER        \n================================");
        System.out.println("Текущий пользователь: 8f31...a92c\n");
        System.out.println("1. Выбор пользователя");
        System.out.println("2. Создать короткую ссылку");
        System.out.println("3. Перейти по короткой ссылке");
        System.out.println("4. Управление моими ссылками\n");
        System.out.println("0. Выход\n================================");
        nextCommand_main = inputCommand.next();
        switch (nextCommand_main){
            case "1":
                System.out.println("========== ПОЛЬЗОВАТЕЛИ ==========");
                System.out.println("1. Создать нового пользователя\n");
                System.out.println("Существующие пользователи:\n");
                //System.out.println("2. Пользователь 8f31...a92c"); Нужно проработать вывод списка пользователей чтобы было возможно выбрать пользователя
                System.out.println("0. Назад\n=================================");
                String nextCommand_1 = inputCommand.next();
                switch (nextCommand_1){
                    case "1":
                        //Обработка создать нового пользователя
                        break;
                    case "0":
                        System.out.println("================================\n        LINK SHORTENER        \n================================");
                        System.out.println("Текущий пользователь: 8f31...a92c\n");
                        System.out.println("1. Выбор пользователя");
                        System.out.println("2. Создать короткую ссылку");
                        System.out.println("3. Перейти по короткой ссылке");
                        System.out.println("4. Управление моими ссылками\n");
                        System.out.println("0. Выход\n================================");
                        nextCommand_1 = inputCommand.next();
                        break;
                    default:
                        //Обработка выбора пользователя
                        break;
                }
                break;
            case "2":
                System.out.println("========== СОЗДАНИЕ ССЫЛКИ ==========\n");
                System.out.println("Введите оригинальный URL (0 для отмены):");
                String nextUrlOriginal = inputCommand.next();
                System.out.println("Введите максимальное количество переходов (0 для отмены):");
                int nexUseMax = inputCommand.nextInt();
                System.out.println("Проверка URL...");
                //Запуск метода проверки ссылки
                System.out.println("Генерация короткого кода...\n");
                //Запуск метода генерации Url
                System.out.println("Ссылка успешно создана!\n");
                //Запуск метода вывода информации о ссылке
                break;
            case "3":
                System.out.println("========== ПЕРЕХОД ПО ССЫЛКЕ ==========\n");
                System.out.println("Введите короткий код (0 для отмены):");
                String nextUrlShort = inputCommand.next();
                System.out.println("Проверка ссылки...");
                //Запуск метода проверки ссылки
                System.out.println("Ссылка действительна.");
                System.out.println("Открытие в браузере...\n");
                //Метод перехода в браузере
                System.out.println("======================================");
                break;
            case "4":
                System.out.println("Здесь должен быть метод вывода ссылок");
                System.out.println("0. Назад\n");
                System.out.println("================================\n");
                System.out.println("Выберите ссылку:");
                String nextCommand_4 = inputCommand.next();
                switch (nextCommand_4){
                    case "2":
                        //не знаю как релизовать выбор ссылки в case веди ссылок не фиксированное значение
                        //потом добавить меню редактирования ссылки
                        break;
                }
                break;
            case "0":
                break;
        }
    }while (nextCommand_main.equalsIgnoreCase("0"));
}
