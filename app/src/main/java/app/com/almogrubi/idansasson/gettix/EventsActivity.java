package app.com.almogrubi.idansasson.gettix;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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

import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.utilities.ManagementScreen;

public class EventsActivity extends ManagementScreen {

    private TextView tvWelcome;
    private RecyclerView eventsRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    private DatabaseReference firebaseDatabaseReference;
    private DatabaseReference eventsDatabaseReference;
    private FirebaseRecyclerAdapter<Event, EventViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
    }

    @Override
    protected void onSignedInInitialize(FirebaseUser user) {
        super.onSignedInInitialize(user);

        tvWelcome = findViewById(R.id.tv_welcome_manager);
        eventsRecyclerView = findViewById(R.id.events_recycler_view);

        tvWelcome.setText(String.format("שלום, %s", user.getDisplayName()));

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
                                            Event event) {
                viewHolder.bindEvent(event);
            }
        };

        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {});

        eventsRecyclerView.setAdapter(firebaseRecyclerAdapter);

//        // Setting up query for adapter
//        FirebaseRecyclerOptions<Event> options =
//                new FirebaseRecyclerOptions.Builder<Event>()
//                        .setQuery(eventsDatabaseReference.orderByChild("producerId").equalTo(user.getUid()), Event.class)
//                        .build();
//oncreateviewholder:
//                view.setFocusable(true);
//            }
//
//            @Override
//            protected void onBindViewHolder(EventViewHolder holder, int position, final Event model) {
//                holder.bindEvent(model);
//                holder.itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Context context = v.getContext();
//                        Intent eventEditActivityIntent = new Intent(context, EventEditActivity.class);
//                        eventEditActivityIntent.putExtra("eventObject", model);
//                        context.startActivity(eventEditActivityIntent);
//                    }
//                });
//            }
//        };
//
    }

    @Override
    protected void onSignedOutCleanup() {
        super.onSignedOutCleanup();
    }
}
