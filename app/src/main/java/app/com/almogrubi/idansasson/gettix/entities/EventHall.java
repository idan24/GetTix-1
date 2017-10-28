package app.com.almogrubi.idansasson.gettix.entities;

import java.util.ArrayList;

/**
 * Created by idans on 28/10/2017.
 */

public class EventHall {

    private String name;
    private String city;
    private ArrayList<Seat> seats;

    // Default constructor required for calls to Firebase's DataSnapshot.getValue
    public EventHall() {}

    public EventHall(String name, String city, ArrayList<Seat> seats) {
        this.name = name;
        this.city = city;
        this.seats = seats;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public ArrayList<Seat> getSeats() {
        ArrayList<Seat> returnedSeats = new ArrayList<>();
        for (Seat seat : this.seats)
            returnedSeats.add(seat);
        return returnedSeats;
    }
}
