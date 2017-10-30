package app.com.almogrubi.idansasson.gettix;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.utilities.DataUtils;

/**
 * Created by almogrubi on 10/14/17.
 */

public class MainActivity extends AppCompatActivity {

    private EditText etEventDate;
    private EditText etEventHall;
    private EditText etEventCity;
    private EditText etEventKeyword;
    private Spinner spEventCategory;
    private Button btSearchEvents;
    private RecyclerView eventsRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    private DatabaseReference firebaseDatabaseReference;
    private DatabaseReference eventsDatabaseReference;
    private FirebaseRecyclerAdapter<Event, EventViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize references to views
        etEventDate = findViewById(R.id.et_find_by_date);
        etEventHall = findViewById(R.id.et_find_by_hall);
        etEventCity = findViewById(R.id.et_find_by_city);
        etEventKeyword = findViewById(R.id.et_find_by_keyword);
        spEventCategory = findViewById(R.id.sp_find_by_category);
        btSearchEvents = findViewById(R.id.bt_search_events);
        eventsRecyclerView = findViewById(R.id.searched_events_recycler_view);

        mLinearLayoutManager = new LinearLayoutManager(this);
        eventsRecyclerView.setLayoutManager(mLinearLayoutManager);

        // New child entries
        firebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        SnapshotParser<Event> parser = new SnapshotParser<Event>() {
            @Override
            public Event parseSnapshot(DataSnapshot dataSnapshot) {
                Event event = dataSnapshot.getValue(Event.class);
                if (event != null) {
                    event.setUid(dataSnapshot.getKey());
                }
                return event;
            }
        };

        eventsDatabaseReference = firebaseDatabaseReference.child("events");
        FirebaseRecyclerOptions<Event> options =
                new FirebaseRecyclerOptions.Builder<Event>()
                        .setQuery(eventsDatabaseReference, parser)
                        .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Event, EventViewHolder>(options) {
            @Override
            public EventViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new EventViewHolder(inflater.inflate(R.layout.event_list_item, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(final EventViewHolder viewHolder,
                                            int position,
                                            final Event event) {
                viewHolder.bindEvent(event);
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Context context = v.getContext();
                        Intent detailActivityIntent = new Intent(context, DetailActivity.class);
                        detailActivityIntent.putExtra("eventObject", event);
                        context.startActivity(detailActivityIntent);
                    }
                });
            }
        };

        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {});

        eventsRecyclerView.setAdapter(firebaseRecyclerAdapter);

        //------------------------------------------------------------------------

        final Calendar calendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                etEventDate.setText(sdf.format(calendar.getTime()));
            }
        };
        etEventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(MainActivity.this, date,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });

        // Initializing an ArrayAdapter for the Spinner
        final ArrayAdapter<DataUtils.Category> spinnerArrayAdapter = new ArrayAdapter<DataUtils.Category>(
                this, R.layout.spinner_item, DataUtils.Category.values()) {};
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        spEventCategory.setAdapter(spinnerArrayAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's inflater's inflate method to inflate our menu layout to this menu */
        getMenuInflater().inflate(R.menu.menu_main, menu);

        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_management) {
            startActivity(new Intent(this, EventsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        firebaseRecyclerAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        firebaseRecyclerAdapter.startListening();
    }

    private void commentedCode() {
        //        eventsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot != null)
//                    showEventList();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });




        //            @Override
//            public boolean isEnabled(int position){
//                if(position == 0)
//                {
//                    // Disable the first item from Spinner
//                    // First item will be use for hint
//                    return false;
//                }
//                else
//                {
//                    return true;
//                }
//            }
//            @Override
//            public View getDropDownView(int position, View convertView,
//                                        ViewGroup parent) {
//                View view = super.getDropDownView(position, convertView, parent);
//                TextView tv = (TextView) view;
//
//                if(position == 0){
//                    // Set the hint text color gray
//                    tv.setTextColor(Color.GRAY);
//                }
//                else {
//                    tv.setTextColor(Color.BLACK);
//                }
//                return view;
//            }


        //        btSearchEvents.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                adapter.filter(
//                        Date.valueOf(etEventDate.getText().toString()),
//                        etEventHall.getText().toString(),
//                        (DataUtils.Category) spEventCategory.getSelectedItem(),
//                        etEventCity.getText().toString(),
//                        etEventKeyword.getText().toString());
//            }
//        });






        //List<Event> showList = new ArrayList<>();
        //Event startwars = new Event("1", "starwars","best movie ever", DataUtils.Category.THEATER, "1","Tel Aviv","",new Date(1),60,
        //        "startwars",30, true ,16,"" );
        //Event got = new Event("2", "game of thrones","2nd best movie ever", DataUtils.Category.THEATER, "2","Tel Aviv","",new Date(2),60,
        //        "got",30, false ,16,"" );
        //showList.add(startwars);
        //showList.add(got);


        //mAdapter = new RecyclerViewAdapter(this, showList);
        //recyclerView.setAdapter(mAdapter);






//        FirebaseRecyclerOptions<Event> options =
//                new FirebaseRecyclerOptions.Builder<Event>()
//                        .setQuery(eventsDatabaseReference, Event.class)
//                        .build();
//        mFirebaseAdapter = new FirebaseRecyclerAdapter<Event, EventViewHolder>(options) {
//            @Override
//            public EventViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
//                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
//                return new EventViewHolder(inflater.inflate(R.layout.event_list_item, viewGroup, false));
//            }
//
//            @Override
//            protected void onBindViewHolder(final EventViewHolder viewHolder,
//                                            int position,
//                                            Event event) {
//                viewHolder.bindEvent(event);
//            }
//        };
//
//        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
//            @Override
//            public void onItemRangeInserted(int positionStart, int itemCount) {
//                super.onItemRangeInserted(positionStart, itemCount);
//            }
//        });
//
//        eventsRecyclerView.setAdapter(mFirebaseAdapter);



//    public void filter(Date date, String hallName, DataUtils.Category category, String city, String keyword) {
//        keyword = keyword.toLowerCase(Locale.getDefault());
//        eventList.clear();
//
//        if (date == null && hallName.isEmpty() && category == null && city.isEmpty() && keyword.isEmpty()) {
//            eventList.addAll(eventArrayList);
//        }
//        else {
//            for (final Event event : eventArrayList) {
//                if ((date != null) && (!date.equals(event.getDateTime())))
//                    continue;
//                else if (!hallName.isEmpty()) {
//                    DatabaseReference hallsDatabaseReference = firebaseDatabase.getReference().child("halls");
//                    Query query = hallsDatabaseReference.orderByChild("name").equalTo(hallName).limitToFirst(1);
//                    query.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            if (dataSnapshot.exists()) {
//                                //Hall hall = (Hall)dataSnapshot.getValue();
//                                Hall hall = dataSnapshot.getChildren().iterator().next().getValue(Hall.class);
//                                if (event.getHallId().equals(hall.getUid()))
//                                    eventList.add(event);
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {}
//                    });
//                    continue;
//                }
//                else if ((category != null) && (category != event.getCategory()))
//                    continue;
//                else if ((!city.isEmpty()) && (!event.getCity().contains(city)))
//                    continue;
//                else if ((!keyword.isEmpty()) &&
//                         (!event.getTitle().contains(keyword)) &&
//                         (!event.getDescription().contains(keyword)) &&
//                         (!event.getPerformer().contains(keyword)))
//                    continue;
//
//                eventList.add(event);
//            }
//        }
//
//        notifyDataSetChanged();
//    }
//}

        //------------------------------------------------------------------------------------------
    }
}