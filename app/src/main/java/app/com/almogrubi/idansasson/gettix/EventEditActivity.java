package app.com.almogrubi.idansasson.gettix;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import app.com.almogrubi.idansasson.gettix.databinding.ActivityEventEditBinding;
import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.entities.EventHall;
import app.com.almogrubi.idansasson.gettix.entities.Hall;
import app.com.almogrubi.idansasson.gettix.utilities.DataUtils;
import app.com.almogrubi.idansasson.gettix.utilities.HallSpinnerAdapter;
import app.com.almogrubi.idansasson.gettix.utilities.ManagementScreen;
import app.com.almogrubi.idansasson.gettix.utilities.Utils;

import static app.com.almogrubi.idansasson.gettix.utilities.Utils.INDEXED_KEY_DIVIDER;

public class EventEditActivity extends ManagementScreen {

    public enum EventEditMode {
        NEW_EVENT,
        NEW_EVENT_BASED_ON_EXISTING,
        EXISTING_EVENT
    }

    // Arbitrary request code value for photo picker
    private static final int RC_PHOTO_PICKER =  2;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference eventsDatabaseReference;
    private DatabaseReference hallsDatabaseReference;
    private DatabaseReference hallSeatsDatabaseReference;
    private DatabaseReference hallEventsDatabaseReference;
    private DatabaseReference hallEventDatesDatabaseReference;
    private DatabaseReference dateEventsDatabaseReference;
    private DatabaseReference cityEventsDatabaseReference;
    private DatabaseReference categoryEventsDatabaseReference;
    private DatabaseReference categoryDateEventsDatabaseReference;
    private DatabaseReference categoryCityEventsDatabaseReference;
    private DatabaseReference categoryHallEventsDatabaseReference;
    private DatabaseReference eventSeatsDatabaseReference;

    private ActivityEventEditBinding binding;
    private String eventPosterUri;
    private ProgressDialog progressDialog;

    private EventEditMode eventEditMode;
    private Event editedEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_edit);

        // Initialization of all needed Firebase database references
        initializeDatabaseReferences();

        // Initialization actions that should happen whether this is a new event or an edited existing event
        initializeUIViews();

        Intent intent = this.getIntent();
        if ((intent != null) && (intent.hasExtra("editMode"))) {
            this.eventEditMode = (EventEditMode) intent.getSerializableExtra("editMode");
        } else {
            this.eventEditMode = EventEditMode.NEW_EVENT;
        }

        // If we should be in new/create mode, initialize views accordingly
        if (this.eventEditMode == EventEditMode.NEW_EVENT) {
            bindEventDateTime();
            binding.ivEventPoster.setVisibility(View.INVISIBLE);
            binding.cbEventSoldOut.setVisibility(View.GONE);
            binding.etEventTitle.requestFocus();
        }
        else {
            // If we should be in edit mode or new event based on another,
            // lookup the event in the database and bind its data to UI
            if (intent.hasExtra("eventUid")) {

                eventsDatabaseReference
                        .child(intent.getStringExtra("eventUid"))
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // If we have a null result, the event was somehow not found in the database
                                if (dataSnapshot == null || !dataSnapshot.exists() || dataSnapshot.getValue() == null) {
                                    abort();
                                    return;
                                }

                                // If we reached here then the existing event was found, we'll bind it to UI
                                editedEvent = dataSnapshot.getValue(Event.class);
                                bindExistingEventInfo();

                                if (eventEditMode == EventEditMode.EXISTING_EVENT) {
                                    binding.tvEventEditTitle.setText(R.string.event_edit_title);
                                    bindExistingEventDateTime(editedEvent.getDate(), editedEvent.getHour());
                                    binding.cbEventSoldOut.setVisibility(View.VISIBLE);
                                }
                                else {
                                    bindEventDateTime();
                                    binding.cbEventSoldOut.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                abort();
                            }
                        });
            }
        }
    }

    private void initializeDatabaseReferences() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        eventsDatabaseReference = firebaseDatabase.getReference().child("events");
        hallsDatabaseReference = firebaseDatabase.getReference().child("halls");
        hallSeatsDatabaseReference = firebaseDatabase.getReference().child("hall_seats");
        hallEventsDatabaseReference = firebaseDatabase.getReference().child("hall_events");
        dateEventsDatabaseReference = firebaseDatabase.getReference().child("date_events");
        cityEventsDatabaseReference = firebaseDatabase.getReference().child("city_events");
        categoryEventsDatabaseReference = firebaseDatabase.getReference().child("category_events");
        categoryDateEventsDatabaseReference = firebaseDatabase.getReference().child("category_date_events");
        categoryCityEventsDatabaseReference = firebaseDatabase.getReference().child("category_city_events");
        categoryHallEventsDatabaseReference = firebaseDatabase.getReference().child("category_hall_events");
        eventSeatsDatabaseReference = firebaseDatabase.getReference().child("event_seats");
        hallEventDatesDatabaseReference = firebaseDatabase.getReference().child("hall_eventDates");
    }

    private void initializeUIViews() {
        setSpinnersAdapterSource();

        binding.btLoadPoster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "השלם פעולה באמצעות..."), RC_PHOTO_PICKER);
            }
        });

        binding.btSaveEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveEventIfInputValid();
            }
        });
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

    private void abort() {
        String eventNotFoundErrorMessage = "המופע לא נמצא, נסה שנית";

        Toast.makeText(EventEditActivity.this, eventNotFoundErrorMessage, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(EventEditActivity.this, EventsActivity.class));
    }

    // Used to bind date/time pickers for new event scenario
    private void bindEventDateTime() {
        final Calendar calendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                binding.etEventDate.setText(DataUtils.UI_DATE_FORMAT.format(calendar.getTime()));
            }
        };
        final TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                binding.etEventHour.setText(DataUtils.HOUR_FORMAT.format(calendar.getTime()));
            }
        };

        binding.etEventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(EventEditActivity.this, dateSetListener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });
        binding.etEventHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(EventEditActivity.this, timeSetListener,
                        calendar.get(Calendar.HOUR),
                        calendar.get(Calendar.MINUTE),
                        true)
                        .show();
            }
        });
    }

    // Used to bind date/time pickers for existing event scenario
    private void bindExistingEventDateTime(final String date, final String hour) {
        final Calendar calendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                binding.etEventDate.setText(DataUtils.UI_DATE_FORMAT.format(calendar.getTime()));
            }
        };
        final TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                binding.etEventHour.setText(DataUtils.HOUR_FORMAT.format(calendar.getTime()));
            }
        };

        binding.etEventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(EventEditActivity.this, dateSetListener,
                        DataUtils.getYearFromDbDate(date),
                        DataUtils.getMonthFromDbDate(date) - 1,
                        DataUtils.getDayFromDbDate(date))
                        .show();
            }
        });
        binding.etEventHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(EventEditActivity.this, timeSetListener,
                        DataUtils.getHourFromDbHour(hour),
                        DataUtils.getMinuteFromDbMinute(hour),
                        true)
                        .show();
            }
        });

        binding.etEventDate.setText(DataUtils.convertToUiDateFormat(date));
        binding.etEventHour.setText(hour);
    }

    private void bindExistingEventInfo() {
        binding.etEventTitle.setText(this.editedEvent.getTitle());
        binding.spEventCategory.setSelection(this.editedEvent.getCategoryAsEnum().ordinal() - 1);

        hallsDatabaseReference.child(this.editedEvent.getEventHall().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // dataSnapshot is the "hall" node
                            Hall hall = dataSnapshot.getValue(Hall.class);
                            binding.spEventHall.setSelection(
                                    ((HallSpinnerAdapter)binding.spEventHall.getAdapter()).getPosition(hall));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });

        binding.etEventDuration.setText(String.valueOf(this.editedEvent.getDuration()));
        binding.etEventDescription.setText(this.editedEvent.getDescription());
        binding.etEventPerformer.setText(this.editedEvent.getPerformer());
        binding.etEventPrice.setText(String.valueOf(this.editedEvent.getPrice()));

        if (this.editedEvent.getCouponCode() != 0) {
            binding.etEventCouponCode.setText(String.valueOf(this.editedEvent.getCouponCode()));
            binding.etEventDiscountedPrice.setText(String.valueOf(this.editedEvent.getDiscountedPrice()));
        }

        loadEventPoster(this.editedEvent.getPosterUri());

        binding.cbEventMarkedSeats.setChecked(this.editedEvent.isMarkedSeats());
        binding.etEventMaxCapacity.setText(String.valueOf(this.editedEvent.getMaxCapacity()));
        changeMaxCapacityVisibility(this.editedEvent.isMarkedSeats());

        binding.cbEventSoldOut.setChecked(this.editedEvent.isSoldOut());
    }

    /*
     * The save flow is like this:
     * - Check validity of entered fields
     * - If new event:
     *   - Check hall is not occupied on date
     *   - Save event with seats and hall dates
     * - If edited event:
     *   - If hall/date were changed:
     *     - Check hall is not occupied on date
     *     - Update event with seats and hall dates
     *   - If hall/date stayed the same:
     *     - Update event
     */
    private void saveEventIfInputValid() {
        if (checkInstantInputValidity()) {

            Hall selectedHall = (Hall) binding.spEventHall.getSelectedItem();

            switch (this.eventEditMode) {
                // This is a new event
                case NEW_EVENT:
                    // Requires a check that the hall + date combination is not taken by another event
                    // This is the next step in the event data validation process
                    // Event save will be triggered from fireHallDateUniqueCheck() if needed
                    fireHallDateUniqueCheck(selectedHall.getUid());
                    break;
                // This is an edited existing event
                case EXISTING_EVENT:
                    // If the event hall or event date were changed, we need to check the new hall is available
                    // on the new date
                    String selectedDate = binding.etEventDate.getText().toString();
                    if ((!editedEvent.getEventHall().getUid().equals(selectedHall.getUid())) ||
                            (!editedEvent.getDate().equals(DataUtils.convertToDbDateFormat(selectedDate)))) {
                        // Event save will be triggered from callback inside fireHallDateUniqueCheck() if needed
                        fireHallDateUniqueCheck(selectedHall.getUid());
                    } else {
                        // If there's no need for hall/date unique check, we trigger event save directly from here
                        updateExistingEventAndExit();
                    }
                    break;
                // This is a new event, based on an existing event
                case NEW_EVENT_BASED_ON_EXISTING:
                    // Requires a check that the hall + date combination is not taken by another event
                    // This is the next step in the event data validation process
                    // Event save will be triggered from fireHallDateUniqueCheck() if needed
                    fireHallDateUniqueCheck(selectedHall.getUid());
                    break;
            }
        }
    }

    private boolean checkInstantInputValidity() {
        final String emptyFieldErrorMessage = "יש למלא את השדה";
        final String invalidDiscountedPrice = "יש למלא מחיר מוזל תקין עבור הקופון";
        final String posterErrorMessage = "יש להעלות פוסטר למופע";
        boolean isValid = true;

        // Checking title was filled
        if (Utils.isTextViewEmpty(binding.etEventTitle)) {
            binding.etEventTitle.setError(emptyFieldErrorMessage);
            isValid = false;
        }
        // Checking date was picked
        if (Utils.isTextViewEmpty(binding.etEventDate)) {
            binding.etEventDate.setError(emptyFieldErrorMessage);
            isValid = false;
        }
        // Checking time was picked
        if (Utils.isTextViewEmpty(binding.etEventHour)) {
            binding.etEventHour.setError(emptyFieldErrorMessage);
            isValid = false;
        }
        // Checking description was filled
        if (Utils.isTextViewEmpty(binding.etEventDescription)) {
            binding.etEventDescription.setError(emptyFieldErrorMessage);
            isValid = false;
        }
        // Checking price was filled
        if (Utils.isTextViewEmpty(binding.etEventPrice)) {
            binding.etEventPrice.setError(emptyFieldErrorMessage);
            isValid = false;
        }
        // If the event is not an event with marked seats, checking max capacity was filled and bigger than 0
        if (!binding.cbEventMarkedSeats.isChecked() &&
                (Utils.isTextViewEmpty(binding.etEventMaxCapacity) ||
                        binding.etEventMaxCapacity.getText().toString().equals("0"))) {
            binding.etEventMaxCapacity.setError(emptyFieldErrorMessage);
            isValid = false;
        }
        // Checking poster file was uploaded
        if (eventPosterUri == null) {
            binding.btLoadPoster.setError(posterErrorMessage);
            isValid = false;
        }
        // If producer entered a coupon code for the event
        if (!Utils.isTextViewEmpty(binding.etEventCouponCode)) {
            // Discounted price must be filled accordingly
            if (Utils.isTextViewEmpty(binding.etEventDiscountedPrice)) {
                binding.etEventDiscountedPrice.setError(invalidDiscountedPrice);
                isValid = false;
            }
            // Discounted price should not be higher the original price
            else if (Integer.parseInt(binding.etEventDiscountedPrice.getText().toString()) >
                     Integer.parseInt(binding.etEventPrice.getText().toString())) {
                binding.etEventDiscountedPrice.setError(invalidDiscountedPrice);
                isValid = false;
            }
        }

        return isValid;
    }

    private void fireHallDateUniqueCheck(String hallUid) {
        hallEventDatesDatabaseReference
                .child(hallUid)
                .child(DataUtils.convertToDbDateFormat(binding.etEventDate.getText().toString()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String hallDateUniqueErrorMessage = "האולם הנבחר אינו פנוי בתאריך זה.";

                // If we have a non-null result, the hall is already occupied on this date
                if (dataSnapshot != null && dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    binding.etEventDate.setError(hallDateUniqueErrorMessage);
                    Toast.makeText(EventEditActivity.this, hallDateUniqueErrorMessage, Toast.LENGTH_LONG).show();
                    return;
                }

                // If we reached here then all input validations are done, we can save the event
                if (eventEditMode == EventEditMode.EXISTING_EVENT)
                    updateExistingEventAndExit();
                else
                    saveNewEventAndExit();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void saveNewEventAndExit() {
        final String newEventUid = eventsDatabaseReference.push().getKey();
        Event newEvent = createEventFromUI(newEventUid);
        eventsDatabaseReference.child(newEventUid).setValue(newEvent);

        // If the new event is with marked seats, create event seat objects in firebase
        if (newEvent.isMarkedSeats())
            createEventSeats(newEvent.getEventHall().getUid(), newEventUid);

        // We use a less detailed event for the indexed tables
        Event diminishedEvent = getDiminishedEventFromEvent(newEvent);

        // add "date_events / $ date / $ newEvent
        updateEventInIndexedTable(dateEventsDatabaseReference, newEvent.getDate(), diminishedEvent);

        // add "city_events / $ eventCity / $newEvent
        updateEventInIndexedTable(cityEventsDatabaseReference, newEvent.getCity(), diminishedEvent);

        // add "hall_events / $ hallUid / $ newEvent
        updateEventInIndexedTable(hallEventsDatabaseReference, newEvent.getEventHall().getUid(), diminishedEvent);

        // add "category_events / $ eventCategory / $ newEvent
        updateEventInIndexedTable(categoryEventsDatabaseReference, newEvent.getCategory(), diminishedEvent);

        // add "category_date_events / $ eventCategory_eventDate / $ newEvent
        updateEventInIndexedTable(categoryDateEventsDatabaseReference,
                newEvent.getCategory() + INDEXED_KEY_DIVIDER + newEvent.getDate(), diminishedEvent);

        // add "category_city_events / $ eventCategory_eventCity / $ newEvent
        updateEventInIndexedTable(categoryCityEventsDatabaseReference,
                newEvent.getCategory() + INDEXED_KEY_DIVIDER + newEvent.getCity(), diminishedEvent);

        // add "category_hall_events / $ eventCategory_eventHallUid / $ newEvent
        updateEventInIndexedTable(categoryHallEventsDatabaseReference,
                newEvent.getCategory() + INDEXED_KEY_DIVIDER + newEvent.getEventHall().getUid(), diminishedEvent);

        // add "hall_eventDates / $ newHallId / $ newEventDate"
        hallEventDatesDatabaseReference
                .child(newEvent.getEventHall().getUid())
                .child(newEvent.getDate())
                .setValue(true);


        Toast.makeText(this, "המופע נשמר בהצלחה!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, EventsActivity.class));
    }

    private void updateExistingEventAndExit() {
        final String eventUid = this.editedEvent.getUid();
        Event updatedEvent = createEventFromUI(eventUid);
        eventsDatabaseReference.child(eventUid).setValue(updatedEvent);

        // In some cases we will need to update event seats
        updateEventSeatsIfNeeded(updatedEvent);

        // We use a less detailed event for the indexed tables
        Event diminishedEvent = getDiminishedEventFromEvent(updatedEvent);

        // If the event category was changed, we need to update "category_events" and all other
        // category-indexed "tables" accordingly.
        // First stage would be to remove the event from the old category
        // Second stage would be to add the event to the new category in all index tables, which should happen
        // anyway (since we need to update the saved event with the new details)
        if (!editedEvent.getCategoryAsEnum().equals(updatedEvent.getCategoryAsEnum())) {
            // remove "category_events / $ oldCategory / $ oldEvent"
            removeEventFromIndexedTable(categoryEventsDatabaseReference, editedEvent.getCategory(), eventUid);
            // remove "category_date_events / $ oldCategory_oldDate / $ oldEvent
            removeEventFromIndexedTable(categoryDateEventsDatabaseReference,
                    editedEvent.getCategory() + INDEXED_KEY_DIVIDER + editedEvent.getDate(), eventUid);
            // remove "category_hall_events / $ oldCategory_oldHallUid / $ oldEvent
            removeEventFromIndexedTable(categoryHallEventsDatabaseReference,
                    editedEvent.getCategory() + INDEXED_KEY_DIVIDER + editedEvent.getEventHall().getUid(), eventUid);
            // remove "category_city_events / $ oldCategory_oldCity / $ oldEvent
            removeEventFromIndexedTable(categoryCityEventsDatabaseReference,
                    editedEvent.getCategory() + INDEXED_KEY_DIVIDER + editedEvent.getCity(), eventUid);
        }

        // If the event hall was changed, we need to update "hall_events" and "city_events" + their category index
        // tables accordingly
        // First stage would be to remove the event from the old hall and old city
        // Second stage would be to add the event to the new hall and new city in all index tables, which should
        // happen anyway (since we need to update the saved event with the new details)
        if (!editedEvent.getEventHall().getUid().equals(updatedEvent.getEventHall().getUid())) {
            // remove "hall_events / $ oldHallUid / $ oldEvent"
            removeEventFromIndexedTable(hallEventsDatabaseReference, editedEvent.getEventHall().getUid(), eventUid);
            // remove "city_events / $ oldCity / $ oldEvent"
            removeEventFromIndexedTable(cityEventsDatabaseReference, editedEvent.getCity(), eventUid);
            // remove "category_hall_events / $ oldCategory_oldHallUid / $ oldEvent
            removeEventFromIndexedTable(categoryHallEventsDatabaseReference,
                    editedEvent.getCategory() + INDEXED_KEY_DIVIDER + editedEvent.getEventHall().getUid(), eventUid);
            // remove "category_city_events / $ oldCategory_oldCity / $ oldEvent
            removeEventFromIndexedTable(categoryCityEventsDatabaseReference,
                    editedEvent.getCategory() + INDEXED_KEY_DIVIDER + editedEvent.getCity(), eventUid);

            // If the event hall or event date were changed, we need to update "hall_eventDates" accordingly
            updateHallEventDate(editedEvent.getEventHall().getUid(), updatedEvent.getEventHall().getUid(),
                    editedEvent.getDate(), updatedEvent.getDate());
        }

        // If the event date was changed, we need to update "date_events" and its category index table accordingly
        // First stage would be to remove the event from the old date
        // Second stage would be to add the event to the new date in all index tables, which should happen
        // anyway (since we need to update the saved event with the new details)
        if (!editedEvent.getDate().equals(updatedEvent.getDate())) {
            // remove "date_events / $ oldDate / $oldEvent
            removeEventFromIndexedTable(dateEventsDatabaseReference, editedEvent.getDate(), eventUid);
            // remove "category_date_events / $ oldCategory_oldDate / $ oldEvent
            removeEventFromIndexedTable(categoryDateEventsDatabaseReference,
                    editedEvent.getCategory() + INDEXED_KEY_DIVIDER + editedEvent.getDate(), eventUid);

            // If the event hall or event date were changed, we need to update "hall_eventDates" accordingly
            updateHallEventDate(editedEvent.getEventHall().getUid(), updatedEvent.getEventHall().getUid(),
                    editedEvent.getDate(), updatedEvent.getDate());
        }

        // update "category_events / $ eventCategory / $ event"
        updateEventInIndexedTable(categoryEventsDatabaseReference, updatedEvent.getCategory(), diminishedEvent);
        // update "date_events / $ eventDate / $ event"
        updateEventInIndexedTable(dateEventsDatabaseReference, updatedEvent.getDate(), diminishedEvent);
        // update "hall_events / $ eventHallUid / $ event"
        updateEventInIndexedTable(hallEventsDatabaseReference, updatedEvent.getEventHall().getUid(), diminishedEvent);
        // update "city_events / $ eventCity / $ event"
        updateEventInIndexedTable(cityEventsDatabaseReference, updatedEvent.getCity(), diminishedEvent);
        // update "category_date_events / $ eventCategory_eventDate / $ newEvent
        updateEventInIndexedTable(categoryDateEventsDatabaseReference,
                updatedEvent.getCategory() + INDEXED_KEY_DIVIDER + updatedEvent.getDate(), diminishedEvent);
        // update "category_city_events / $ eventCategory_eventCity / $ newEvent
        updateEventInIndexedTable(categoryCityEventsDatabaseReference,
                updatedEvent.getCategory() + INDEXED_KEY_DIVIDER + updatedEvent.getCity(), diminishedEvent);
        // update "category_hall_events / $ eventCategory_eventHallUid / $ newEvent
        updateEventInIndexedTable(categoryHallEventsDatabaseReference,
                updatedEvent.getCategory() + INDEXED_KEY_DIVIDER + updatedEvent.getEventHall().getUid(), diminishedEvent);

        Toast.makeText(this, "השינויים נשמרו בהצלחה!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, EventsActivity.class));
    }

    private Event createEventFromUI(String eventUid) {
        String newEventTitle = binding.etEventTitle.getText().toString();
        DataUtils.Category newEventCategory = (DataUtils.Category) binding.spEventCategory.getSelectedItem();

        final Hall selectedHall = (Hall) binding.spEventHall.getSelectedItem();
        EventHall newEventHall =
                new EventHall(selectedHall.getUid(),
                        selectedHall.getName(),
                        selectedHall.getRows(),
                        selectedHall.getColumns());

        String newEventCity = selectedHall.getCity();
        String newEventDate = DataUtils.convertToDbDateFormat(binding.etEventDate.getText().toString());
        String newEventHour = binding.etEventHour.getText().toString();

        int newEventDuration = TextUtils.isEmpty(binding.etEventDuration.getText())
                ? 0
                : Integer.parseInt(binding.etEventDuration.getText().toString());
        String newEventDescription = binding.etEventDescription.getText().toString();
        String newEventPerformer = binding.etEventPerformer.getText().toString();
        int newEventPrice = Integer.parseInt(binding.etEventPrice.getText().toString());
        int newEventCouponCode = 0;
        int newEventDiscountedPrice = 0;

        if (!Utils.isTextViewEmpty(binding.etEventCouponCode)) {
            newEventCouponCode = Integer.parseInt(binding.etEventCouponCode.getText().toString());
            newEventDiscountedPrice = Integer.parseInt(binding.etEventDiscountedPrice.getText().toString());
        }

        String newEventPosterUri = eventPosterUri;
        boolean newEventIsMarkedSeats = binding.cbEventMarkedSeats.isChecked();
        int newEventMaxCapacity = newEventIsMarkedSeats
                ? 0
                : Integer.parseInt(binding.etEventMaxCapacity.getText().toString());
        String newEventProducerId = EventEditActivity.super.user.getUid();
        return new Event(eventUid, newEventTitle, newEventCategory, newEventHall, newEventCity,
                newEventDate, newEventHour, newEventDuration, newEventDescription, newEventPerformer,
                newEventPrice, newEventCouponCode, newEventDiscountedPrice, newEventPosterUri,
                newEventIsMarkedSeats, newEventMaxCapacity, newEventProducerId);
    }

    private Event getDiminishedEventFromEvent(Event event) {
        return new Event(event.getUid(), event.getTitle(), event.getCategoryAsEnum(), event.getEventHall(),
                event.getCity(), event.getDate(), event.getHour(), event.getDuration(), event.getPrice(),
                event.getPosterUri(), event.isMarkedSeats(), event.getMaxCapacity());
    }

    // Used for updating event seats when editing an existing event
    private void updateEventSeatsIfNeeded(Event updatedEvent) {
        // If the event hall was changed, update its seats (according to event's marked-seats property)
        if (!editedEvent.getEventHall().getUid().equals(updatedEvent.getEventHall().getUid())) {
            if (updatedEvent.isMarkedSeats())
                createEventSeats(updatedEvent.getEventHall().getUid(), updatedEvent.getUid());
            else
                eventSeatsDatabaseReference.child(updatedEvent.getUid()).removeValue();
        }

        // If the event was an event with no marked seats before, and now it should have marked seats
        // Create event seat objects in database
        if (!editedEvent.isMarkedSeats() && updatedEvent.isMarkedSeats())
            createEventSeats(updatedEvent.getEventHall().getUid(), updatedEvent.getUid());
        // If the event was an event with marked seats before, and now it shouldn't have marked seats
        // Delete the already existing event seats from database
        else if (editedEvent.isMarkedSeats() && !updatedEvent.isMarkedSeats())
            eventSeatsDatabaseReference.child(updatedEvent.getUid()).removeValue();
    }

    private void createEventSeats(final String hallUid, final String eventUid) {
        // Clearing whatever formation of seats that used to be for the event beforehand (if any)
        eventSeatsDatabaseReference.child(eventUid).removeValue();

        hallSeatsDatabaseReference.child(hallUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Map eventSeatsData = new HashMap();

                            // Going through all of hall's rows
                            for (DataSnapshot rowSnapshot : dataSnapshot.getChildren()) {
                                // Going through all row's seats
                                for (DataSnapshot seatSnapshot : rowSnapshot.getChildren()) {
                                    // Add: "event_seats / $ eventUid / $ hallRow / $ rowSeat / false"
                                    // Which means the seat (and row) will be created in nested fashion,
                                    // with "false" value for occupied since all seats should be free at this point
                                    eventSeatsData.put(
                                            String.format("%s/%s", rowSnapshot.getKey(), seatSnapshot.getKey()),
                                            false);
                                }
                            }

                            eventSeatsDatabaseReference.child(eventUid).updateChildren(eventSeatsData);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
    }

    private void removeEventFromIndexedTable(DatabaseReference table, String index, String eventUid) {
        table.child(index).child(eventUid).removeValue();
    }

    private void updateEventInIndexedTable(DatabaseReference table, String index, Event event) {
        table.child(index).child(event.getUid()).setValue(event);
    }

    private void updateHallEventDate(String oldHallUid, String newHallUid, String oldDate, String newDate) {
        // remove "halls_eventDates / $ oldHallId / $ oldEventDate"
        hallEventDatesDatabaseReference.child(oldHallUid).child(oldDate).removeValue();
        // add "halls_eventDates / $ newHallId / $ newEventDate"
        hallEventDatesDatabaseReference.child(newHallUid).child(newDate).setValue(true);
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            progressDialog = ProgressDialog.show(EventEditActivity.this, "רק רגע",
                    "המתן להעלאת הפוסטר...", true, false);

            Uri selectedImageUri = data.getData();

            MediaManager.get().upload(selectedImageUri)
                    .unsigned(Utils.CLOUDINARY_PRESET)
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {}

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {}

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            loadEventPoster(Uri.parse(resultData.get("url").toString()).getLastPathSegment());
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {}

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {}
                    })
                    .dispatch();
        }
    }

    public void onMarkedSeatsClicked(View view) {
        if (view.getId() == R.id.cb_event_marked_seats)
            changeMaxCapacityVisibility(((CheckBox) view).isChecked());
    }

    private void changeMaxCapacityVisibility(boolean isMarkedSeats) {
        if (isMarkedSeats) {
            binding.tvEventMaxCapacity.setVisibility(View.GONE);
            binding.etEventMaxCapacity.setVisibility(View.GONE);
        } else {
            binding.tvEventMaxCapacity.setVisibility(View.VISIBLE);
            binding.etEventMaxCapacity.setVisibility(View.VISIBLE);
        }
    }

    private void loadEventPoster(String photoUri) {
        binding.ivEventPoster.setVisibility(View.VISIBLE);
        Glide.with(binding.ivEventPoster.getContext())
                .load(Utils.getTransformedCloudinaryImageUrl(50, 50, photoUri, "thumb"))
                .into(binding.ivEventPoster);
        eventPosterUri = photoUri;
    }
}