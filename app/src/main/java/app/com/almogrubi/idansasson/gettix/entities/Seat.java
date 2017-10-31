package app.com.almogrubi.idansasson.gettix.entities;

import java.io.Serializable;

/**
 * Created by idans on 28/10/2017.
 */

public class Seat implements Serializable {

    private String uid;
    private int row;
    private int number;

    public String getUid() {
        return uid;
    }

    public int getRow() {
        return row;
    }

    public int getNumber() {
        return number;
    }

    // Default constructor required for calls to Firebase's DataSnapshot.getValue
    public Seat() {}

    public Seat(String uid, int row, int number) {
        this.uid = uid;
        this.row = row;
        this.number = number;
    }
}
