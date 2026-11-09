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
    String inputUrlShort="";
    Scanner inputUrl = new Scanner(System.in);
    do{
        System.out.println("Введите короткий код (S для выхода)");
        inputUrlShort = inputUrl.next();

        if (inputUrlShort.equalsIgnoreCase("S")) {
            System.out.println("Завершение программы");

        }else{
            LinkShort link = linkData.get(inputUrlShort);

            if (link != null) {
                link.useLink();
                link.genLinkShort();
            } else {
                System.out.println("Ссылка не существует");
            }
        }
    }while (!inputUrlShort.equalsIgnoreCase("S"));
}
