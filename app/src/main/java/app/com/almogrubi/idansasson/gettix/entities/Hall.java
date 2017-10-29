package app.com.almogrubi.idansasson.gettix.entities;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by idans on 21/10/2017.
 */

public class Hall {

    private String uid;
    private String name;
    private String address;
    private String city;
    private String officialWebsite;
    private int rows;
    private int columns;
    public Map<String, Seat> seats = new HashMap<>();
    //private ArrayList<Seat> seats;
    private ArrayList<DateTime> eventDateTimes;

    // Default constructor required for calls to Firebase's DataSnapshot.getValue
    public Hall() {}

    public Hall(String uid, String name, String address, String city, String officialWebsite, int rows, int columns, Map<String, Seat> seats) {
        this.uid = uid;
        this.name = name;
        this.address = address;
        this.city = city;
        this.officialWebsite = officialWebsite;
        this.rows = rows;
        this.columns = columns;
        this.seats = seats;
    }

    public String getUid() {
        return this.uid;
    }

    public String getName() {
        return this.name;
    }

    public String getAddress() {
        return this.address;
    }

    public String getCity() {
        return this.city;
    }

    public String getOfficialWebsite() {
        return this.officialWebsite;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public Map<String, Seat> getSeats() {
        return this.seats;
    }

    // This method is used to create an Event's EventSeat objects from its Hall's Seat objects
    public Map<String, EventSeat> makeEventSeats() {
        Map<String, EventSeat> returnedEventSeats = new HashMap<>();
        for (Seat seat : this.seats.values()) {
            returnedEventSeats.put(seat.getUid(), new EventSeat(seat.getUid(), seat.getRow(), seat.getNumber()));
        }
        return returnedEventSeats;
    }

    // We override equals() to that we can compare Halls quickly with no use of lambda expressions
    // TODO: remove if unused
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Hall) {
            Hall hall = (Hall) obj;
            return this.name.equals(hall.getName());
        }

        return false;
    }

    @Override
    public String toString() { return this.name; }
}
