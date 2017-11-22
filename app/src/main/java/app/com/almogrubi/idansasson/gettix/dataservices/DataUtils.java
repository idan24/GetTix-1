package app.com.almogrubi.idansasson.gettix.dataservices;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataUtils {

    // This enum represents all event categories supported by the app
    public enum Category {
        ALL("הכל"),
        MUSIC("מוזיקה"),
        THEATER("תיאטרון"),
        DANCE("מחול"),
        CHILDREN("ילדים"),
        COMEDY("סטנד-אפ"),
        LECTURES("הרצאות"),
        SPORTS("ספורט");

        private String friendlyName;

        private Category(String friendlyName){
            this.friendlyName = friendlyName;
        }

        @Override public String toString(){
            return friendlyName;
        }
    };

    // This enum represents all possible status for an order
    public enum OrderStatus {
        IN_PROGRESS("בתהליך"),
        FINAL("סופי"),
        CANCELLED("בוטל");

        private String friendlyName;

        private OrderStatus(String friendlyName){
            this.friendlyName = friendlyName;
        }

        @Override public String toString(){
            return friendlyName;
        }
    };

    // This enum represents all ways events are indexed in the database, except from by producer
    public enum EventIndexKey {
        ALL,
        CATEGORY,
        DATE,
        HALL,
        CITY,
        CATEGORY_DATE,
        CATEGORY_HALL,
        CATEGORY_CITY
    }

    // We use these formats to manipulate the appearance of dates
    public static SimpleDateFormat DB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat UI_DATE_FORMAT = new SimpleDateFormat("dd/MM/yy");
    public static SimpleDateFormat HOUR_FORMAT = new SimpleDateFormat("HH:mm");

    public static String createDbStringFromDate(Date date) {
        DB_DATE_FORMAT.setLenient(false);
        return DB_DATE_FORMAT.format(date);
    }

    public static String convertToDbDateFormat(String uiDate) {
        DB_DATE_FORMAT.setLenient(false);
        return DB_DATE_FORMAT.format(getDateFromString(uiDate, UI_DATE_FORMAT));
    }

    public static String convertToUiDateFormat(String dbDate) {
        UI_DATE_FORMAT.setLenient(false);
        return UI_DATE_FORMAT.format(getDateFromString(dbDate, DB_DATE_FORMAT));
    }

    public static String convertToUiDateFormat(long timestamp) {
        UI_DATE_FORMAT.setLenient(false);
        return UI_DATE_FORMAT.format(new Date(timestamp));
    }

    public static int getYearFromDbDate(String dbDate) {
        return new DateTime(getDateFromString(dbDate, DB_DATE_FORMAT)).getYear();
    }

    public static int getMonthFromDbDate(String dbDate) {
        return new DateTime(getDateFromString(dbDate, DB_DATE_FORMAT)).getMonthOfYear();
    }

    public static int getDayFromDbDate(String dbDate) {
        return new DateTime(getDateFromString(dbDate, DB_DATE_FORMAT)).getDayOfMonth();
    }

    public static int getHourFromDbHour(String dbHour) {
        return new DateTime(getDateFromString(dbHour, HOUR_FORMAT)).getHourOfDay();
    }

    public static int getMinuteFromDbMinute(String dbMinute) {
        return new DateTime(getDateFromString(dbMinute, HOUR_FORMAT)).getMinuteOfHour();
    }

    private static Date getDateFromString(String dateString, SimpleDateFormat format) {
        format.setLenient(false);
        try {
            return format.parse(dateString);
        } catch (ParseException e) {
            return new Date();
        }
    }
}