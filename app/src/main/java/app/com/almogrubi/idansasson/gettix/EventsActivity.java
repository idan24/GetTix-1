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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.utilities.ManagementScreen;

public class EventsActivity extends ManagementScreen {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference eventsDatabaseReference;

    private TextView tvWelcome;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
    }

    @Override
    protected void onSignedInInitialize(FirebaseUser user) {
        super.onSignedInInitialize(user);

        tvWelcome = findViewById(R.id.tv_welcome_manager);
        recyclerView = findViewById(R.id.events_recycler_view);

        firebaseDatabase = FirebaseDatabase.getInstance();
        eventsDatabaseReference = firebaseDatabase.getReference().child("events");

        tvWelcome.setText(String.format("שלום, %s", user.getDisplayName()));

        // Setting up query for adapter
        FirebaseRecyclerOptions<Event> options =
                new FirebaseRecyclerOptions.Builder<Event>()
                        .setQuery(eventsDatabaseReference.orderByChild("producerId").equalTo(user.getUid()), Event.class)
                        .build();
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Event, EventViewHolder>(options) {
            @Override
            public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list_item, parent, false);
                view.setFocusable(true);
                return new EventViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(EventViewHolder holder, int position, final Event model) {
                holder.bindEvent(model);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Context context = v.getContext();
                        Intent eventEditActivityIntent = new Intent(context, EventEditActivity.class);
                        eventEditActivityIntent.putExtra("eventObject", model);
                        context.startActivity(eventEditActivityIntent);
                    }
                });
            }
        };

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onSignedOutCleanup() {
        super.onSignedOutCleanup();
    }
}
