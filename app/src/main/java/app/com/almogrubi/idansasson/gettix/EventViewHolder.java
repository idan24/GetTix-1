package app.com.almogrubi.idansasson.gettix;

/**
 * Created by idans on 25/10/2017.
 */

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.joda.time.DateTime;

import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.entities.EventHall;
import app.com.almogrubi.idansasson.gettix.entities.Hall;
import app.com.almogrubi.idansasson.gettix.utilities.Utils;

/**
 * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
 * a cache of the child views for a forecast item. It's also a convenient place to set an
 * OnClickListener, since it has access to the adapter and the views.
 */
public class EventViewHolder extends RecyclerView.ViewHolder {

    private TextView tvEventTitle;
    private TextView tvEventDateTime;
    private TextView tvEventHall;
    private ImageView ivEventCategory;
    private ImageView ivEventPoster;

    public View layout;

    public EventViewHolder(View itemView) {
        super(itemView);
        layout = itemView;
        this.tvEventTitle = itemView.findViewById(R.id.tv_event_item_title);
        this.tvEventDateTime = itemView.findViewById(R.id.tv_event_item_datetime);
        this.tvEventHall = itemView.findViewById(R.id.tv_event_item_hall);
        this.ivEventCategory = itemView.findViewById(R.id.iv_event_item_category);
        this.ivEventPoster = itemView.findViewById(R.id.iv_event_item_poster);
    }

    public void bindEvent(Event event) {

        tvEventTitle.setText(event.getTitle());

        DateTime dateTime = new DateTime(event.getDateTime());
        tvEventDateTime.setText(String.format("%d/%d/%d בשעה %d:%d",
                dateTime.getDayOfMonth(),
                dateTime.getMonthOfYear(),
                dateTime.getYearOfCentury(),
                dateTime.getHourOfDay(),
                dateTime.getMinuteOfHour()));

        EventHall eventHall = event.getEventHall();
        tvEventHall.setText(String.format("%s, %s", eventHall.getName(), eventHall.getCity()));

        ivEventCategory.setBackgroundResource(Utils.lookupImageByCategory(event.getCategoryAsEnum()));

        Uri photoUri = Uri.parse(event.getPosterUri());
        Glide.with(ivEventPoster.getContext())
                .load(photoUri)
                .into(ivEventPoster);
    }
}