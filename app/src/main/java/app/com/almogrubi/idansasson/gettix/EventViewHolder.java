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
import app.com.almogrubi.idansasson.gettix.utilities.DataUtils;
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
    private ImageView ivEventSoldout;

    public View layout;

    public EventViewHolder(View itemView) {
        super(itemView);
        layout = itemView;
        this.tvEventTitle = itemView.findViewById(R.id.tv_event_item_title);
        this.tvEventDateTime = itemView.findViewById(R.id.tv_event_item_datetime);
        this.tvEventHall = itemView.findViewById(R.id.tv_event_item_hall);
        this.ivEventCategory = itemView.findViewById(R.id.iv_event_item_category);
        this.ivEventPoster = itemView.findViewById(R.id.iv_event_item_poster);
        this.ivEventSoldout = itemView.findViewById(R.id.iv_event_item_soldout);
    }

    public void bindEvent(Event event) {

        tvEventTitle.setText(event.getTitle());

        tvEventDateTime.setText(String.format("%s בשעה %s",
                DataUtils.convertToUiDateFormat(event.getDate()),
                event.getHour()));

        tvEventHall.setText(String.format("%s, %s", event.getEventHall().getName(), event.getCity()));

        ivEventCategory.setBackgroundResource(Utils.lookupImageByCategory(event.getCategoryAsEnum()));

        Uri photoUri = Uri.parse(event.getPosterUri());
        Glide.with(ivEventPoster.getContext())
                .load(photoUri)
                .into(ivEventPoster);

        if (event.isSoldOut()) {
            ivEventSoldout.setVisibility(View.VISIBLE);
            ivEventPoster.setAlpha((float) 0.7);
        } else {
            ivEventSoldout.setVisibility(View.INVISIBLE);
        }
    }
}