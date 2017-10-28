package app.com.almogrubi.idansasson.gettix.entities;

import org.joda.time.DateTime;

/**
 * Created by idans on 21/10/2017.
 */

public class Seat {

    private String id;
    private int row;
    private int number;
    private boolean isTaken;
    private boolean isOccupied;
    private DateTime occupiedTimestamp;

    // Default constructor required for calls to Firebase's DataSnapshot.getValue
    public Seat() {}

    public Seat(String id, int row, int number) {
        this.id = id;
        this.row = row;
        this.number = number;
        this.isTaken = false;
        this.isOccupied = false;
        this.occupiedTimestamp = null;
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

    public boolean isTaken() { return isTaken; }

    public void setTaken(boolean taken) { isTaken = taken; }

    public boolean isOccupied() { return isOccupied; }

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }

    public DateTime getOccupiedTimestamp() { return occupiedTimestamp; }

    public void setOccupiedTimestamp(DateTime occupiedTimestamp) {
        this.occupiedTimestamp = occupiedTimestamp;
    }
}
