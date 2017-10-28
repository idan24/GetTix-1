package app.com.almogrubi.idansasson.gettix.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import app.com.almogrubi.idansasson.gettix.utilities.DataUtils;

/**
 * Created by idans on 21/10/2017.
 */

public class Order implements Serializable {

    private String id;
    private String eventId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private int totalPrice;
    private String creditCard;
    private ArrayList<EventSeat> eventSeats;
    private DataUtils.OrderStatus status;
    private int ticketNum;
    private Date creationDate;

    // Default constructor required for calls to Firebase's DataSnapshot.getValue
    public Order() {
        this.ticketNum= 0;
    }

    public Order(String id, Event event, String customerName, String customerPhone, String customerEmail,
                    int totalPrice, String creditCard, ArrayList<EventSeat> eventSeats) {
        this.id = id;
        this.eventId = eventId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.totalPrice = totalPrice;
        this.creditCard = creditCard;

        for (EventSeat eventSeat : eventSeats)
            this.eventSeats.add(eventSeat);

        this.status = status;

        this.creationDate = Calendar.getInstance().getTime();
    }


    public int getTicketNum() {
        return ticketNum;
    }

    public void setTicketNum(int ticketNum) {
        this.ticketNum = ticketNum;
    }

    public Date getCreationDate() {
        return creationDate;
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

    public ArrayList<EventSeat> getEventSeats() {
        ArrayList<EventSeat> returnedEventSeats = new ArrayList<>();
        for (EventSeat eventSeat : this.eventSeats)
            returnedEventSeats.add(eventSeat);
        return returnedEventSeats;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setCreditCard(String creditCard) {
        this.creditCard = creditCard;
    }

    public void setEventSeats(ArrayList<EventSeat> eventSeats) {
        this.eventSeats = eventSeats;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public DataUtils.OrderStatus getStatus() {
        return status;
    }

    public void setStatus(DataUtils.OrderStatus status) {
        this.status = status;
    }
}
