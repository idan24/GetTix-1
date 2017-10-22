package app.com.almogrubi.idansasson.gettix.entities;

/**
 * Created by idans on 21/10/2017.
 */

public class Seat {

    private String id;
    private int row;
    private int number;

    // Default constructor required for calls to Firebase's DataSnapshot.getValue
    public Seat() {}

    public Seat(String id, int row, int number) {
        this.id = id;
        this.row = row;
        this.number = number;
    }

    public String getId() {
        return this.id;
    }

    public int getRow() {
        return this.row;
    }

    public int getNumber() {
        return this.number;
    }
}
