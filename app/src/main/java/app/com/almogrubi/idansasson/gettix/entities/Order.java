package app.com.almogrubi.idansasson.gettix.entities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by idans on 21/10/2017.
 */

public class Order {

    private String id;
    private String eventId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private int totalPrice;
    private String creditCard;
    private ArrayList<Seat> seats;
    private String status;
    private Date creationDate;

    // Default constructor required for calls to Firebase's DataSnapshot.getValue
    public Order() {}

    public Order(String id, Event event, String customerName, String customerPhone, String customerEmail,
                    int totalPrice, String creditCard, ArrayList<Seat> seats, String status) {
        this.id = id;
        this.eventId = eventId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.totalPrice = totalPrice;
        this.creditCard = creditCard;

        for (Seat seat : seats)
            this.seats.add(seat);

        this.status = status;

        this.creationDate = Calendar.getInstance().getTime();
    }

    public String getId() {
        return this.id;
    }

    public String getEventId() {
        return this.eventId;
    }

    public String getCustomerName() {
        return this.customerName;
    }

    public String getCustomerPhone() {
        return this.customerPhone;
    }

    public String getCustomerEmail() {
        return this.customerEmail;
    }

    public int getTotalPrice() {
        return this.totalPrice;
    }

    public String getCreditCard() {
        return this.creditCard;
    }

    public ArrayList<Seat> getSeats() {
        ArrayList<Seat> returnedSeats = new ArrayList<>();
        for (Seat seat : this.seats)
            returnedSeats.add(seat);
        return returnedSeats;
    }

    public String getStatus() {
        return this.status;
    }

    public Date getCreationDate() {
        return this.creationDate;
    }
}
