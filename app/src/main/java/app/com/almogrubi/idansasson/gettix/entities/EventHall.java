package app.com.almogrubi.idansasson.gettix.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by idans on 28/10/2017.
 */

public class EventHall {

    private String uid;
    private String name;
    private String city;
    private int rows;
    private int columns;
    public Map<String, EventSeat> eventSeats = new HashMap<>();

    // Default constructor required for calls to Firebase's DataSnapshot.getValue
    public EventHall() {}

    public EventHall(String uid, String name, String city, int rows, int columns, Map<String, EventSeat> eventSeats) {
        this.uid = uid;
        this.name = name;
        this.city = city;
        this.rows = rows;
        this.columns = columns;
        this.eventSeats = eventSeats;
    }

    public String getUid() { return uid; }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public int getRows() { return rows; }

    public int getColumns() { return columns; }

    public Map<String, EventSeat> getEventSeats() {
        return this.eventSeats;
    }
}
