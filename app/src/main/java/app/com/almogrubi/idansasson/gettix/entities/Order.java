package app.com.almogrubi.idansasson.gettix.entities;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import app.com.almogrubi.idansasson.gettix.utilities.DataUtils;

/**
 * Created by idans on 21/10/2017.
 */

public class Order implements Serializable {

    private String uid;
    private String eventId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String customerCreditCard;
    private boolean isCouponUsed;
    private int totalPrice;
    private DataUtils.OrderStatus status;
    private int ticketsNum;
    private Long creationDate;

    // Default constructor required for calls to Firebase's DataSnapshot.getValue
    public Order() {
        this.ticketsNum= 0;
    }

    public Order(String uid, Event event, String customerName, String customerPhone, String customerEmail,
                 boolean isCouponUsed, int totalPrice, String customerCreditCard) {
        this.uid = uid;
        this.eventId = eventId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.isCouponUsed = isCouponUsed;
        this.totalPrice = totalPrice;
        this.customerCreditCard = customerCreditCard;
        this.status = status;

        this.creationDate = Calendar.getInstance().getTimeInMillis();
    }


    public int getTicketsNum() {
        return ticketsNum;
    }

    public void setTicketsNum(int ticketsNum) {
        this.ticketsNum = ticketsNum;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public String getUid() {
        return this.uid;
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

    public boolean isCouponUsed() { return this.isCouponUsed; }

    public int getTotalPrice() {
        return this.totalPrice;
    }

    public String getCustomerCreditCard() {
        return this.customerCreditCard;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public void setCouponUsed(boolean isCouponUsed) { this.isCouponUsed = isCouponUsed; }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setCustomerCreditCard(String customerCreditCard) {
        this.customerCreditCard = customerCreditCard;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    public void setStatus(DataUtils.OrderStatus status) {
        this.status = status;
    }

    @Exclude
    public DataUtils.OrderStatus getStatusAsEnum() {
        return this.status;
    }

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
