import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

public class LinkShort {
    private String urlOriginal;
    private int useCount;
    private int useMax;

    private final UUID ownerId;
    private final LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public LinkShort(String urlOriginal, int useMax, UUID ownerId) {
        this.urlOriginal = urlOriginal;
        this.useCount = 0;
        this.useMax = useMax;

        this.ownerId = ownerId;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = createdAt.plusHours(24);
    }

    public int getUseCount() {
        return useCount;
    }

    public int getUseMax() {
        return useMax;
    }

    public String getUrlOriginal() {
        return urlOriginal;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void setUseMax(int newMax) {
        if (newMax > 0) {
            useMax = newMax;
        }
    }

    public void useLink() {
        if (isExpired()) {
            System.out.println("Срок действия ссылки истёк.");
        } else if (useCount < useMax) {
            useCount++;
            System.out.println("Успешный переход");
        } else {
            System.out.println("Лимит исчерпан");
        }
    }
    SecureRandom random = new SecureRandom();
    public String genLinkShort(){
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
