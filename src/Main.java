//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
void main() {
    LinkShort onelink = new LinkShort();
    LinkShort twolink = new LinkShort();
    onelink.urlShort = "https://rutube.ru";
    twolink.urlShort = "https://ru.wikipedia.org";
    onelink.urlOriginal = "123456789";
    twolink.urlOriginal = "987654321";
    onelink.useMax = 10;
    twolink.useMax = 11;
    onelink.useCount = 7;
    twolink.useCount = 3;
    System.out.println("Первая ссылка");
    System.out.println("Код: "+onelink.urlShort);
    System.out.println("URL: "+onelink.urlOriginal);
    System.out.println("Лимит: "+ onelink.useMax);
    System.out.println("Переходов: "+ onelink.useCount);
    System.out.println("Вторая ссылка");
    System.out.println("Код: "+twolink.urlShort);
    System.out.println("URL: "+twolink.urlOriginal);
    System.out.println("Лимит: "+ twolink.useMax);
    System.out.println("Переходов: "+ twolink.useCount);
}
