package app.com.almogrubi.idansasson.gettix.entities;

/**
 * Created by idans on 21/10/2017.
 */

public class Seat {

    private String id;
    private int row;
    private int number;
    private boolean isTaken;

    public boolean isTaken() { return isTaken; }

    public void setTaken(boolean taken) { isTaken = taken; }

    // Default constructor required for calls to Firebase's DataSnapshot.getValue
    public Seat() {}

    public Seat(String id, int row, int number) {
        this.id = id;
        this.row = row;
        this.number = number;
        this.isTaken = false;
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
