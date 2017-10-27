package app.com.almogrubi.idansasson.gettix.entities;

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
    private String creatingProducerId;

    // Default constructor required for calls to Firebase's DataSnapshot.getValue
    public Hall() {}

    public Hall(String id, String name, String address, String city, String officialWebsite,
                    ArrayList<Seat> seats, String creatingProducerId) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.city = city;
        this.officialWebsite = officialWebsite;

        for (Seat seat : seats)
            this.seats.add(seat);

        this.creatingProducerId = creatingProducerId;
    }

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

    public String getCreatingProducerId() {
        return this.creatingProducerId;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
