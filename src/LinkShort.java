public class LinkShort {
    private String urlOriginal;
    private String urlShort;
    private int useCount;
    private int useMax;
    public LinkShort(String urlOriginal, String urlShort, int useMax){
        this.urlOriginal = urlOriginal;
        this.urlShort = urlShort;
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
    public String getUrlShort(){
        return  urlShort;
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
}
