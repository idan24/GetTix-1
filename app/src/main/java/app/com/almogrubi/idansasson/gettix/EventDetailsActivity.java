package app.com.almogrubi.idansasson.gettix;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import app.com.almogrubi.idansasson.gettix.databinding.ActivityEventDetailsBinding;
import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.entities.Hall;
import app.com.almogrubi.idansasson.gettix.utilities.DataUtils;
import app.com.almogrubi.idansasson.gettix.utilities.Utils;

public class EventDetailsActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference eventsDatabaseReference;
    private DatabaseReference hallsDatabaseReference;

    private ActivityEventDetailsBinding binding;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_details);

        // Initialize all needed Firebase database references
        firebaseDatabase = FirebaseDatabase.getInstance();
        eventsDatabaseReference = firebaseDatabase.getReference().child("events");
        hallsDatabaseReference = firebaseDatabase.getReference().child("halls");

        Intent intent = this.getIntent();
        // Lookup the event in the database and bind its data to UI
        if ((intent != null) && (intent.hasExtra("eventUid"))) {
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
                            event = dataSnapshot.getValue(Event.class);
                            bindEventToUI(event);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            abort();
                        }
                    });
        } else {
            abort();
        }
    }

    private void bindEventToUI(final Event event) {

        Uri posterUri = Uri.parse(event.getPosterUri());
        Glide.with(binding.ivEventPoster.getContext())
                .load(posterUri)
                .into(binding.ivEventPoster);

//        MediaManager.get().url()
//                .transformation(new Transformation().width(100).height(150).crop("fill").gravity("faces"))
//                .generate("Banai288X245_part7_gnrs2c.jpg");

        binding.tvEventTitle.setText(Utils.createIndentedText(event.getTitle(),
                Utils.FIRST_LINE_INDENT, Utils.PARAGRAPH_INDENT));
        binding.tvEventDatetime.setText(String.format("%s בשעה %s",
                DataUtils.convertToUiDateFormat(event.getDate()),
                event.getHour()));

        hallsDatabaseReference
                .child(event.getEventHall().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // If we have a null result, the hall was somehow not found in the database
                        if (dataSnapshot == null || !dataSnapshot.exists() || dataSnapshot.getValue() == null) {
                            return;
                        }

                        // If we reached here then the hall was found, we'll bind it to UI
                        Hall hall = dataSnapshot.getValue(Hall.class);
                        String hallAddress = String.format("%s, %s, %s",
                                hall.getName(), hall.getAddress(), hall.getCity());
                        binding.tvEventHallAddress.setText(Utils.createIndentedText(hallAddress,
                                Utils.FIRST_LINE_INDENT, Utils.PARAGRAPH_INDENT));

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        abort();
                    }
                });

        binding.ivEventCategory.setBackgroundResource(Utils.lookupImageByCategory(event.getCategoryAsEnum()));

        binding.tvTicketsLeft.setText(String.format("נותרו %s כרטיסים למופע", event.getLeftTicketsNum()));
        binding.tvEventPrice.setText(String.format("%s₪", event.getPrice()));
        binding.tvEventDescription.setText(event.getDescription());

        binding.btBookTickets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If the event is with marked seats, go to seats picking activity
                if (event.isMarkedSeats()) {
                    Intent seatsActivity = new Intent(v.getContext(), SeatsActivity.class);
                    seatsActivity.putExtra("eventUid", event.getUid());
                    startActivity(seatsActivity);
                }
                // If the event is not seat-marked,
                else {
                    Intent noSeatsActivity = new Intent(v.getContext(), NoSeatsActivity.class);
                    noSeatsActivity.putExtra("eventUid", event.getUid());
                    startActivity(noSeatsActivity);
                }
            }
        });
    }

    private void abort() {
        String eventNotFoundErrorMessage = "המופע לא נמצא, נסה שנית";

        Toast.makeText(this, eventNotFoundErrorMessage, Toast.LENGTH_SHORT);
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}