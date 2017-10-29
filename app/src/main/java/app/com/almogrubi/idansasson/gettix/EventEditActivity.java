package app.com.almogrubi.idansasson.gettix;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
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
import com.google.firebase.auth.FirebaseUser;
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

import app.com.almogrubi.idansasson.gettix.databinding.ActivityEventEditBinding;
import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.entities.EventHall;
import app.com.almogrubi.idansasson.gettix.entities.Hall;
import app.com.almogrubi.idansasson.gettix.utilities.DataUtils;
import app.com.almogrubi.idansasson.gettix.utilities.HallSpinnerAdapter;
import app.com.almogrubi.idansasson.gettix.utilities.ManagementScreen;

public class EventEditActivity extends ManagementScreen {

    // Arbitrary request code value for photo picker
    private static final int RC_PHOTO_PICKER =  2;

    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;
    private DatabaseReference eventsDatabaseReference;
    private DatabaseReference hallsDatabaseReference;
    private StorageReference eventPostersStorageReference;

    private ActivityEventEditBinding binding;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);
    }

    @Override
    protected void onSignedInInitialize(FirebaseUser user) {
        super.onSignedInInitialize(user);

        Intent intent = this.getIntent();

        if (intent != null) {
            event = (Event) intent.getSerializableExtra("eventObject");
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_edit);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        eventsDatabaseReference = firebaseDatabase.getReference().child("events");
        hallsDatabaseReference = firebaseDatabase.getReference().child("halls");
        eventPostersStorageReference = firebaseStorage.getReference().child("event_posters");

        setSpinnersAdapterSource();
        bindEventDateTime(event != null ? new DateTime(event.getDateTime()) : DateTime.now());
        binding.btLoadPoster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });
        binding.ivEventPoster.setVisibility(View.INVISIBLE);
        binding.cbEventSoldOut.setVisibility(View.GONE);

        if (event != null) {
            bindEventInfo();
            binding.cbEventSoldOut.setVisibility(View.VISIBLE);
        }

        binding.btSaveEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkInputValidity()) {
                    String newEventId = eventsDatabaseReference.push().getKey();
                    String newEventTitle = binding.etEventTitle.getText().toString();
                    DataUtils.Category newEventCategory = (DataUtils.Category) binding.spEventCategory.getSelectedItem();

                    Hall selectedHall = (Hall) binding.spEventHall.getSelectedItem();
                    EventHall newEventHall =
                            new EventHall(selectedHall.getUid(),
                                    selectedHall.getName(),
                                    selectedHall.getCity(),
                                    selectedHall.getRows(),
                                    selectedHall.getColumns(),
                                    selectedHall.makeEventSeats());

                    final Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, Integer.parseInt(binding.etEventDate.getText().toString().substring(6,10)));
                    calendar.set(Calendar.MONTH, Integer.parseInt(binding.etEventDate.getText().toString().substring(3,5)));
                    calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(binding.etEventDate.getText().toString().substring(0,2)));
                    calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(binding.etEventHour.getText().toString().substring(0,2)));
                    calendar.set(Calendar.MINUTE, Integer.parseInt(binding.etEventHour.getText().toString().substring(3,5)));
                    Long newEventDateTime = new DateTime(calendar.getTime()).getMillis();

                    int newEventDuration = TextUtils.isEmpty(binding.etEventDuration.getText())
                            ? 0
                            : Integer.parseInt(binding.etEventDuration.getText().toString());
                    String newEventDescription = binding.etEventDescription.getText().toString();
                    String newEventPerformer = binding.etEventPerformer.getText().toString();
                    int newEventPrice = Integer.parseInt(binding.etEventPrice.getText().toString());
                    String newEventPosterUri = binding.ivEventPoster.getTag().toString();
                    boolean newEventHasMarkedSeats = binding.cbEventMarkedSeats.isChecked();
                    int newEventMaxCapacity = newEventHasMarkedSeats
                            ? 0
                            : Integer.parseInt(binding.etEventMaxCapacity.getText().toString());
                    String newEventProducerId = EventEditActivity.super.user.getUid();
                    event = new Event(newEventId, newEventTitle, newEventCategory, newEventHall, newEventDateTime,
                            newEventDuration, newEventDescription, newEventPerformer, newEventPrice, newEventPosterUri,
                            newEventHasMarkedSeats, newEventMaxCapacity, newEventProducerId);
                    eventsDatabaseReference.child(newEventId).setValue(event);

                    // Adding event's datetime to its hall's inner list of taken dates
//                    Map<String, Object> hallDateTimeUpdate = new HashMap<>();
//                    hallDateTimeUpdate.put(newEventDateTime.toString(), newEventDateTime);
//                    // "halls/id/event_date_times/"
//                    hallsDatabaseReference
//                            .child(selectedHall.getUid())
//                            .child("eventDateTimes")
//                            .updateChildren(hallDateTimeUpdate);
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
        if (!binding.cbEventMarkedSeats.isChecked() &&
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

    private void setSpinnersAdapterSource() {

        // Initializing an ArrayAdapter for the category spinner
        DataUtils.Category[] categories = DataUtils.Category.values();
        final ArrayAdapter<DataUtils.Category> categorySpinnerArrayAdapter = new ArrayAdapter<DataUtils.Category>(
                this, R.layout.spinner_item, Arrays.copyOfRange(categories, 1, categories.length)) {};
        categorySpinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        binding.spEventCategory.setAdapter(categorySpinnerArrayAdapter);

        hallsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<Hall> halls = new ArrayList<>();

                if (dataSnapshot.exists())
                    for (DataSnapshot hallSnapshot : dataSnapshot.getChildren())
                        halls.add(hallSnapshot.getValue(Hall.class));

                HallSpinnerAdapter hallSpinnerAdapter =
                        new HallSpinnerAdapter(EventEditActivity.this, R.layout.spinner_item, halls);
                hallSpinnerAdapter.setDropDownViewResource(R.layout.spinner_item);
                binding.spEventHall.setAdapter(hallSpinnerAdapter);
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
                binding.etEventDate.setText(sdf.format(calendar.getTime()));
            }
        };
        final TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                binding.etEventHour.setText(sdf.format(calendar.getTime()));
            }
        };

        binding.etEventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(EventEditActivity.this, dateSetListener,
                        dateTime.getYear(),
                        dateTime.getMonthOfYear(),
                        dateTime.getDayOfMonth())
                        .show();
            }
        });
        binding.etEventHour.setOnClickListener(new View.OnClickListener() {
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
        binding.spEventCategory.setSelection(event.getCategoryAsEnum().ordinal());

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
                    loadEventPoster(binding.ivEventPoster, taskSnapshot.getDownloadUrl());
                }
            });
        }
    }

    public void onMarkedSeatsClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        if (view.getId() == R.id.cb_event_marked_seats) {
            if (checked) {
                binding.tvEventMaxCapacity.setVisibility(View.GONE);
                binding.etEventMaxCapacity.setVisibility(View.GONE);
            } else {
                binding.tvEventMaxCapacity.setVisibility(View.VISIBLE);
                binding.etEventMaxCapacity.setVisibility(View.VISIBLE);
            }
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
