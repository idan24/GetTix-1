package app.com.almogrubi.idansasson.gettix.entities;

/**
 * Created by idans on 28/10/2017.
 */

public class Seat {

    private String id;
    private int row;
    private int number;

    public String getId() {
        return id;
    }

    public int getRow() {
        return row;
    }

    public int getNumber() {
        return number;
    }

    // Default constructor required for calls to Firebase's DataSnapshot.getValue
    public Seat() {}

    public Seat(String id, int row, int number) {
        this.id = id;
        this.row = row;
        this.number = number;
    }
}
