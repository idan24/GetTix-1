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

import org.joda.time.DateTime;

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
    private LinearLayoutManager linearLayoutManager;

    private DatabaseReference firebaseDatabaseReference;
    private DatabaseReference eventsDatabaseReference;
    private FirebaseRecyclerAdapter<Event, EventViewHolder> firebaseRecyclerAdapter;

    SnapshotParser<Event> eventSnapshotParser;

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

        linearLayoutManager = new LinearLayoutManager(this);
        eventsRecyclerView.setLayoutManager(linearLayoutManager);

        // New child entries
        firebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        eventsDatabaseReference = firebaseDatabaseReference.child("events");

        updateWithFilter(Long.parseLong("0"),"", DataUtils.Category.ALL, "", "");

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

        btSearchEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, Integer.parseInt(etEventDate.getText().toString().substring(6,10)));
                calendar.set(Calendar.MONTH, Integer.parseInt(etEventDate.getText().toString().substring(3,5)));
                calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(etEventDate.getText().toString().substring(0,2)));

                updateWithFilter(new DateTime(calendar.getTime()).getMillis(),
                        etEventHall.getText().toString(),
                        (DataUtils.Category) spEventCategory.getSelectedItem(),
                        etEventCity.getText().toString(),
                        etEventKeyword.getText().toString());
            }
        });
    }

    private void updateWithFilter(Long eventDate, String eventHallName, DataUtils.Category eventCategory,
                                  String eventCity, String keyword) {
        eventSnapshotParser = new SnapshotParser<Event>() {
            @Override
            public Event parseSnapshot(DataSnapshot dataSnapshot) {
                Event event = dataSnapshot.getValue(Event.class);
                if (event != null) {
                    event.setUid(dataSnapshot.getKey());
                }
                return event;
            }
        };

        FirebaseRecyclerOptions<Event> options =
                new FirebaseRecyclerOptions.Builder<Event>()
                        .setQuery(eventsDatabaseReference, eventSnapshotParser)
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
        //------------------------------------------------------------------------------------------
    }
}