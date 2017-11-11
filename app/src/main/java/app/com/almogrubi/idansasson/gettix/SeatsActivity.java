package app.com.almogrubi.idansasson.gettix;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.entities.Order;

/**
 * Created by almogrubi on 10/14/17.
 */

public class SeatsActivity extends AppCompatActivity{


    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference eventsDatabaseReference;

    private Event event;
    private Order order = new Order();
    private int rows = 0;
    private int columns;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Initialization of all needed Firebase database references
        initializeDatabaseReferences();

        Intent intent = getIntent();
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

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            abort();
                        }
                    });


            // TODO: uncomment and replace with query
            //List<EventSeat> seatsList = new ArrayList<EventSeat>(event.getEventHall().getEventSeat().values());



            rows =  event.getEventHall().getRows();
            rows =  event.getEventHall().getColumns();

            for (int i = 0; i < rows; i++) {
                LinearLayout row = new LinearLayout(this);
                row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    //change to size in %
                for (int j = 0; j < columns; j++) {

                    Button b = new Button(this);
                    b.setLayoutParams(new LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.WRAP_CONTENT));
                    // TODO: uncomment after replacing with query
                    //b.setTag(seatsList.get(i+j*columns));
                    b.setText("" + (j + 1 + (i * 10)));
                    b.setId(j + 1 + (i * 10));
                    row.addView(b);

                    b.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
//                                Intent intent = new Intent(v.getContext(), SeatsActivity.class);
//                                intent.putExtra("eventObject", event);
//                                startActivity(intent);
                                ((Button) v).setText("*");
                                ((Button) v).setEnabled(false);

                                Log.i("almog", "id is " + v.getId());

                                order.setTicketsNum(order.getTicketsNum()+1);


                            }
                        }

                    );
                }

                layout.addView(row);
            }
            setContentView(layout);
            //setContentView(R.layout.main);
        }

        else{ abort(); }


        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        Button next = new Button(this);
        next.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        next.setText("next");
        next.setId(0);
        row.addView(next);
        layout.addView(row);
        setContentView(layout);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), PaymentActivity.class);
                intent.putExtra("orderObject", order);
                intent.putExtra("eventUid", event.getUid());
                startActivity(intent);
                Log.i("almog", "id is " + v.getId());

//                event.choseSit(v.getUid());

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

        Toast.makeText(this, eventNotFoundErrorMessage, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
    }

}