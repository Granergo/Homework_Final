import java.security.SecureRandom;

public class LinkShort {
    private String urlOriginal;
    private int useCount;
    private int useMax;
    public LinkShort(String urlOriginal, int useMax){
        this.urlOriginal = urlOriginal;
        this.useCount = 0;
        this.useMax = useMax;
    }
    public int getUseCount(){
        return  useCount;
    }
    public int getUseMax(){
        return  useMax;
    }
    public String getUrlOriginal(){
        return  urlOriginal;
    }
    public int setUseMax (int newMax){
        if (newMax>0)  useMax = newMax;
        return useMax;
    }
    public void useLink(){
        if (useCount<useMax) {
            useCount++;
            System.out.println("Успешный переход");
        } else {
            System.out.println("Лимит исчерпан");
        }
    }
    public String genLinkShort(){
        SecureRandom random = new SecureRandom();
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder urlShort = new StringBuilder(8);
        for (int i = 0;i<8;i++){
            int index = random.nextInt(alphabet.length());
            char symbol = alphabet.charAt(index);
            urlShort.append(symbol);
        }
        System.out.println(urlShort.toString());
        return urlShort.toString();
    }
}
