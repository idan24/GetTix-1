package app.com.almogrubi.idansasson.gettix;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import app.com.almogrubi.idansasson.gettix.databinding.ActivitySeatsBinding;
import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.entities.Order;
import app.com.almogrubi.idansasson.gettix.utilities.DataUtils;
import app.com.almogrubi.idansasson.gettix.utilities.SeatImageView;
import app.com.almogrubi.idansasson.gettix.utilities.Utils;

/**
 * Created by almogrubi on 10/14/17.
 */

public class SeatsActivity extends AppCompatActivity{

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference eventsDatabaseReference;
    private DatabaseReference eventSeatsDatabaseReference;
    private DatabaseReference ordersDatabaseReference;
    private DatabaseReference orderSeatsDatabaseReference;

    private Event event;
    private int ticketsNum = 0;
    private boolean[][] chosenSeats;
    private boolean isCouponUsed = false;

    private ActivitySeatsBinding binding;
    private TextView tvCouponCode;
    private EditText etCouponCode;
    private Button btCheckCoupon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seats);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_seats);
        tvCouponCode = binding.couponBox.findViewById(R.id.tv_coupon_code);
        etCouponCode = binding.couponBox.findViewById(R.id.et_coupon_code);
        btCheckCoupon = binding.couponBox.findViewById(R.id.bt_check_coupon);

        // Initialization of all needed Firebase database references
        initializeDatabaseReferences();

        Intent intent = this.getIntent();
        // Lookup the event in the database and bind its data to UI
        if ((intent != null) && (intent.hasExtra("eventUid"))) {
            String eventUid = intent.getStringExtra("eventUid");
            eventsDatabaseReference
                    .child(eventUid)
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
                            binding.tvEventTitle.setText(event.getTitle());

                            // Initializing the array of chosen rows and seats
                            // false value being set by default for "chosen" indication
                            chosenSeats =
                                    new boolean[event.getEventHall().getRows()][event.getEventHall().getColumns()];

                            updateTicketsNumUI();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            abort();
                        }
                    });

            createSeatsUI(eventUid);

        } else {
            abort();
        }

        btCheckCoupon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEnteredCouponCodeValid())
                    onValidCouponEntered();
            }
        });

        binding.btNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if no seats were chosen, order cannot be placed
                if (ticketsNum == 0) {
                    Toast.makeText(SeatsActivity.this, "יש לבחור לפחות מושב אחד!", Toast.LENGTH_LONG).show();
                    return;
                }

                // Create a new order in database
                final String newOrderUid = ordersDatabaseReference.push().getKey();
                Order newOrder = createNewOrderFromUI(newOrderUid);
                // orders / $ eventUid / $ newOrderUid / newOrder
                ordersDatabaseReference.child(event.getUid()).child(newOrderUid).setValue(newOrder);

                // Update order's event's leftTicketsNum (and soldOut if necessary)
                updateEventTicketsNum(newOrder.getTicketsNum());

                // Handle order seats save
                saveNewOrderSeats(newOrderUid, event.getUid());

                // We create a service to return the tickets if after 10 min order is
                // not finished
                fireCancelOrderService(newOrder);

                // Proceed to PaymentActivity
                proceedToPayment(v.getContext(), newOrder);
            }
        });
    }

    private void proceedToPayment(Context context, Order newOrder) {

        Intent paymentActivity = new Intent(context, PaymentActivity.class);
        paymentActivity.putExtra("eventUid", event.getUid());
        paymentActivity.putExtra("eventTitle", event.getTitle());
        paymentActivity.putExtra("eventMarkedSeats", true);
        paymentActivity.putExtra("orderObject", newOrder);

        String[][] chosenSeatsStrings = new String[chosenSeats.length][chosenSeats[0].length];
        for (int i = 0; i < chosenSeats.length; i++)
            for (int j = 0; j < chosenSeats[0].length; j++)
                chosenSeatsStrings[i][j] = String.valueOf(chosenSeats[i][j]);

        paymentActivity.putExtra("orderSeats", chosenSeatsStrings);

        startActivity(paymentActivity);
    }

    private void updateEventTicketsNum(int newOrderTicketsNum) {
        int newLeftTicketsNum = event.getLeftTicketsNum() - newOrderTicketsNum;
        Map newEventData = new HashMap();
        newEventData.put("leftTicketsNum", newLeftTicketsNum);

        if (newLeftTicketsNum == 0) {
            newEventData.put("soldOut", true);
        }

        eventsDatabaseReference.child(event.getUid()).updateChildren(newEventData);
    }

    private void initializeDatabaseReferences() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        eventsDatabaseReference = firebaseDatabase.getReference().child("events");
        eventSeatsDatabaseReference = firebaseDatabase.getReference().child("event_seats");
        ordersDatabaseReference = firebaseDatabase.getReference().child("orders");
        orderSeatsDatabaseReference = firebaseDatabase.getReference().child("order_seats");
    }

    private void abort() {
        String eventNotFoundErrorMessage = "המופע לא נמצא, נסה שנית";

        Toast.makeText(this, eventNotFoundErrorMessage, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, EventDetailsActivity.class));
    }

    private void createSeatsUI(String eventUid) {
        eventSeatsDatabaseReference
                .child(eventUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final int RECOMMENDED_DIMEN_FOR_10_SEATS = 85;

                        if (dataSnapshot.exists()) {
                            // Going through all of event hall's rows
                            ArrayList<DataSnapshot> eventRows =
                                    Utils.toArrayList(dataSnapshot.getChildren());
                            for (int i = eventRows.size() - 1; i >= 0; i--) {
                                DataSnapshot rowSnapshot = eventRows.get(i);
                                String rowNumber =  rowSnapshot.getKey();

                                // Create a new row to be added
                                TableRow tr = new TableRow(SeatsActivity.this);
                                tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                                tr.setGravity(Gravity.CENTER);

                                // Create left/right row number view
                                TextView tvRowNumber = new TextView(SeatsActivity.this);
                                tvRowNumber.setTextSize(R.dimen.common_text_size);
                                tvRowNumber.setText(rowNumber);
                                tvRowNumber.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

                                // Add left tvRowNumber to row
                                tr.addView(tvRowNumber);

                                // Go through all row's seats
                                ArrayList<DataSnapshot> rowSeats =
                                        Utils.toArrayList(rowSnapshot.getChildren());
                                final int DIMEN_FOR_SEAT =
                                        RECOMMENDED_DIMEN_FOR_10_SEATS / (rowSeats.size() / 10);
                                for (int j = rowSeats.size() - 1; j >= 0; j--) {
                                    DataSnapshot seatSnapshot = rowSeats.get(j);
                                    String seatNumber = seatSnapshot.getKey();
                                    boolean isSeatTaken = (boolean) seatSnapshot.getValue();

                                    // This is the new seat image to be added to UI
                                    SeatImageView newSeatImageView = new SeatImageView(
                                            SeatsActivity.this,
                                            Integer.parseInt(rowNumber),
                                            Integer.parseInt(seatNumber),
                                            isSeatTaken
                                                    ? Utils.SeatStatus.OCCUPIED
                                                    : Utils.SeatStatus.AVAILABLE);

                                    // Setting seat color depending on its occupied/available status
                                    newSeatImageView.setBackgroundResource(
                                            Utils.lookupImageBySeatStatus(newSeatImageView.getStatus()));

                                    // If the seat is occupied, it shouldn't be enabled for the user
                                    if (isSeatTaken) {
                                        newSeatImageView.setClickable(false);
                                    }
                                    // If the seat is available
                                    else {
                                        newSeatImageView.setClickable(true);
                                        newSeatImageView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                SeatImageView clickedSeat = (SeatImageView) v;

                                                // Setting seat's new status as the reverse of its previous status
                                                Utils.SeatStatus newSeatStatus =
                                                        clickedSeat.getStatus() == Utils.SeatStatus.AVAILABLE
                                                                ? Utils.SeatStatus.CHOSEN
                                                                : Utils.SeatStatus.AVAILABLE;
                                                clickedSeat.setStatus(newSeatStatus);

                                                // Updating seat color according to new status (chosen/available)
                                                clickedSeat.setBackgroundResource(
                                                        Utils.lookupImageBySeatStatus(newSeatStatus));

                                                // Handle class lists and UI-side text
                                                updateChosenSeatsAndUI(clickedSeat);
                                            }
                                        });
                                    }

                                    // Add seat image to row
                                    tr.addView(newSeatImageView);

                                    newSeatImageView.getLayoutParams().height = DIMEN_FOR_SEAT;
                                    newSeatImageView.getLayoutParams().width = DIMEN_FOR_SEAT;
                                    newSeatImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                                    newSeatImageView.setAdjustViewBounds(true);
                                }

                                // Add row to TableLayout
                                binding.tlEventSeats.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        abort();
                    }
                });
    }

    private void updateChosenSeatsAndUI(SeatImageView clickedSeat) {
        // If the seat's new status is chosen, increment tickets num and chosen seats list
        if (clickedSeat.getStatus() == Utils.SeatStatus.CHOSEN) {
            this.chosenSeats[clickedSeat.getRow() - 1][clickedSeat.getNumber() - 1] = true;
            this.ticketsNum++;
        }
        // If the seat's new status is available, decrement tickets num and remove from chosen seats list
        else {
            this.chosenSeats[clickedSeat.getRow() - 1][clickedSeat.getNumber() - 1] = false;
            this.ticketsNum--;
        }

        // Handle tickets num + total price text view
        updateTicketsNumUI();

        // If there are no currently chosen seats, hide chosen seats text
        if (ticketsNum == 0)
            binding.tvChosenSeats.setVisibility(View.INVISIBLE);
        // If there are currently chosen seats, show chosen seats text and handle its contents
        else {
            binding.tvChosenSeats.setVisibility(View.VISIBLE);
            binding.tvChosenSeats.setText(Utils.generateOrderSeatsUIString(this.chosenSeats));
        }
    }

    private void updateTicketsNumUI() {
        if (ticketsNum == 0) {
            binding.tvFriendlyTicketsNum.setText("לא נבחרו מושבים");
            binding.tvFriendlyTicketsNum.setTextSize(16);
            binding.ivSeatChosen.setVisibility(View.INVISIBLE);
            binding.tvSeatChosen.setVisibility(View.INVISIBLE);
        }
        else {
            int newTotalPrice = isCouponUsed
                    ? ticketsNum * event.getDiscountedPrice()
                    : ticketsNum * event.getPrice();
            binding.tvFriendlyTicketsNum.setText(String.format("נבחרו %d מושבים: %d ₪", ticketsNum, newTotalPrice));
            binding.tvFriendlyTicketsNum.setTextSize(22);
            binding.ivSeatChosen.setVisibility(View.VISIBLE);
            binding.tvSeatChosen.setVisibility(View.VISIBLE);
        }
    }

    private boolean isEnteredCouponCodeValid() {
        final String emptyFieldErrorMessage = "יש למלא קוד קופון";
        final String wrongCodeErrorMessage = "קוד זה אינו תקף עבור המופע";

        if (Utils.isTextViewEmpty(etCouponCode)) {
            etCouponCode.setError(emptyFieldErrorMessage);
            return false;
        }

        int enteredCode = Integer.parseInt(etCouponCode.getText().toString());

        // If the event does not offer any coupons or the entered coupon code is wrong
        if ((this.event.getCouponCode() == 0) || (enteredCode != this.event.getCouponCode())) {
            etCouponCode.setError(wrongCodeErrorMessage);
            return false;
        }

        return true;
    }

    private void onValidCouponEntered() {
        final String validCouponMessage = "הקופון הוזן בהצלחה!";
        Toast.makeText(SeatsActivity.this, validCouponMessage, Toast.LENGTH_LONG).show();

        // Should be set before updating tickets num UI
        isCouponUsed = true;

        updateTicketsNumUI();

        tvCouponCode.setTextColor(Color.GRAY);
        etCouponCode.setEnabled(false);
        btCheckCoupon.setEnabled(false);
        btCheckCoupon.setBackgroundColor(Color.LTGRAY);
        btCheckCoupon.setTextColor(Color.GRAY);
    }

    private Order createNewOrderFromUI(String orderUid) {
        int newOrderTotalPrice = isCouponUsed
                ? ticketsNum * event.getDiscountedPrice()
                : ticketsNum * event.getPrice();

        return new Order(orderUid, ticketsNum, isCouponUsed, newOrderTotalPrice,
                DataUtils.OrderStatus.IN_PROGRESS);
    }

    private void saveNewOrderSeats(String newOrderUid, String orderEventUid) {
        Map orderSeatsData = new HashMap();

        // For every row
        for (int i = 0; i < this.chosenSeats.length; i++) {
            // For every seat from the row
            for (int j = 0; j < this.chosenSeats[i].length; j++)
                if (this.chosenSeats[i][j])
                    orderSeatsData.put(String.format("%d/%d", i+1, j+1), true);
        }

        orderSeatsDatabaseReference.child(newOrderUid).updateChildren(orderSeatsData);
        eventSeatsDatabaseReference.child(orderEventUid).updateChildren(orderSeatsData);
    }

    private void fireCancelOrderService(Order order) {

        // Create a new dispatcher using the Google Play driver.
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(SeatsActivity.this));

        Bundle cancelOrderJobServiceExtrasBundle = new Bundle();
        cancelOrderJobServiceExtrasBundle.putString("order_uid", order.getUid());
        cancelOrderJobServiceExtrasBundle.putString("event_uid", event.getUid());
        cancelOrderJobServiceExtrasBundle.putBoolean("event_marked_seats", event.isMarkedSeats());

        Job myJob = dispatcher.newJobBuilder()
                // the JobService that will be called
                .setService(CancelOrderJobService.class)
                // uniquely identifies the job
                .setTag("cancel-order-" + order.getUid())
                // one-off job
                .setRecurring(false)
                // persist past a device reboot
                .setLifetime(Lifetime.FOREVER)
                // start in 10-11 minutes from now
                .setTrigger(Trigger.executionWindow(600, 660))
                //.setTrigger(Trigger.executionWindow(5, 15))
                // don't overwrite an existing job with the same tag
                .setReplaceCurrent(false)
                // retry with exponential backoff
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                // constraints that need to be satisfied for the job to run
                .setConstraints(
                        // run on any network
                        Constraint.ON_ANY_NETWORK
                )
                .setExtras(cancelOrderJobServiceExtrasBundle)
                .build();

        dispatcher.mustSchedule(myJob);
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