package app.com.almogrubi.idansasson.gettix.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import app.com.almogrubi.idansasson.gettix.R;
import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.dataservices.DataUtils;

/**
 * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
 * a cache of the child views for a forecast item. It's also a convenient place to set an
 * OnClickListener, since it has access to the adapter and the views.
 */
public class EventOccupancyViewHolder extends RecyclerView.ViewHolder {

    private TextView tvEventTitle;
    private TextView tvEventDateTime;
    private TextView tvEventHall;
    private TextView tvEventSoldTickets;
    private TextView tvEventOccupancy;

    public EventOccupancyViewHolder(View itemView) {
        super(itemView);
        this.tvEventTitle = itemView.findViewById(R.id.tv_event_item_title);
        this.tvEventDateTime = itemView.findViewById(R.id.tv_event_item_datetime);
        this.tvEventHall = itemView.findViewById(R.id.tv_event_item_hall);
        this.tvEventSoldTickets = itemView.findViewById(R.id.tv_event_item_sold_tickets);
        this.tvEventOccupancy = itemView.findViewById(R.id.tv_event_item_occupancy);
    }

    public void bindEvent(Event event) {
        tvEventTitle.setText(event.getTitle());
        tvEventDateTime.setText(DataUtils.convertToUiDateFormat(event.getDate()));
        tvEventHall.setText(String.format("%s, %s", event.getEventHall().getName(), event.getCity()));

        int eventTotalTicketsNum = event.isMarkedSeats()
                ? event.getEventHall().getRows() * event.getEventHall().getColumns()
                : event.getMaxCapacity();

        int eventSoldTicketsNum = eventTotalTicketsNum - event.getLeftTicketsNum();

        // Calculate occupancy in percentage
        int eventOccupancy = (eventSoldTicketsNum * 100) / eventTotalTicketsNum;

        tvEventSoldTickets.setText(String.format("%d כרטיסים נמכרו מתוך %d", eventSoldTicketsNum, eventTotalTicketsNum));
        tvEventOccupancy.setText(String.format("תפוסה:%d%%", eventOccupancy));
    }
}
