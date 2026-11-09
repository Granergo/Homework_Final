//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import java.util.Locale;
import java.util.Scanner;
import java.util.Random;
import java.util.Arrays;

void main() {
    ArrayList<LinkShort> linkData = new ArrayList<>();
    linkData.add(new LinkShort("https://rutube.ru","123456789",10));
    linkData.add(new LinkShort("https://ru.wikipedia.org", "987654321", 11));
    for (LinkShort linkShort: linkData){
        System.out.println("Код: " + linkShort.getUrlShort());
        System.out.println("URL: " + linkShort.getUrlOriginal());
        System.out.println("Лимит: " + linkShort.getUseMax());
        System.out.println("Переходов: " + linkShort.getUseCount());
    }
    String inputUrlShort="";
    Scanner inputUrl = new Scanner(System.in);
    do{
        boolean found = false;
        System.out.println("Введите короткий код (S для выхода)");
        inputUrlShort = inputUrl.next();

        if (inputUrlShort.equalsIgnoreCase("S")) {
            System.out.println("Завершение программы");

        }else
        for (LinkShort linkShort : linkData) {
            if (inputUrlShort.equals(linkShort.getUrlShort())) {
                linkShort.useLink();
                found = true;
                break;
            }
        }
        if (!found) {
            System.out.println("Ссылка не существует");
        }
    }while (!inputUrlShort.equalsIgnoreCase("S"));
}
