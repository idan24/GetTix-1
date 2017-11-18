package app.com.almogrubi.idansasson.gettix.entities;

import java.io.Serializable;

/**
 * Created by idans on 11/11/2017.
 */

public class Customer implements Serializable {

    private String name;
    private String phone;
    private String email;

    // Default constructor required for calls to Firebase's DataSnapshot.getValue
    public Customer() {}

    public Customer(String name, String phone, String email) {
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
