package app.com.almogrubi.idansasson.gettix.activities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import app.com.almogrubi.idansasson.gettix.viewholders.EventIncomeViewHolder;
import app.com.almogrubi.idansasson.gettix.viewholders.EventOccupancyViewHolder;
import app.com.almogrubi.idansasson.gettix.R;
import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.authentication.ManagementScreen;

public class ReportsActivity extends ManagementScreen {

    enum Report {
        INCOME("הכנסות"),
        OCCUPANCY("תפוסה");

        private String friendlyName;

        private Report(String friendlyName){
            this.friendlyName = friendlyName;
        }

        @Override public String toString(){
            return friendlyName;
        }
    };

    private Spinner spReport;

    private RecyclerView reportRecyclerView;
    private LinearLayoutManager linearLayoutManager;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference eventsDatabaseReference;

    private FirebaseRecyclerAdapter<Event, EventIncomeViewHolder> eventIncomeFirebaseRecyclerAdapter;
    private FirebaseRecyclerAdapter<Event, EventOccupancyViewHolder> eventOccupancyFirebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        spReport = findViewById(R.id.sp_report);
        reportRecyclerView = findViewById(R.id.report_recycler_view);

        linearLayoutManager = new LinearLayoutManager(this);
        reportRecyclerView.setLayoutManager(linearLayoutManager);

        initializeDatabaseReferences();

        // Initializing an ArrayAdapter for the report spinner
        final ArrayAdapter<Report> spinnerArrayAdapter = new ArrayAdapter<Report>(
                this, R.layout.spinner_item, Report.values()) {};
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        spReport.setAdapter(spinnerArrayAdapter);
        spReport.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Report selectedReport = (Report) spReport.getSelectedItem();

                if (selectedReport == Report.INCOME)
                    loadIncomeReport();
                else if (selectedReport == Report.OCCUPANCY)
                    loadOccupancyReport();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void initializeDatabaseReferences() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        eventsDatabaseReference = firebaseDatabase.getReference().child("events");
    }

    private void loadIncomeReport() {
        SnapshotParser<Event> eventSnapshotParser = new SnapshotParser<Event>() {
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
                        .setQuery(eventsDatabaseReference
                                .orderByChild("producerId").equalTo(user.getUid()),
                                eventSnapshotParser)
                        .build();

        FirebaseRecyclerAdapter<Event, EventIncomeViewHolder> newFirebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Event, EventIncomeViewHolder>(options) {
                    @Override
                    public EventIncomeViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                        return new EventIncomeViewHolder(inflater.inflate(R.layout.event_income_list_item, viewGroup, false));
                    }

                    @Override
                    protected void onBindViewHolder(final EventIncomeViewHolder viewHolder,
                                                    int position,
                                                    final Event event) {
                        viewHolder.bindEvent(event);
                    }
                };

        newFirebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {});

        if (eventIncomeFirebaseRecyclerAdapter != null)
            eventIncomeFirebaseRecyclerAdapter.stopListening();
        if (eventOccupancyFirebaseRecyclerAdapter != null)
            eventOccupancyFirebaseRecyclerAdapter.stopListening();
        eventIncomeFirebaseRecyclerAdapter = newFirebaseRecyclerAdapter;

        reportRecyclerView.setAdapter(eventIncomeFirebaseRecyclerAdapter);

        eventIncomeFirebaseRecyclerAdapter.startListening();
    }

    private void loadOccupancyReport() {
        SnapshotParser<Event> eventSnapshotParser = new SnapshotParser<Event>() {
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
                        .setQuery(eventsDatabaseReference
                                        .orderByChild("producerId").equalTo(user.getUid()),
                                eventSnapshotParser)
                        .build();

        FirebaseRecyclerAdapter<Event, EventOccupancyViewHolder> newFirebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Event, EventOccupancyViewHolder>(options) {
                    @Override
                    public EventOccupancyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                        return new EventOccupancyViewHolder(inflater.inflate(R.layout.event_occupancy_list_item, viewGroup, false));
                    }

                    @Override
                    protected void onBindViewHolder(final EventOccupancyViewHolder viewHolder,
                                                    int position,
                                                    final Event event) {
                        viewHolder.bindEvent(event);
                    }
                };

        newFirebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {});

        if (eventIncomeFirebaseRecyclerAdapter != null)
            eventIncomeFirebaseRecyclerAdapter.stopListening();
        if (eventOccupancyFirebaseRecyclerAdapter != null)
            eventOccupancyFirebaseRecyclerAdapter.stopListening();
        eventOccupancyFirebaseRecyclerAdapter = newFirebaseRecyclerAdapter;

        reportRecyclerView.setAdapter(eventOccupancyFirebaseRecyclerAdapter);

        eventOccupancyFirebaseRecyclerAdapter.startListening();
    }

    @Override
    public void onPause() {
        if (eventIncomeFirebaseRecyclerAdapter != null)
            eventIncomeFirebaseRecyclerAdapter.stopListening();
        if (eventOccupancyFirebaseRecyclerAdapter != null)
            eventOccupancyFirebaseRecyclerAdapter.stopListening();

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (eventIncomeFirebaseRecyclerAdapter != null)
            eventIncomeFirebaseRecyclerAdapter.startListening();
        if (eventOccupancyFirebaseRecyclerAdapter != null)
            eventOccupancyFirebaseRecyclerAdapter.stopListening();
    }
}
