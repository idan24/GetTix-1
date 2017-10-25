package app.com.almogrubi.idansasson.gettix;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.util.Log;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference eventsDatabaseReference;
    private ChildEventListener childEventListener;

    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;

    //List<Event> eventList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseDatabase = FirebaseDatabase.getInstance();
        eventsDatabaseReference = firebaseDatabase.getReference().child("events");

        // Initialize references to views
        etEventDate = findViewById(R.id.et_find_by_date);
        etEventHall = findViewById(R.id.et_find_by_hall);
        etEventCity = findViewById(R.id.et_find_by_city);
        etEventKeyword = findViewById(R.id.et_find_by_keyword);
        spEventCategory = findViewById(R.id.sp_find_by_category);
        btSearchEvents = findViewById(R.id.bt_search_events);
        recyclerView = findViewById(R.id.searched_events_recycler_view);

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

        // Initializing an ArrayAdapter
        final ArrayAdapter<DataUtils.Category> spinnerArrayAdapter = new ArrayAdapter<DataUtils.Category>(
                this, R.layout.spinner_item, DataUtils.Category.values()) {
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
        };
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        spEventCategory.setAdapter(spinnerArrayAdapter);

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

        // Setting up query for adapter
        FirebaseRecyclerOptions<Event> options =
                new FirebaseRecyclerOptions.Builder<Event>()
                        .setQuery(eventsDatabaseReference, Event.class)
                        .build();
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Event, EventViewHolder>(options) {
            @Override
            public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
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
                        Intent detailActivityIntent = new Intent(context, DetailActivity.class);
                        detailActivityIntent.putExtra("eventObject", model);
                        context.startActivity(detailActivityIntent);
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


        //List<Event> showList = new ArrayList<>();
        //Event startwars = new Event("1", "starwars","best movie ever", DataUtils.Category.THEATER, "1","Tel Aviv","",new Date(1),60,
        //        "startwars",30, true ,16,"" );
        //Event got = new Event("2", "game of thrones","2nd best movie ever", DataUtils.Category.THEATER, "2","Tel Aviv","",new Date(2),60,
        //        "got",30, false ,16,"" );
        //showList.add(startwars);
        //showList.add(got);


        //mAdapter = new RecyclerViewAdapter(this, showList);
        //recyclerView.setAdapter(mAdapter);

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
}