package app.com.almogrubi.idansasson.gettix;

import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.utilities.DataUtils;
import app.com.almogrubi.idansasson.gettix.utilities.Utils;

/**
 * Created by almogrubi on 10/14/17.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.EventViewHolder>
{
    // The context we use for utility methods, app resources and layout inflaters
    private final Context context;

    private List<Event> eventList;
    private ArrayList<Event> eventArrayList;

    public RecyclerViewAdapter(Context context, List<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
        this.eventArrayList = new ArrayList<>(eventList);
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (like ours does) you
     *                  can use this viewType integer to provide a different layout.
     * @return A new ForecastAdapterViewHolder that holds the View for each list item
     */
    @Override
    public RecyclerViewAdapter.EventViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
        view.setFocusable(true);
        return new EventViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the event
     * details for this particular position, using the "position" argument.
     *
     * @param holder The ViewHolder which should be updated to represent the
     *                        contents of the item at the given position in the data set.
     * @param position        The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.bindEvent(event);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public void filter(Date date, String hall, DataUtils.Category category, String city, String keyword) {

        keyword = keyword.toLowerCase(Locale.getDefault());
        eventList.clear();

        if (date == null && hall.isEmpty() && category == null && city.isEmpty() && keyword.isEmpty()) {
            eventList.addAll(eventArrayList);
        }
        else {
            for (Event event : eventArrayList) {
                if ((date != null) && (!date.equals(event.getDateTime())))
                    continue;
                else if ((!hall.isEmpty()) && (!hall.equals(event.getHallId())))
                    continue;
                else if ((category != null) && (category != event.getCategory()))
                    continue;
                else if ((!city.isEmpty()) && (!event.getCity().contains(city)))
                    continue;
                else if ((!keyword.isEmpty()) &&
                         (!event.getTitle().contains(keyword)) &&
                         (!event.getDescription().contains(keyword)) &&
                         (!event.getPerformer().contains(keyword)))
                    continue;

                eventList.add(event);
            }
        }

        notifyDataSetChanged();
    }

    private void lookupHall(String hallName) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference hallsDatabaseReference = firebaseDatabase.getReference().child("halls");
        Query query = hallsDatabaseReference.orderByChild("name").equalTo(hallName).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    dataSnapshot.getChildren().iterator().next();
                    // dataSnapshot is the "hall" node with name [hallName]
                    for (DataSnapshot hall : dataSnapshot.getChildren()) {
                        // do something with the individual "issues"
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

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
            this.titleView = (TextView) itemView.findViewById(R.id.tv_event_item_title);
            this.dateTimeView = (TextView) itemView.findViewById(R.id.tv_event_item_datetime);
            this.hallView = (TextView) itemView.findViewById(R.id.tv_event_item_hall);
            this.categoryView = (ImageView) itemView.findViewById(R.id.iv_event_item_category);
            this.imageView = (ImageView) itemView.findViewById(R.id.iv_event_item_image);
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

            hallView.setText(String.format("%s, %s", event.getHallId(), event.getCity()));

            categoryView.setBackgroundResource(Utils.lookupImageByCategory(event.getCategory()));

            imageView.setBackgroundResource(R.drawable.miserables);
//
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d("I", "was clicked");
//                Context contextO = v.getContext();
//                Intent intent = new Intent(contextO, DetailActivity.class);
//                intent.putExtra("showObject", show);
//                contextO.startActivity(intent);
//            }
//        });
        }
    }
}
