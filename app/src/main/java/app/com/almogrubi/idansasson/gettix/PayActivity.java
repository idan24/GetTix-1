package app.com.almogrubi.idansasson.gettix;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.entities.Order;

/**
 * Created by almogrubi on 10/20/17.
 */

public class PayActivity extends AppCompatActivity {

    //objects from intent
    private Event event;
    private Order order;

    //setting buttons and view
    private TextView detailText;
    private EditText name;
    private EditText phone;
    private EditText email;
    private EditText credit;
    private EditText expiration;
    private EditText CVV;
    private Button next;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference eventsDatabaseReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        detailText = (TextView) findViewById(R.id.detail_text);
        email = (EditText) findViewById(R.id.email_edit_text);
        name = (EditText) findViewById(R.id.name_edit_text);
        phone = (EditText) findViewById(R.id.phone_edit_text);
        credit = (EditText) findViewById(R.id.credit_edit_text);
        expiration = (EditText) findViewById(R.id.expiration_edit_text);
        CVV = (EditText) findViewById(R.id.CVV_edit_text);


        // Initialization of all needed Firebase database references
        initializeDatabaseReferences();

        Intent intent = this.getIntent();
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

        }

        //TODO fix price query
        event.setPrice(10);

        Log.i("almog", String.valueOf(order.getTicketsNum()));
        //dosnt get event price
        Log.i("almog", String.valueOf(event.getPrice()));

        detailText.setText(String.format("רכישת %d כרטיסים: %d ₪", order.getTicketsNum(),order.getTicketsNum()*event.getPrice()));

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(v.getContext(), FinishingActivity.class);

                order.setCustomerName(name.getText().toString());
                order.setCustomerPhone(phone.getText().toString());
                order.setCustomerEmail(email.getText().toString());
                order.setCustomerCreditCard(credit.getText().toString());

                intent.putExtra("orderObject", order);
                intent.putExtra("eventUid", event.getUid());
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
