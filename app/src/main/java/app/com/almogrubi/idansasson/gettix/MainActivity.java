package app.com.almogrubi.idansasson.gettix;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.cloudinary.android.MediaManager;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.entities.Hall;
import app.com.almogrubi.idansasson.gettix.utilities.DataUtils;
import app.com.almogrubi.idansasson.gettix.utilities.HallSpinnerAdapter;
import app.com.almogrubi.idansasson.gettix.utilities.Utils;

import static app.com.almogrubi.idansasson.gettix.utilities.Utils.INDEXED_KEY_DIVIDER;

/**
 * Created by almogrubi on 10/14/17.
 */

public class MainActivity extends AppCompatActivity {

    enum FilterKey {
        DATE("תאריך"),
        HALL("אולם"),
        CITY("עיר");

        private String friendlyName;

        private FilterKey(String friendlyName){
            this.friendlyName = friendlyName;
        }

        @Override public String toString(){
            return friendlyName;
        }
    };

    private Spinner spEventCategory;
    private CheckBox cbFilter;
    private Spinner spFilterKey;
    private TextView tvFilterColon;
    private EditText etEventDate;
    private AutoCompleteTextView etEventHall;
    private EditText etEventCity;
    private Button btSearchEvents;
    private RecyclerView eventsRecyclerView;
    private LinearLayoutManager linearLayoutManager;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference eventsDatabaseReference;
    private DatabaseReference hallsDatabaseReference;
    private DatabaseReference hallEventsDatabaseReference;
    private DatabaseReference dateEventsDatabaseReference;
    private DatabaseReference cityEventsDatabaseReference;
    private DatabaseReference categoryEventsDatabaseReference;
    private DatabaseReference categoryDateEventsDatabaseReference;
    private DatabaseReference categoryCityEventsDatabaseReference;
    private DatabaseReference categoryHallEventsDatabaseReference;

    private Hall selectedHall = null;
    private FirebaseRecyclerAdapter<Event, EventViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize references to views
        cbFilter = findViewById(R.id.cb_filter);
        spFilterKey = findViewById(R.id.sp_filter_key);
        tvFilterColon = findViewById(R.id.tv_filter_colon);
        spEventCategory = findViewById(R.id.sp_category);
        etEventDate = findViewById(R.id.et_find_by_date);
        etEventHall = findViewById(R.id.et_find_by_hall);
        etEventCity = findViewById(R.id.et_find_by_city);
        btSearchEvents = findViewById(R.id.bt_search_events);
        eventsRecyclerView = findViewById(R.id.searched_events_recycler_view);

        linearLayoutManager = new LinearLayoutManager(this);
        eventsRecyclerView.setLayoutManager(linearLayoutManager);

        initializeDatabaseReferences();

        // Initializing an ArrayAdapter for the category spinner
        final ArrayAdapter<DataUtils.Category> spinnerArrayAdapter = new ArrayAdapter<DataUtils.Category>(
                this, R.layout.spinner_item, DataUtils.Category.values()) {};
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        spEventCategory.setAdapter(spinnerArrayAdapter);

        cbFilter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    expandFiltering();
                else
                    shrinkFiltering();
            }
        });

        // Initializing an ArrayAdapter for the filter key spinner
        final ArrayAdapter<FilterKey> filterKeyArrayAdapter = new ArrayAdapter<FilterKey>(
                this, R.layout.spinner_item, FilterKey.values()) {};
        filterKeyArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        spFilterKey.setAdapter(filterKeyArrayAdapter);
        spFilterKey.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateFilterInputView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateFilterInputView();
            }
        });

        shrinkFiltering();

        final Calendar calendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                etEventDate.setText(DataUtils.UI_DATE_FORMAT.format(calendar.getTime()));
            }
        };
        etEventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(MainActivity.this, dateSetListener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });

        hallsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<Hall> halls = new ArrayList<>();

                if (dataSnapshot.exists())
                    for (DataSnapshot hallSnapshot : dataSnapshot.getChildren())
                        halls.add(hallSnapshot.getValue(Hall.class));

                final HallSpinnerAdapter hallSpinnerAdapter =
                        new HallSpinnerAdapter(MainActivity.this, R.layout.spinner_item, halls);
                hallSpinnerAdapter.setDropDownViewResource(R.layout.spinner_item);
                etEventHall.setAdapter(hallSpinnerAdapter);
                etEventHall.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        selectedHall = hallSpinnerAdapter.getItem(position);
                        etEventHall.setError(null);
                    }
                });
                etEventHall.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                        ArrayList<Hall> halls = hallSpinnerAdapter.getValues();
                        for (Hall hall : halls) {
                            if (hall.getName().equals(s.toString())) {
                                selectedHall = hall;
                                etEventHall.setError(null);
                                return;
                            }
                        }

                        // Unset the selected hall whenever the user types. Validation will then fail.
                        // This is how we enforce selecting from the list.
                        selectedHall = null;
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        btSearchEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInputValid()) {
                    searchEvents();
                }
            }
        });

        // In initial display, we load all events with no filters
        loadEventsByIndexKey(DataUtils.EventIndexKey.ALL);
    }

    private void initializeDatabaseReferences() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        eventsDatabaseReference = firebaseDatabase.getReference().child("events");
        hallsDatabaseReference = firebaseDatabase.getReference().child("halls");
        hallEventsDatabaseReference = firebaseDatabase.getReference().child("hall_events");
        dateEventsDatabaseReference = firebaseDatabase.getReference().child("date_events");
        cityEventsDatabaseReference = firebaseDatabase.getReference().child("city_events");
        categoryEventsDatabaseReference = firebaseDatabase.getReference().child("category_events");
        categoryDateEventsDatabaseReference = firebaseDatabase.getReference().child("category_date_events");
        categoryCityEventsDatabaseReference = firebaseDatabase.getReference().child("category_city_events");
        categoryHallEventsDatabaseReference = firebaseDatabase.getReference().child("category_hall_events");
    }

    private void expandFiltering() {
        cbFilter.setText(R.string.filter_events_selected);
        spFilterKey.setVisibility(View.VISIBLE);
        tvFilterColon.setVisibility(View.VISIBLE);

        FilterKey filterKey = (FilterKey) spFilterKey.getSelectedItem();

        switch (filterKey) {
            case DATE:
                etEventDate.setVisibility(View.VISIBLE);
                break;
            case HALL:
                etEventHall.setVisibility(View.VISIBLE);
                break;
            case CITY:
                etEventCity.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void shrinkFiltering() {
        cbFilter.setText(R.string.filter_events_label);
        spFilterKey.setVisibility(View.GONE);
        tvFilterColon.setVisibility(View.GONE);
        etEventDate.setVisibility(View.GONE);
        etEventHall.setVisibility(View.GONE);
        etEventCity.setVisibility(View.GONE);
    }

    private void updateFilterInputView() {

        FilterKey filterKey = (FilterKey) spFilterKey.getSelectedItem();

        switch (filterKey) {
            case DATE:
                etEventDate.setVisibility(View.VISIBLE);
                etEventHall.setVisibility(View.GONE);
                etEventCity.setVisibility(View.GONE);
                break;
            case HALL:
                etEventDate.setVisibility(View.GONE);
                etEventHall.setVisibility(View.VISIBLE);
                etEventCity.setVisibility(View.GONE);
                break;
            case CITY:
                etEventDate.setVisibility(View.GONE);
                etEventHall.setVisibility(View.GONE);
                etEventCity.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void loadEventsByIndexKey(DataUtils.EventIndexKey indexKey) {

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
                        .setQuery(getQueryFromIndexKey(indexKey), eventSnapshotParser)
                        .build();

        FirebaseRecyclerAdapter<Event, EventViewHolder> newFirebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Event, EventViewHolder>(options) {
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
                        Intent detailActivityIntent = new Intent(context, EventDetailsActivity.class);
                        detailActivityIntent.putExtra("eventUid", event.getUid());
                        context.startActivity(detailActivityIntent);
                    }
                });
            }
        };

        newFirebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {});

        if (firebaseRecyclerAdapter != null)
            firebaseRecyclerAdapter.stopListening();
        firebaseRecyclerAdapter = newFirebaseRecyclerAdapter;

        eventsRecyclerView.setAdapter(firebaseRecyclerAdapter);

        firebaseRecyclerAdapter.startListening();
    }

    private Query getQueryFromIndexKey(DataUtils.EventIndexKey indexKey) {
        String nowDateString = DataUtils.createDbStringFromDate(new Date(DateTime.now().getMillis()));
        Query returnedQuery = null;

        switch (indexKey) {
            case ALL: {
                returnedQuery = eventsDatabaseReference
                        .orderByChild("date").startAt(nowDateString);
                break;
            }
            case CATEGORY: {
                String searchedCategory = ((DataUtils.Category) spEventCategory.getSelectedItem()).name();
                returnedQuery = categoryEventsDatabaseReference
                        .child(searchedCategory)
                        .orderByChild("date").startAt(nowDateString);
                break;
            }
            case DATE: {
                String searchedDate = DataUtils.convertToDbDateFormat(etEventDate.getText().toString());
                returnedQuery = dateEventsDatabaseReference
                        .child(searchedDate);
                break;
            }
            case HALL: {
                String searchedHallUid = selectedHall.getUid();
                returnedQuery = hallEventsDatabaseReference
                        .child(searchedHallUid)
                        .orderByChild("date").startAt(nowDateString);
                break;
            }
            case CITY: {
                String searchedCity = etEventCity.getText().toString();
                returnedQuery = cityEventsDatabaseReference
                        .child(searchedCity)
                        .orderByChild("date").startAt(nowDateString);
                break;
            }
            case CATEGORY_DATE: {
                String searchedCategory = ((DataUtils.Category) spEventCategory.getSelectedItem()).name();
                String searchedDate = DataUtils.convertToDbDateFormat(etEventDate.getText().toString());
                returnedQuery = categoryDateEventsDatabaseReference
                        .child(searchedCategory + INDEXED_KEY_DIVIDER + searchedDate);
                break;
            }
            case CATEGORY_HALL: {
                String searchedCategory = ((DataUtils.Category) spEventCategory.getSelectedItem()).name();
                String searchedHallUid = selectedHall.getUid();
                returnedQuery = categoryHallEventsDatabaseReference
                        .child(searchedCategory + INDEXED_KEY_DIVIDER + searchedHallUid)
                        .orderByChild("date").startAt(nowDateString);
                break;
            }
            case CATEGORY_CITY: {
                String searchedCategory = ((DataUtils.Category) spEventCategory.getSelectedItem()).name();
                String searchedCity = etEventCity.getText().toString();
                returnedQuery = categoryCityEventsDatabaseReference
                        .child(searchedCategory + INDEXED_KEY_DIVIDER + searchedCity)
                        .orderByChild("date").startAt(nowDateString);
                break;
            }
        }

        return returnedQuery;
    }

    private boolean isInputValid() {
        // No input needs to be validated when filtering option not checked
        if (!cbFilter.isChecked()) return true;

        String DateNotSelectedErrorMessage = "באיזה תאריך תרצה ללכת?";
        String hallNotSelectedErrorMessage = "הקלד ובחר אולם מרשימת ההצעות";
        String emptyCityErrorMessage = "לאיזו עיר תרצה לצאת?";

        FilterKey filterKey = (FilterKey) spFilterKey.getSelectedItem();

        switch (filterKey) {
            case DATE:
                if (Utils.isTextViewEmpty(etEventDate)) {
                    etEventDate.setError(DateNotSelectedErrorMessage);
                    return false;
                }
                etEventDate.setError(null);
                return true;
            case HALL:
                if (selectedHall == null) {
                    etEventHall.setError(hallNotSelectedErrorMessage);
                    return false;
                }
                etEventHall.setError(null);
                return true;
            case CITY:
                if (Utils.isTextViewEmpty(etEventCity)) {
                    etEventCity.setError(emptyCityErrorMessage);
                    return false;
                }
                etEventCity.setError(null);
                return true;
            default:
                return false;
        }
    }

    private void searchEvents() {
        DataUtils.Category selectedCategory = (DataUtils.Category) spEventCategory.getSelectedItem();

        // Additional filtering is disabled - searching only by category
        if (!cbFilter.isChecked()) {

            if (selectedCategory == DataUtils.Category.ALL) {
                loadEventsByIndexKey(DataUtils.EventIndexKey.ALL);
                return;
            }
            else {
                loadEventsByIndexKey(DataUtils.EventIndexKey.CATEGORY);
                return;
            }
        }
        // Additional filtering is enabled
        else {
            FilterKey filterKey = (FilterKey) spFilterKey.getSelectedItem();

            switch (filterKey) {
                case DATE:
                    if (selectedCategory == DataUtils.Category.ALL)
                        loadEventsByIndexKey(DataUtils.EventIndexKey.DATE);
                    else
                        loadEventsByIndexKey(DataUtils.EventIndexKey.CATEGORY_DATE);
                    break;
                case HALL:
                    if (selectedCategory == DataUtils.Category.ALL)
                        loadEventsByIndexKey(DataUtils.EventIndexKey.HALL);
                    else
                        loadEventsByIndexKey(DataUtils.EventIndexKey.CATEGORY_HALL);
                    break;
                case CITY:
                    if (selectedCategory == DataUtils.Category.ALL)
                        loadEventsByIndexKey(DataUtils.EventIndexKey.CITY);
                    else
                        loadEventsByIndexKey(DataUtils.EventIndexKey.CATEGORY_CITY);
                    break;
            }
        }
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
            startActivity(new Intent(this, ManagementActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        if (firebaseRecyclerAdapter != null)
            firebaseRecyclerAdapter.stopListening();

        etEventDate.setError(null);
        etEventHall.setError(null);
        etEventCity.setError(null);

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (firebaseRecyclerAdapter != null)
            firebaseRecyclerAdapter.startListening();
    }
}