package app.com.almogrubi.idansasson.gettix;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import app.com.almogrubi.idansasson.gettix.databinding.ActivityConfirmationBinding;
import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.entities.Hall;
import app.com.almogrubi.idansasson.gettix.entities.Order;
import app.com.almogrubi.idansasson.gettix.utilities.DataUtils;
import app.com.almogrubi.idansasson.gettix.utilities.Utils;


public class ConfirmationActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference eventsDatabaseReference;
    private DatabaseReference hallsDatabaseReference;
    private DatabaseReference orderSeatsDatabaseReference;

    private ActivityConfirmationBinding binding;
    private Event event;
    private Order order;
    private boolean[][] orderSeats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_confirmation);

        // Initialize all needed Firebase database references
        firebaseDatabase = FirebaseDatabase.getInstance();
        eventsDatabaseReference = firebaseDatabase.getReference().child("events");
        hallsDatabaseReference = firebaseDatabase.getReference().child("halls");
        orderSeatsDatabaseReference = firebaseDatabase.getReference().child("order_seats");

        Toast.makeText(this, "הזמנתך התקבלה בהצלחה!", Toast.LENGTH_LONG).show();

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
                                return;
                            }

                            // If we reached here then the existing event was found, we'll bind it to UI
                            event = dataSnapshot.getValue(Event.class);
                            bindEventToUI(event);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });

            if (intent.hasExtra("orderObject")) {
                this.order = (Order) intent.getSerializableExtra("orderObject");
                bindOrderToUI(order);
            }
            if (intent.hasExtra("orderSeats")) {
                String[][] orderSeatsStrings = (String[][]) intent.getSerializableExtra("orderSeats");

                orderSeats = new boolean[orderSeatsStrings.length][orderSeatsStrings[0].length];
                for (int i = 0; i < orderSeatsStrings.length; i++)
                    for (int j = 0; j < orderSeatsStrings[0].length; j++)
                        orderSeats[i][j] = Boolean.parseBoolean(orderSeatsStrings[i][j]);

                binding.tvChosenSeats.setText(Utils.generateOrderSeatsUIString(this.orderSeats));
            }
            else {
                binding.tvChosenSeats.setVisibility(View.GONE);
            }
        }

        binding.btGotoHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainActivity = new Intent(ConfirmationActivity.this, MainActivity.class);
                startActivity(mainActivity);
            }
        });
    }

    private void bindEventToUI(final Event event) {

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
                    public void onCancelled(DatabaseError databaseError) {}
                });

        binding.ivEventCategory.setBackgroundResource(Utils.lookupImageByCategory(event.getCategoryAsEnum()));
    }

    private void bindOrderToUI(final Order order) {
        binding.tvOrderNumber.setText("מספר הזמנה: " + order.getConfirmationNumber());
        binding.tvTicketsNum.setText(order.getTicketsNum() + " כרטיסים");
        binding.tvTotalPrice.setText(String.format("סכום כולל: %d ₪", order.getTotalPrice()));
        binding.tvCustomerName.setText(order.getCustomer().getName());
        binding.tvCustomerPhone.setText(order.getCustomer().getPhone());
        binding.tvCustomerEmail.setText(order.getCustomer().getEmail());
    }
}
