package app.com.almogrubi.idansasson.gettix;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.entities.Hall;
import app.com.almogrubi.idansasson.gettix.entities.Seat;
import app.com.almogrubi.idansasson.gettix.utilities.ManagementScreen;

public class EventsActivity extends ManagementScreen {

    private TextView tvWelcome;
    private TextView tvYourEvents;
    private RecyclerView eventsRecyclerView;
    private LinearLayoutManager linearLayoutManager;

    private DatabaseReference firebaseDatabaseReference;
    private DatabaseReference eventsDatabaseReference;
    private FirebaseRecyclerAdapter<Event, EventViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        tvWelcome = findViewById(R.id.tv_welcome_manager);
        tvYourEvents = findViewById(R.id.tv_your_events);
        eventsRecyclerView = findViewById(R.id.events_recycler_view);

        tvYourEvents.setVisibility(View.INVISIBLE);

        linearLayoutManager = new LinearLayoutManager(this);
        eventsRecyclerView.setLayoutManager(linearLayoutManager);

        // New child entries
        firebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        eventsDatabaseReference = firebaseDatabaseReference.child("events");
    }

    @Override
    protected void onSignedInInitialize(FirebaseUser user) {
        super.onSignedInInitialize(user);

        tvWelcome.setText(String.format("שלום, %s", user.getDisplayName()));
        tvYourEvents.setVisibility(View.VISIBLE);

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

        // Displaying all events produced by the signed-in user
        FirebaseRecyclerOptions<Event> options =
                new FirebaseRecyclerOptions.Builder<Event>()
                        .setQuery(eventsDatabaseReference.orderByChild("producerId").equalTo(user.getUid()), parser)
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
                        Intent eventEditActivityIntent = new Intent(context, EventEditActivity.class);
                        eventEditActivityIntent.putExtra("eventObject", event);
                        context.startActivity(eventEditActivityIntent);
                    }
                });
            }
        };

        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {});

        eventsRecyclerView.setAdapter(firebaseRecyclerAdapter);

        firebaseRecyclerAdapter.startListening();
    }

    protected void onSignedOutCleanup() {}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_event) {
            startActivity(new Intent(this, EventEditActivity.class));

//            addNewHall("זאפה הרצליה",
//                    "מדינת היהודים 85",
//                    "הרצליה",
//                    "https://www.zappa-club.co.il/%D7%9E%D7%95%D7%A2%D7%93%D7%95%D7%9F/%D7%96%D7%90%D7%A4%D7%94-%D7%94%D7%A8%D7%A6%D7%9C%D7%99%D7%94/",
//                    30, 15);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addNewHall(String name, String address, String city, String officialWebsite,
                            int rows, int columns) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference hallsDatabaseReference = firebaseDatabase.getReference().child("halls");

        String hallUid = hallsDatabaseReference.push().getKey();

        Map<String, Seat> seats = new HashMap<>();

        for (int i=0; i<rows; i++)
            for (int j=0; j<columns; j++) {
                String seatUid = String.format("%sSEAT%d-%d", hallUid, i+1, j+1);
                seats.put(seatUid, new Seat(seatUid, i+1, j+1));
//                hallsDatabaseReference.child(hallUid).child("seats")
//                        .child(seatUid).setValue(new Seat(seatUid, i+1, j+1));
            }

        Hall newHall = new Hall(hallUid, name, address, city, officialWebsite, rows, columns, seats);
        hallsDatabaseReference.child(hallUid).setValue(newHall);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (firebaseRecyclerAdapter != null)
            firebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (firebaseRecyclerAdapter != null)
            firebaseRecyclerAdapter.stopListening();
    }
}
