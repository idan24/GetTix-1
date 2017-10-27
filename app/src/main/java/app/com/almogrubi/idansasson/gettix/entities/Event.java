package app.com.almogrubi.idansasson.gettix.entities;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Date;

import app.com.almogrubi.idansasson.gettix.utilities.DataUtils;

/**
 * Created by idans on 21/10/2017.
 */

public class Event implements Serializable {

    private String id;
    private String title;
    private DataUtils.Category category;
    private Hall hall;
    private String city;
    private DateTime dateTime;
    private int duration;
    private String description;
    private String performer;
    private int price;
    private String image;
    private boolean hasMarkedSeats;
    private int maxCapacity;
    private boolean isSoldOut;
    private String producerId;

    // Default constructor required for calls to Firebase's DataSnapshot.getValue
    public Event() {}

    public Event(String id, String title, String description, DataUtils.Category category, Hall hall,
                    String city, String performer, DateTime dateTime, int duration, String image, int price,
                        boolean hasMarkedSeats, int maxCapacity, String producerId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.hall = hall;
        this.city = city;
        this.performer = performer;
        this.dateTime = dateTime;
        this.duration = duration;
        this.image = image;
        this.price = price;
        this.hasMarkedSeats = hasMarkedSeats;
        this.maxCapacity = maxCapacity;
        this.producerId = producerId;

        this.isSoldOut = false;
    }

    public String getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public DataUtils.Category getCategory() {
        return this.category;
    }

    public Hall getHall() {
        return this.hall;
    }

    public String getCity() {
        return this.city;
    }

    public String getPerformer() {
        return this.performer;
    }

    public DateTime getDateTime() {
        return this.dateTime;
    }

    public int getDuration() {
        return this.duration;
    }

    public String getImage() {
        return this.image;
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
