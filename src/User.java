
import java.util.UUID;
import java.util.HashMap;

public class User {
    private final UUID id;

    public User() {
        this.id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }
}


