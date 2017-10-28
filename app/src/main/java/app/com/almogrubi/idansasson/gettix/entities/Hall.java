package app.com.almogrubi.idansasson.gettix.entities;

import org.joda.time.DateTime;

import java.util.ArrayList;

/**
 * Created by idans on 21/10/2017.
 */

public class Hall {

    private String id;
    private String name;
    private String address;
    private String city;
    private String officialWebsite;
    private ArrayList<Seat> seats;
    private ArrayList<DateTime> eventDateTimes;

    // Default constructor required for calls to Firebase's DataSnapshot.getValue
    public Hall() {}

    public String getId() {
        return this.id;
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

    public ArrayList<Seat> getSeats() {
        ArrayList<Seat> returnedSeats = new ArrayList<>();
        for (Seat seat : this.seats)
            returnedSeats.add(seat);
        return returnedSeats;
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
    public String toString() {
        return this.name;
    }
}
