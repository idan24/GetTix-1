package app.com.almogrubi.idansasson.gettix.entities;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by idans on 21/10/2017.
 */

public class EventSeat implements Serializable {

    private String uid;
    private int row;
    private int number;
    private boolean isTaken;
    private boolean isOccupied;
    private HashMap<String, Object> occupiedTimestamp;

    // Default constructor required for calls to Firebase's DataSnapshot.getValue
    public EventSeat() {}

    public EventSeat(String uid, int row, int number) {
        this.uid = uid;
        this.row = row;
        this.number = number;
        this.isTaken = false;
        this.isOccupied = false;
        this.occupiedTimestamp = null;
    }

    public String getUid() {
        return this.uid;
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

    public void updateOccupiedTimestamp() {
        // Occupied timestamp will always be set to ServerValue.TIMESTAMP
        HashMap<String, Object> nowTimestamp = new HashMap<>();
        nowTimestamp.put("timestamp", ServerValue.TIMESTAMP);
        this.occupiedTimestamp = nowTimestamp;
    }

    public HashMap<String, Object> getOccupiedTimestamp() { return occupiedTimestamp; }

    // Use the method to get the long value from the occupied timestamp object
    @Exclude
    public long getOccupiedTimestampLong() {
        return (long)occupiedTimestamp.get("timestamp");
    }
}
