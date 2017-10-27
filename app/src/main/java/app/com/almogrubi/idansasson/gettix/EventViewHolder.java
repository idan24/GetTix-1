package app.com.almogrubi.idansasson.gettix;

/**
 * Created by idans on 25/10/2017.
 */

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.DateTime;

import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.utilities.Utils;

/**
 * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
 * a cache of the child views for a forecast item. It's also a convenient place to set an
 * OnClickListener, since it has access to the adapter and the views.
 */
public class EventViewHolder extends RecyclerView.ViewHolder {

    private TextView titleView;
    private TextView dateTimeView;
    private TextView hallView;
    private ImageView categoryView;
    private ImageView imageView;

    public View layout;

    public EventViewHolder(View itemView) {
        super(itemView);
        layout = itemView;
        this.titleView = itemView.findViewById(R.id.tv_event_item_title);
        this.dateTimeView = itemView.findViewById(R.id.tv_event_item_datetime);
        this.hallView = itemView.findViewById(R.id.tv_event_item_hall);
        this.categoryView = itemView.findViewById(R.id.iv_event_item_category);
        this.imageView = itemView.findViewById(R.id.iv_event_item_image);
    }

    public void bindEvent(Event event) {

        titleView.setText(event.getTitle());

        DateTime dateTime = new DateTime(event.getDateTime());
        dateTimeView.setText(String.format("%d/%d/%d בשעה %d:%d",
                dateTime.getDayOfMonth(),
                dateTime.getMonthOfYear(),
                dateTime.getYearOfCentury(),
                dateTime.getHourOfDay(),
                dateTime.getMinuteOfHour()));

        hallView.setText(String.format("%s, %s", event.getHall().getName(), event.getCity()));

        categoryView.setBackgroundResource(Utils.lookupImageByCategory(event.getCategory()));

        imageView.setBackgroundResource(R.drawable.miserables);
    }
}