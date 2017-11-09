package app.com.almogrubi.idansasson.gettix;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.entities.Order;


public class NoSeatsActivity extends AppCompatActivity {

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

    private Event event;

    private TextView sitsNumber;
    private TextView details;
    private Button add;
    private Button remove;
    private Button next;
    private Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_sits);

        sitsNumber = (TextView) findViewById (R.id.sitsNumber);
        details = (TextView) findViewById (R.id.details);
        remove = (Button) findViewById (R.id.removeButton);
        add = (Button) findViewById (R.id.addButton);
        next = (Button) findViewById (R.id.next);

        Intent intent = this.getIntent();
        // Initialization of all needed Firebase database references
        initializeDatabaseReferences();

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



        order = new Order();

        add.setOnClickListener(new View.OnClickListener()
           {
               @Override
               public void onClick(View v){
                   order.setTicketsNum(order.getTicketsNum()+1);
                   sitsNumber.setText(String.valueOf(order.getTicketsNum()));

           }
        }

        );

        remove.setOnClickListener(new View.OnClickListener()
           {
                public void onClick(View v){
                    if (order.getTicketsNum()!=0){
                        order.setTicketsNum(order.getTicketsNum()-1);}
                        sitsNumber.setText(String.valueOf(order.getTicketsNum()));
                    }
           }

        );

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), PayActivity.class);
                intent.putExtra("eventUid", event.getUid());
                intent.putExtra("orderObject", order);

                startActivity(intent);

            }
        }

        );

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
