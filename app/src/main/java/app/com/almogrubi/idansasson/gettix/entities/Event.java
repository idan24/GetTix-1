package app.com.almogrubi.idansasson.gettix.entities;

import com.google.firebase.database.Exclude;

import org.joda.time.DateTime;

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
    private Long dateTime;
    private int duration;
    private String description;
    private String performer;
    private int price;
    private String posterUri;
    private boolean hasMarkedSeats;
    private int maxCapacity;
    private boolean isSoldOut;
    private String producerId;

    // Default constructor required for calls to Firebase's DataSnapshot.getValue
    public Event() {}

    public Event(String uid, String title, DataUtils.Category category, EventHall eventHall, Long dateTime,
                 int duration, String description, String performer, int price, String posterUri,
                 boolean hasMarkedSeats, int maxCapacity, String producerId) {
        this.uid = uid;
        this.title = title;
        this.category = category;
        this.eventHall = eventHall;
        this.dateTime = dateTime;
        this.duration = duration;
        this.description = description;
        this.performer = performer;
        this.price = price;
        this.posterUri = posterUri;
        this.hasMarkedSeats = hasMarkedSeats;
        this.maxCapacity = maxCapacity;
        this.producerId = producerId;

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

    public String getPerformer() {
        return this.performer;
    }

    public Long getDateTime() {
        return this.dateTime;
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

    public boolean hasMarkedSeats() {
        return this.hasMarkedSeats;
    }

    public int getMaxCapacity() {
        return this.maxCapacity;
    }

    public String getProducerId() {
        return this.producerId;
    }

    public boolean isSoldOut() {
        return this.isSoldOut;
    }
}
