package app.com.almogrubi.idansasson.gettix.entities;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by idans on 21/10/2017.
 */

public class Hall implements Serializable {

    private String uid;
    private String name;
    private String address;
    private String city;
    private String officialWebsite;
    private int rows;
    private int columns;

    // Default constructor required for calls to Firebase's DataSnapshot.getValue
    public Hall() {}

    public Hall(String uid, String name, String address, String city, String officialWebsite, int rows, int columns) {
        this.uid = uid;
        this.name = name;
        this.address = address;
        this.city = city;
        this.officialWebsite = officialWebsite;
        this.rows = rows;
        this.columns = columns;
    }

    public String getUid() {
        return this.uid;
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

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setOfficialWebsite(String officialWebsite) {
        this.officialWebsite = officialWebsite;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    @Override
    public String toString() { return this.name; }
}
