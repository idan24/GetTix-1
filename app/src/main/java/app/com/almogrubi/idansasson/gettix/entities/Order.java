package app.com.almogrubi.idansasson.gettix.entities;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import app.com.almogrubi.idansasson.gettix.utilities.DataUtils;

/**
 * Created by idans on 21/10/2017.
 */

// Indexed in database by Event uid
public class Order implements Serializable {

    private String uid;
    private Customer customer;
    private String creditCardToken;
    private int ticketsNum;
    private boolean isCouponUsed;
    private int totalPrice;
    private DataUtils.OrderStatus status;
    private String confirmationNumber;
    private HashMap<String, Object> creationDate;

    // Default constructor required for calls to Firebase's DataSnapshot.getValue
    public Order() {}

    public Order(String uid, int ticketsNum, boolean isCouponUsed, int totalPrice, DataUtils.OrderStatus status) {
        this.uid = uid;
        this.ticketsNum = ticketsNum;
        this.isCouponUsed = isCouponUsed;
        this.totalPrice = totalPrice;
        this.status = status;

        // Creation date will always be set to ServerValue.TIMESTAMP
        HashMap<String, Object> nowTimestamp = new HashMap<>();
        nowTimestamp.put("timestamp", ServerValue.TIMESTAMP);
        this.creationDate = nowTimestamp;
    }

    public Order(String uid, Customer customer, String creditCardToken, int ticketsNum, boolean isCouponUsed,
                 int totalPrice, String confirmationNumber, DataUtils.OrderStatus status) {
        this.uid = uid;
        this.customer = customer;
        this.creditCardToken = creditCardToken;
        this.ticketsNum = ticketsNum;
        this.isCouponUsed = isCouponUsed;
        this.totalPrice = totalPrice;
        this.confirmationNumber = confirmationNumber;
        this.status = status;

        // Creation date will always be set to ServerValue.TIMESTAMP
        HashMap<String, Object> nowTimestamp = new HashMap<>();
        nowTimestamp.put("timestamp", ServerValue.TIMESTAMP);
        this.creationDate = nowTimestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getCreditCardToken() {
        return creditCardToken;
    }

    public void setCreditCardToken(String creditCardToken) {
        this.creditCardToken = creditCardToken;
    }

    public int getTicketsNum() {
        return ticketsNum;
    }

    public void setTicketsNum(int ticketsNum) {
        this.ticketsNum = ticketsNum;
    }

    public boolean isCouponUsed() {
        return this.isCouponUsed;
    }

    public void setCouponUsed(boolean couponUsed) {
        this.isCouponUsed = couponUsed;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getConfirmationNumber() {
        return confirmationNumber;
    }

    public void setConfirmationNumber(String confirmationNumber) {
        this.confirmationNumber = confirmationNumber;
    }

    public HashMap<String, Object> getCreationDate() { return creationDate; }

    // Use the method to get the long value from the creation date object
    @Exclude
    public long getCreationDateLong() {
        return (long)creationDate.get("timestamp");
    }

    @Exclude
    public DataUtils.OrderStatus getStatusAsEnum() {
        return this.status;
    }

    public void setStatusAsEnum(DataUtils.OrderStatus status) { this.status = status; }

    // these methods are just a Firebase 9.0.0 hack to handle the enum
    public String getStatus(){
        if (this.status == null){
            return null;
        } else {
            return this.status.name();
        }
    }

    public void setStatus(String statusString){
        if (statusString == null){
            this.status = null;
        } else {
            this.status = DataUtils.OrderStatus.valueOf(statusString);
        }
    }
}
