package app.com.almogrubi.idansasson.gettix.entities;

/**
 * Created by idans on 21/10/2017.
 */

public class Producer {

    public String userId;
    public String userName;
    public String email;

    // Default constructor required for calls to Firebase's DataSnapshot.getValue
    public Producer() {}

    public Producer(String userId, String userName, String email) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getEmail() {
        return this.email;
    }
}
