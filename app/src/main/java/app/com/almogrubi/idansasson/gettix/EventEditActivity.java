package app.com.almogrubi.idansasson.gettix;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.com.almogrubi.idansasson.gettix.databinding.ActivityEventEditBinding;
import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.entities.EventHall;
import app.com.almogrubi.idansasson.gettix.entities.Hall;
import app.com.almogrubi.idansasson.gettix.utilities.DataUtils;
import app.com.almogrubi.idansasson.gettix.utilities.ManagementScreen;

public class EventEditActivity extends ManagementScreen {

    // Arbitrary request code value for photo picker
    private static final int RC_PHOTO_PICKER =  2;

    private Spinner spEventCategory;
    private Spinner spEventHall;
    private EditText etEventDate;
    private EditText etEventHour;
    private TextView tvEventMaxCapacity;
    private EditText etEventMaxCapacity;
    private ImageView ivEventPoster;
    private Button btLoadPoster;
    private Button btSave;

    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;
    private DatabaseReference eventsDatabaseReference;
    private DatabaseReference hallsDatabaseReference;
    private StorageReference eventPostersStorageReference;

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
        ivEventPoster = findViewById(R.id.iv_event_poster);
        btLoadPoster = findViewById(R.id.bt_load_poster);
        btSave = findViewById(R.id.bt_save_event);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        eventsDatabaseReference = firebaseDatabase.getReference().child("events");
        hallsDatabaseReference = firebaseDatabase.getReference().child("halls");
        eventPostersStorageReference = firebaseStorage.getReference().child("event_posters");

        setSpinnersAdapterSource();
        bindEventDateTime(event != null ? event.getDateTime() : DateTime.now());
        btLoadPoster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });
        ivEventPoster.setVisibility(View.INVISIBLE);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_edit);

        if (event != null) {
            bindEventInfo();
        }

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkInputValidity()) {
                    String newEventId = eventsDatabaseReference.push().getKey();
                    String newEventTitle = binding.etEventTitle.getText().toString();
                    DataUtils.Category newEventCategory = (DataUtils.Category) binding.spEventCategory.getSelectedItem();

                    Hall selectedHall = (Hall) binding.spEventHall.getSelectedItem();
                    EventHall newEventHall =
                            new EventHall(selectedHall.getName(), selectedHall.getCity(), selectedHall.getSeats());

                    final Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, Integer.parseInt(binding.etEventDate.getText().toString().substring(6,9)));
                    calendar.set(Calendar.MONTH, Integer.parseInt(binding.etEventDate.getText().toString().substring(3,4)));
                    calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(binding.etEventDate.getText().toString().substring(0,1)));
                    calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(binding.etEventHour.getText().toString().substring(0,1)));
                    calendar.set(Calendar.MINUTE, Integer.parseInt(binding.etEventHour.getText().toString().substring(3,4)));
                    DateTime newEventDateTime = new DateTime(calendar.getTime());

                    int newEventDuration = TextUtils.isEmpty(binding.etEventDuration.getText())
                            ? 0
                            : Integer.parseInt(binding.etEventDuration.getText().toString());
                    String newEventDescription = binding.etEventDescription.getText().toString();
                    String newEventPerformer = binding.etEventPerformer.getText().toString();
                    int newEventPrice = Integer.parseInt(binding.etEventPrice.getText().toString());
                    String newEventPosterUri = binding.ivEventPoster.getTag().toString();
                    boolean newEventHasMarkedSeats = binding.cbEventMarkedSeats.isSelected();
                    int newEventMaxCapacity = newEventHasMarkedSeats
                            ? 0
                            : Integer.parseInt(binding.etEventMaxCapacity.getText().toString());
                    String newEventProducerId = EventEditActivity.super.user.getUid();
                    event = new Event(newEventId, newEventTitle, newEventCategory, newEventHall, newEventDateTime,
                                newEventDuration, newEventDescription, newEventPerformer, newEventPrice, newEventPosterUri,
                                    newEventHasMarkedSeats, newEventMaxCapacity, newEventProducerId);
                    eventsDatabaseReference.child(newEventId).setValue(event);

                    // Adding event's datetime to its hall's inner list of taken dates
                    Map<String, Object> hallDateTimeUpdate = new HashMap<>();
                    hallDateTimeUpdate.put(newEventDateTime.toString(), newEventDateTime);
                    // "halls/id/event_date_times/"
                    hallsDatabaseReference
                            .child(selectedHall.getId())
                            .child("event_date_times")
                            .updateChildren(hallDateTimeUpdate);
                }
            }
        });
    }

    private boolean checkInputValidity() {
        final String emptyFieldErrorMessage = "יש למלא את השדה";
        final String posterErrorMessage = "יש להעלות פוסטר למופע";
        boolean isValid = true;

        // Checking title was filled
        if (TextUtils.isEmpty(binding.etEventTitle.getText().toString())) {
            binding.etEventTitle.setError(emptyFieldErrorMessage);
            isValid = false;
        }
        // Checking date was picked
        if (TextUtils.isEmpty(binding.etEventDate.getText().toString())) {
            binding.etEventDate.setError(emptyFieldErrorMessage);
            isValid = false;
        }
        // Checking time was picked
        if (TextUtils.isEmpty(binding.etEventHour.getText().toString())) {
            binding.etEventHour.setError(emptyFieldErrorMessage);
            isValid = false;
        }
        // Checking description was filled
        if (TextUtils.isEmpty(binding.etEventDescription.getText().toString())) {
            binding.etEventDescription.setError(emptyFieldErrorMessage);
            isValid = false;
        }
        // Checking price was filled
        if (TextUtils.isEmpty(binding.etEventPrice.getText().toString())) {
            binding.etEventPrice.setError(emptyFieldErrorMessage);
            isValid = false;
        }
        // If the event is not an event with marked seats, checking max capacity was filled
        if (!binding.cbEventMarkedSeats.isSelected() &&
                TextUtils.isEmpty(binding.etEventMaxCapacity.getText().toString())) {
            binding.etEventMaxCapacity.setError(emptyFieldErrorMessage);
            isValid = false;
        }
        // Checking poster file was uploaded
        if (binding.ivEventPoster.getTag() == null) {
            binding.btLoadPoster.setError(posterErrorMessage);
            isValid = false;
        }

        if (!isValid) return isValid;

        // TODO: check hall and date unique - query

        return isValid;
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

        // Initializing an ArrayAdapter for the category spinner
        DataUtils.Category[] categories = DataUtils.Category.values();
        final ArrayAdapter<DataUtils.Category> categorySpinnerArrayAdapter = new ArrayAdapter<DataUtils.Category>(
                this, R.layout.spinner_item, Arrays.copyOfRange(categories, 1, categories.length)) {};
        categorySpinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        spEventCategory.setAdapter(categorySpinnerArrayAdapter);

        hallsDatabaseReference.orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Hall> halls = new ArrayList<>();
                for (DataSnapshot hallSnapshot : dataSnapshot.getChildren()) {
                    halls.add(hallSnapshot.getValue(Hall.class));
                }

                // Initializing an ArrayAdapter for the hall spinner
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

        hallsDatabaseReference.orderByChild("name").equalTo(event.getEventHall().getName()).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "hall" node with all children with id 0
                    Hall hall = dataSnapshot.getChildren().iterator().next().getValue(Hall.class);
                    // do something with the individual "halls"
                    binding.spEventHall.setSelection(
                            ((ArrayAdapter)binding.spEventHall.getAdapter()).getPosition(hall.getName()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        binding.etEventDuration.setText(event.getDuration());
        binding.etEventDescription.setText(event.getDescription());
        binding.etEventPerformer.setText(event.getPerformer());
        binding.etEventPrice.setText(event.getPrice());

        loadEventPoster(binding.ivEventPoster, Uri.parse(event.getPosterUri()));

        binding.cbEventMarkedSeats.setSelected(event.hasMarkedSeats());
        binding.etEventMaxCapacity.setText(event.getMaxCapacity());
        binding.cbEventSoldOut.setSelected(event.isSoldOut());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();

            // Get a reference to store file at event_posters/<FILENAME>
            StorageReference photoRef = eventPostersStorageReference.child(selectedImageUri.getLastPathSegment());

            // Upload file to Firebase Storage
            photoRef.putFile(selectedImageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    loadEventPoster(ivEventPoster, taskSnapshot.getDownloadUrl());
                }
            });
        }
    }

    private void loadEventPoster(ImageView view, Uri photoUri) {
        view.setVisibility(View.VISIBLE);
        Glide.with(view.getContext())
                .load(photoUri)
                .into(view);
        view.setTag(photoUri);
    }
}
