
public class LinkShort {
    String urlOriginal;
    String urlShort;
    int useCount;
    int useMax;
    public String useLink(String useResault, int useCount, int useMax){
        if (useCount<useMax) useResault ="Успешный переход";
        return useResault;
    }
}
