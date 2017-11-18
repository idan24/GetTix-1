package app.com.almogrubi.idansasson.gettix.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by idans on 28/10/2017.
 */

public class EventHall implements Serializable {

    private String uid;
    private String name;
    private int rows;
    private int columns;

    // Default constructor required for calls to Firebase's DataSnapshot.getValue
    public EventHall() {}

    public EventHall(String uid, String name, int rows, int columns) {
        this.uid = uid;
        this.name = name;
        this.rows = rows;
        this.columns = columns;
    }

    public String getUid() { return uid; }

    public String getName() {
        return name;
    }

    public int getRows() { return rows; }

    public int getColumns() { return columns; }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }
}
