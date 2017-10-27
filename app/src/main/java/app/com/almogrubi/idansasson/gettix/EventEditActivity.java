package app.com.almogrubi.idansasson.gettix;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import app.com.almogrubi.idansasson.gettix.databinding.ActivityEventEditBinding;
import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.entities.Hall;
import app.com.almogrubi.idansasson.gettix.utilities.DataUtils;
import app.com.almogrubi.idansasson.gettix.utilities.ManagementScreen;

public class EventEditActivity extends ManagementScreen {

    private Spinner spEventCategory;
    private Spinner spEventHall;
    private EditText etEventDate;
    private EditText etEventHour;
    private TextView tvEventMaxCapacity;
    private EditText etEventMaxCapacity;
    private Button btSave;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference eventsDatabaseReference;
    private DatabaseReference hallsDatabaseReference;

    private ActivityEventEditBinding binding;
    private Event event;

    public EventEditActivity(Event event) {
        this.event = event;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);

        spEventCategory = findViewById(R.id.sp_event_category);
        spEventHall = findViewById(R.id.sp_event_hall);
        etEventDate = findViewById(R.id.et_event_date);
        etEventHour = findViewById(R.id.et_event_hour);
        tvEventMaxCapacity = findViewById(R.id.tv_event_max_capacity);
        etEventMaxCapacity = findViewById(R.id.et_event_max_capacity);
        btSave = findViewById(R.id.bt_save_event);

        firebaseDatabase = FirebaseDatabase.getInstance();
        eventsDatabaseReference = firebaseDatabase.getReference().child("events");
        hallsDatabaseReference = firebaseDatabase.getReference().child("halls");

        setSpinnersAdapterSource();
        bindEventDateTime(event != null ? new DateTime(event.getDateTime()) : DateTime.now());

        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_edit);

        if (event != null) {
            bindEventInfo();
        }

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: save to firebase
                if (checkInputValidity()) {
                    String newEventId = eventsDatabaseReference.push().getKey();
                    String newEventTitle = binding.etEventTitle.getText().toString();

                    Hall newEventHall = (Hall) binding.spEventHall.getSelectedItem();

                }
            }
        });
    }

    private boolean checkInputValidity() {
        return true;
    }

    public void onMarkedSeatsClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        if (view.getId() == R.id.cb_event_marked_seats) {
            if (checked) {
                tvEventMaxCapacity.setVisibility(View.GONE);
                etEventMaxCapacity.setVisibility(View.GONE);
            } else {
                tvEventMaxCapacity.setVisibility(View.VISIBLE);
                etEventMaxCapacity.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setSpinnersAdapterSource() {
        // Initializing an ArrayAdapter for the Spinner
        final ArrayAdapter<DataUtils.Category> categorySpinnerArrayAdapter = new ArrayAdapter<DataUtils.Category>(
                this, R.layout.spinner_item, DataUtils.Category.values()) {};
        categorySpinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        spEventCategory.setAdapter(categorySpinnerArrayAdapter);

        hallsDatabaseReference.orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Hall> halls = new ArrayList<>();
                for (DataSnapshot hallSnapshot : dataSnapshot.getChildren()) {
                    Hall hall = hallSnapshot.getValue(Hall.class);
                    halls.add(hall);
                }
                final ArrayAdapter<Hall> hallSpinnerArrayAdapter = new ArrayAdapter<Hall>(
                        EventEditActivity.this, R.layout.spinner_item, halls) {};
                hallSpinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
                spEventHall.setAdapter(hallSpinnerArrayAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void bindEventDateTime(final DateTime dateTime) {
        final Calendar calendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
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
        final TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
                etEventHour.setText(sdf.format(calendar.getTime()));
            }
        };

        etEventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(EventEditActivity.this, dateSetListener,
                        dateTime.getYear(),
                        dateTime.getMonthOfYear(),
                        dateTime.getDayOfMonth())
                        .show();
            }
        });
        etEventHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(EventEditActivity.this, timeSetListener,
                        dateTime.getHourOfDay(),
                        dateTime.getMinuteOfHour(),
                        true)
                        .show();
            }
        });
    }

    private void bindEventInfo() {
        binding.tvEventEditTitle.setText(R.string.event_edit_title);

        binding.etEventTitle.setText(event.getTitle());
        binding.spEventCategory.setSelection(event.getCategory().ordinal());

        hallsDatabaseReference.orderByChild("uid").equalTo(event.getHall().getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Hall hall = dataSnapshot.getValue(Hall.class);
                binding.spEventHall.setSelection(
                        ((ArrayAdapter)binding.spEventHall.getAdapter()).getPosition(hall.getName()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        binding.etEventDuration.setText(event.getDuration());
        binding.etEventDescription.setText(event.getDescription());
        binding.etEventPerformer.setText(event.getPerformer());
        binding.etEventPrice.setText(event.getPrice());

        // TODO: image

        binding.cbEventMarkedSeats.setSelected(event.hasMarkedSeats());
        binding.etEventMaxCapacity.setText(event.getMaxCapacity());
        binding.cbEventSoldOut.setSelected(event.isSoldOut());
    }
}
