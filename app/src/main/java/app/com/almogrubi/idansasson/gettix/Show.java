package app.com.almogrubi.idansasson.gettix;

import java.io.Serializable;

/**
 * Created by Almog.Rubinstein on 10/11/2017.
 */

public class Show implements Serializable {

    private String title;
    private String location;
    private String type;
    private String descreption;
    private String date;
    private String time;
    private int price;
    private int ID;
    private String drawNmae;

    public Show(String title, String location, String type, String descreption, String date, String time, int price, int id, String drawNmae)
    {
        this.title = title;
        this.location = location;
        this.type = type;
        this.descreption = descreption;
        this.date = date;
        this.time = time;
        this.price = price;
        this.ID = id;
        this.drawNmae = drawNmae;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescreption() {
        return descreption;
    }

    public void setDescreption(String descreption) {
        this.descreption = descreption;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }


    public String getDrawNmae() {
        return drawNmae;
    }

    public void setDrawNmae(String drawNmae) {
        this.drawNmae = drawNmae;
    }

    @Override
    public String toString() {
        return "Show{" +
                "title='" + title + '\'' +
                ", location='" + location + '\'' +
                ", type='" + type + '\'' +
                ", descreption='" + descreption + '\'' +
                ", date=" + date +
                ", time=" + time +
                ", price=" + price +
                ", ID=" + ID +
                '}';
    }
}
