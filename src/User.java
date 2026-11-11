
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

    public static User createUser(HashMap<UUID, User> users) {
        User newUser = new User();

        users.put(newUser.getId(), newUser);

        return newUser;
    }
}
