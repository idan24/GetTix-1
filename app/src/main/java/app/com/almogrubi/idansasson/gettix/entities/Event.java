package app.com.almogrubi.idansasson.gettix.entities;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

import app.com.almogrubi.idansasson.gettix.utilities.DataUtils;

/**
 * Created by idans on 21/10/2017.
 */

public class Event implements Serializable {

    private String uid;
    private String title;
    private DataUtils.Category category;
    private EventHall eventHall;
    private String city;
    private String date;
    private String hour;
    private int duration;
    private String description;
    private String performer;
    private int price;
    private int couponCode;
    private int discountedPrice;
    private String posterUri;
    private boolean isMarkedSeats;
    private int maxCapacity;
    private int leftTicketsNum;
    private boolean isSoldOut;
    private String producerId;

    // Default constructor required for calls to Firebase's DataSnapshot.getValue
    public Event() {}

    public Event(String uid, String title, DataUtils.Category category, EventHall eventHall, String city,
                 String date, String hour, int duration, int price, String posterUri, boolean isMarkedSeats,
                 int maxCapacity) {
        this.uid = uid;
        this.title = title;
        this.category = category;
        this.eventHall = eventHall;
        this.city = city;
        this.date = date;
        this.hour = hour;
        this.duration = duration;
        this.price = price;
        this.posterUri = posterUri;
        this.isMarkedSeats = isMarkedSeats;
        this.maxCapacity = maxCapacity;

        // Calculate number of tickets left from its details
        this.leftTicketsNum = isMarkedSeats ?
                eventHall.getRows() * eventHall.getColumns() :
                maxCapacity;
        // Newly created event should never be already sold out
        this.isSoldOut = false;
    }

    public Event(String uid, String title, DataUtils.Category category, EventHall eventHall, String city,
                 String date, String hour, int duration, String description, String performer, int price,
                 int couponCode, int discountedPrice, String posterUri, boolean isMarkedSeats, int maxCapacity,
                 String producerId) {
        this.uid = uid;
        this.title = title;
        this.category = category;
        this.eventHall = eventHall;
        this.city = city;
        this.date = date;
        this.hour = hour;
        this.duration = duration;
        this.description = description;
        this.performer = performer;
        this.price = price;
        this.couponCode = couponCode;
        this.discountedPrice = discountedPrice;
        this.posterUri = posterUri;
        this.isMarkedSeats = isMarkedSeats;
        this.maxCapacity = maxCapacity;
        this.producerId = producerId;

        // Calculate number of tickets left from its details
        this.leftTicketsNum = isMarkedSeats ?
                eventHall.getRows() * eventHall.getColumns() :
                maxCapacity;
        // Newly created event should never be already sold out
        this.isSoldOut = false;
    }

    public String getUid() {
        return this.uid;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    @Exclude
    public DataUtils.Category getCategoryAsEnum() {
        return this.category;
    }

    // these methods are just a Firebase 9.0.0 hack to handle the enum
    public String getCategory(){
        if (this.category == null){
            return null;
        } else {
            return this.category.name();
        }
    }

    public void setCategory(String categoryString){
        if (categoryString == null){
            this.category = null;
        } else {
            this.category = DataUtils.Category.valueOf(categoryString);
        }
    }

    public EventHall getEventHall() {
        return this.eventHall;
    }

    public String getCity() { return this.city; }

    public String getPerformer() {
        return this.performer;
    }

    public String getDate() {
        return this.date;
    }

    public String getHour() {
        return this.hour;
    }

    public int getDuration() {
        return this.duration;
    }

    public String getPosterUri() {
        return this.posterUri;
    }

    public int getPrice() {
        return this.price;
    }

    public int getCouponCode() {
        return this.couponCode;
    }

    public int getDiscountedPrice() {
        return this.discountedPrice;
    }

    public boolean isMarkedSeats() { return this.isMarkedSeats; }

    public int getMaxCapacity() {
        return this.maxCapacity;
    }

    public int getLeftTicketsNum() { return leftTicketsNum; }

    public String getProducerId() {
        return this.producerId;
    }

    public boolean isSoldOut() {
        return this.isSoldOut;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setEventHall(EventHall eventHall) {
        this.eventHall = eventHall;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPerformer(String performer) {
        this.performer = performer;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setCouponCode(int couponCode) {
        this.couponCode = couponCode;
    }

    public void setDiscountedPrice(int discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public void setPosterUri(String posterUri) {
        this.posterUri = posterUri;
    }

    public void setMarkedSeats(boolean markedSeats) {
        this.isMarkedSeats = markedSeats;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public void setLeftTicketsNum(int leftTicketsNum) { this.leftTicketsNum = leftTicketsNum; }

    public void setSoldOut(boolean soldOut) {
        this.isSoldOut = soldOut;
    }

    public void setProducerId(String producerId) {
        this.producerId = producerId;
    }

    @Override
    public String toString() {
        return String.format("%s בתאריך %s שעה %s, ב%s",
                this.title,
                DataUtils.convertToUiDateFormat(this.date),
                this.hour,
                this.eventHall.getName());
    }
}
