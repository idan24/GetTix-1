package app.com.almogrubi.idansasson.gettix;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.entities.Order;


public class FinishingActivity extends AppCompatActivity {


    private Event event;
    private Order order;
    private int counter;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference eventsDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finishing);

        // Initialization of all needed Firebase database references
        initializeDatabaseReferences();

        Intent intent = getIntent();
        if ((intent != null) && (intent.hasExtra("eventUid"))) {
            order = (Order) intent.getSerializableExtra("orderObject");
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


    private void initializeDatabaseReferences() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        eventsDatabaseReference = firebaseDatabase.getReference().child("events");
    }

    private void abort() {
        String eventNotFoundErrorMessage = "המופע לא נמצא, נסה שנית";

        Toast.makeText(this, eventNotFoundErrorMessage, Toast.LENGTH_SHORT);
        startActivity(new Intent(this, MainActivity.class));
    }

}
