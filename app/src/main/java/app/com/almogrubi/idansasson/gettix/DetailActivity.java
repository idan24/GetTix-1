package app.com.almogrubi.idansasson.gettix;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import app.com.almogrubi.idansasson.gettix.entities.Event;

public class DetailActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;
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
    private StorageReference eventPostersStorageReference;

    private ImageView imgTop;
    private Button pickSitsButton;
    private TextView showName;
    private TextView showDateAndTime;
    private TextView showLocation;
    private TextView description;

    private String eventUid;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        imgTop = (ImageView) findViewById(R.id.image_top);
        showName = (TextView) findViewById (R.id.show_name);
        showDateAndTime = (TextView) findViewById (R.id.show_date_and_time);
        showLocation = (TextView) findViewById (R.id.show_location);
        description = (TextView) findViewById (R.id.description);
        pickSitsButton = (Button) findViewById(R.id.button_pick_sits);


        // Initialization of all needed Firebase database references
        initializeDatabaseReferences();

        Intent intent = this.getIntent();

        if (intent != null && (intent.hasExtra("eventUid"))){
        Log.i("almog", "not null");
        Log.i("almog", intent.getStringExtra("eventUid"));

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

                            showName.setText(event.getTitle());
                            showDateAndTime.setText(event.getDate().toString());
                            showLocation.setText(event.getEventHall().getName());
                            description.setText(event.getDescription());

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            abort();
                        }
                    });


//            String imgName = event.getPosterUri().toString();
//
//            int idImg = this.getResources().getIdentifier(imgName, "mipmap",
//                    this.getPackageName());
//            imgTop.setImageResource(idImg);


        }

        pickSitsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                Intent intent;

                if (event.isMarkedSeats())
                    {
                    intent = new Intent(v.getContext(), NoSeatsActivity.class);
                        intent.putExtra("eventUid", eventUid);
                        startActivity(intent);

                    }
                if (!event.isMarkedSeats())
                    {
                    intent = new Intent(v.getContext(), SeatsActivity.class);
                    intent.putExtra("eventUid", eventUid);
                    startActivity(intent);
                    }
                }


        });

    }

    private void abort() {
        String eventNotFoundErrorMessage = "המופע לא נמצא, נסה שנית";

        Toast.makeText(this, eventNotFoundErrorMessage, Toast.LENGTH_SHORT);
        startActivity(new Intent(this, MainActivity.class));
    }

    private void initializeDatabaseReferences() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
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
        eventPostersStorageReference = firebaseStorage.getReference().child("event_posters");
    }

}