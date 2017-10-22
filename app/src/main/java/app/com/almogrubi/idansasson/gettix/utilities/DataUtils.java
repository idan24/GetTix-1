package app.com.almogrubi.idansasson.gettix.utilities;

/**
 * Created by idans on 21/10/2017.
 */

public class DataUtils {

    public enum Category {
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

}
