package app.com.almogrubi.idansasson.gettix.entities;

import java.util.ArrayList;

/**
 * Created by idans on 28/10/2017.
 */

public class EventHall {

    private String name;
    private String city;
    private int rows;
    private int columns;
    private ArrayList<EventSeat> eventSeats;

    // Default constructor required for calls to Firebase's DataSnapshot.getValue
    public EventHall() {}

    public EventHall(String name, String city, int rows, int columns, ArrayList<EventSeat> eventSeats) {
        this.name = name;
        this.city = city;
        this.rows = rows;
        this.columns = columns;
        this.eventSeats = eventSeats;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public int getRows() { return rows; }

    public int getColumns() { return columns; }

    public ArrayList<EventSeat> getEventSeats() {
        ArrayList<EventSeat> returnedEventSeats = new ArrayList<>();
        for (EventSeat eventSeat : this.eventSeats)
            returnedEventSeats.add(eventSeat);
        return returnedEventSeats;
    }
}
