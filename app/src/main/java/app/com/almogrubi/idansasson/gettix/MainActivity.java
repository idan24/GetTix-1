package app.com.almogrubi.idansasson.gettix;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.util.Log;
import android.widget.Spinner;

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
    private LinearLayoutManager mLayoutManager;

    List<Event> eventList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseDatabase = FirebaseDatabase.getInstance();
        eventsDatabaseReference = firebaseDatabase.getReference().child("events");

        // Initialize references to views
        etEventDate = (EditText) findViewById(R.id.et_find_by_date);
        etEventHall = (EditText) findViewById(R.id.et_find_by_hall);
        etEventCity = (EditText) findViewById(R.id.et_find_by_city);
        etEventKeyword = (EditText) findViewById(R.id.et_find_by_keyword);
        spEventCategory = (Spinner) findViewById(R.id.sp_find_by_category);
        btSearchEvents = (Button) findViewById(R.id.bt_search_events);
        recyclerView = (RecyclerView) findViewById(R.id.events_recycler_view);

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

        spEventCategory.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, DataUtils.Category.values()));

        btSearchEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.filter(
                        Date.valueOf(etEventDate.getText().toString()),
                        etEventHall.getText().toString(),
                        (DataUtils.Category) spEventCategory.getSelectedItem(),
                        etEventCity.getText().toString(),
                        etEventKeyword.getText().toString());
            }
        });

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        adapter = new RecyclerViewAdapter(this, eventList);
        recyclerView.setAdapter(adapter);
    }
}