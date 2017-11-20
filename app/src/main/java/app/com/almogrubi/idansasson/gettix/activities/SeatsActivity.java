package app.com.almogrubi.idansasson.gettix.activities;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import app.com.almogrubi.idansasson.gettix.R;
import app.com.almogrubi.idansasson.gettix.databinding.ActivitySeatsBinding;
import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.entities.Order;
import app.com.almogrubi.idansasson.gettix.dataservices.DataUtils;
import app.com.almogrubi.idansasson.gettix.dataservices.OrderDataService;
import app.com.almogrubi.idansasson.gettix.views.SeatImageView;
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
    private SeatImageView[][] seats;
    private boolean isCouponUsed = false;

    private ActivitySeatsBinding binding;
    private TextView tvCouponCode;
    private EditText etCouponCode;
    private Button btCheckCoupon;

    private boolean isOrderSaveInProgress = false;

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

                            // Initializing the array of rows and seats
                            seats =
                                new SeatImageView[event.getEventHall().getRows()][event.getEventHall().getColumns()];

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
                onNext();
            }
        });
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

        startActivity(new Intent(this, MainActivity.class));
    }

    private void onNext() {
        // if no seats were chosen, order cannot be placed
        if (ticketsNum == 0) {
            Toast.makeText(SeatsActivity.this, "יש לבחור לפחות מושב אחד!", Toast.LENGTH_LONG).show();
            return;
        }

        isOrderSaveInProgress = true;

        String[][] chosenSeatsStringArray = getChosenSeatsStringArray();

        // Create a new order in database
        final String newOrderUid = ordersDatabaseReference.push().getKey();
        Order newOrder = createNewOrderFromUI(newOrderUid);
        // orders / $ eventUid / $ newOrderUid / newOrder
        ordersDatabaseReference.child(event.getUid()).child(newOrderUid).setValue(newOrder);

        // Update order's event's leftTicketsNum (and soldOut if necessary) in DB
        updateEventTicketsNum(newOrder.getTicketsNum());

        // Handle order seats save
        // If seats are successfully saved, we can proceed with the rest of the actions and move to PaymentActivity
        // This will happen from the callback inside saveNewOrderSeats()
        // If seats are not successfully saved, order will be cancelled
        saveNewOrderSeats(newOrder, chosenSeatsStringArray);
    }

    private void proceedToPayment(Context context, Order newOrder, String[][] chosenSeatsStringArray) {

        Intent paymentActivity = new Intent(context, PaymentActivity.class);
        paymentActivity.putExtra("eventUid", event.getUid());
        paymentActivity.putExtra("eventTitle", event.getTitle());
        paymentActivity.putExtra("eventMarkedSeats", true);
        paymentActivity.putExtra("orderObject", newOrder);
        paymentActivity.putExtra("orderSeats", chosenSeatsStringArray);

        startActivity(paymentActivity);
    }

    private void updateEventTicketsNum(final int newOrderTicketsNum) {
        eventsDatabaseReference.child(event.getUid()).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Event event = mutableData.getValue(Event.class);
                if (event == null)
                    return Transaction.success(mutableData);

                int newLeftTicketsNum = event.getLeftTicketsNum() - newOrderTicketsNum;
                mutableData.child("leftTicketsNum").setValue(newLeftTicketsNum);

                if (newLeftTicketsNum <= 0)
                    mutableData.child("soldOut").setValue(true);

                // Report transaction success
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {}
        });
    }

    private void createSeatsUI(String eventUid) {
        eventSeatsDatabaseReference
                .child(eventUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        final int SCREEN_WIDTH = displayMetrics.widthPixels;

                        if (dataSnapshot.exists()) {
                            // Going through all of event hall's rows
                            ArrayList<DataSnapshot> eventRows =
                                    Utils.toArrayList(dataSnapshot.getChildren());
                            for (int i = eventRows.size() - 1; i >= 0; i--) {
                                DataSnapshot rowSnapshot = eventRows.get(i);
                                String rowNumber =  rowSnapshot.getKey();

                                // Create a new row to be added
                                TableRow tr = new TableRow(SeatsActivity.this);
                                tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                                tr.setGravity(Gravity.CENTER);

                                // Go through all row's seats
                                ArrayList<DataSnapshot> rowSeats =
                                        Utils.toArrayList(rowSnapshot.getChildren());
                                final int DIMEN_FOR_SEAT = SCREEN_WIDTH / (rowSeats.size() + 2);
                                for (int j = rowSeats.size() - 1; j >= 0; j--) {
                                    DataSnapshot seatSnapshot = rowSeats.get(j);
                                    String seatNumber = seatSnapshot.getKey();
                                    boolean isSeatTaken = (boolean) seatSnapshot.getValue();
                                    Utils.SeatStatus seatStatus =
                                            isSeatTaken ? Utils.SeatStatus.OCCUPIED : Utils.SeatStatus.AVAILABLE;

                                    SeatImageView newSeatImageView =
                                            createSeatImageView(
                                                    Integer.parseInt(rowNumber),
                                                    Integer.parseInt(seatNumber),
                                                    seatStatus);

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

    private SeatImageView createSeatImageView(int rowNumber, int seatNumber, Utils.SeatStatus status) {
        // This is the new seat image to be added to UI
        SeatImageView newSeatImageView = new SeatImageView(
                SeatsActivity.this,
                rowNumber, seatNumber, status);

        setSeatUI(newSeatImageView);
        addSeatDbListener(newSeatImageView);

        // Adding to seats array
        seats[rowNumber - 1][seatNumber - 1] = newSeatImageView;
        return newSeatImageView;
    }

    private void addSeatDbListener(final SeatImageView seat) {
        eventSeatsDatabaseReference
                .child(event.getUid())
                .child(String.valueOf(seat.getRow()))
                .child(String.valueOf(seat.getNumber()))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot seatDataSnapshot) {
                        if (seatDataSnapshot.exists()) {
                            boolean isSeatTaken = (boolean) seatDataSnapshot.getValue();
                            Utils.SeatStatus oldSeatStatus = seat.getStatus();
                            Utils.SeatStatus newSeatStatus =
                                    isSeatTaken ? Utils.SeatStatus.OCCUPIED : Utils.SeatStatus.AVAILABLE;
                            if (oldSeatStatus == newSeatStatus) return;

                            // If the seat was chosen before this update from DB, it means it was available and
                            // now it is occupied by another customer, or that we returned from PaymentActivity to
                            // this activity in which case the order seats have just become re-available.
                            // In either case, we undo the seat's selection
                            if (oldSeatStatus == Utils.SeatStatus.CHOSEN) {

                                // In case seat update probably comes from this own customer's order,
                                // which is right after he clicked "Next", we wouldn't want to update chosen seats UI
                                if (isOrderSaveInProgress) return;

                                // Update seat's status
                                seat.setStatus(newSeatStatus);
                                setSeatUI(seat);

                                // Update chosen tickets num
                                ticketsNum--;

                                // Handle tickets num + total price text view
                                updateTicketsNumUI();

                                // Handle chosen seats UI
                                updateChosenSeatsUI();

                                if (newSeatStatus == Utils.SeatStatus.OCCUPIED) {
                                    // Notify the user a seat he has chosen has become occupied by another
                                    String toastMessage = "שים לב! המושבים שבחרת נתפסו ע\"י לקוח אחר. אנא בחר מחדש";
                                    Toast.makeText(SeatsActivity.this, toastMessage, Toast.LENGTH_LONG).show();
                                }
                            }
                            // In case the seat was not chosen before this update from DB, we just update its appearance
                            // in the seat map
                            else {
                                seat.setStatus(newSeatStatus);
                                setSeatUI(seat);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
    }

    private void setSeatUI(SeatImageView seatImageView) {
        // Setting seat color depending on its occupied/available status
        seatImageView.setBackgroundResource(
                Utils.lookupImageBySeatStatus(seatImageView.getStatus()));

        // If the seat is occupied, it shouldn't be enabled for the user
        if (seatImageView.getStatus() == Utils.SeatStatus.OCCUPIED) {
            seatImageView.setClickable(false);
        }
        // If the seat is available, enable its selection
        else {
            seatImageView.setClickable(true);
            seatImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleOnSeatClick((SeatImageView) v);
                }
            });
        }
    }

    private void handleOnSeatClick(SeatImageView clickedSeat) {
        // Setting seat's new status as the reverse of its previous status
        Utils.SeatStatus newSeatStatus =
                clickedSeat.getStatus() == Utils.SeatStatus.AVAILABLE
                        ? Utils.SeatStatus.CHOSEN
                        : Utils.SeatStatus.AVAILABLE;
        clickedSeat.setStatus(newSeatStatus);

        // Updating seat color according to new status (chosen/available)
        clickedSeat.setBackgroundResource(
                Utils.lookupImageBySeatStatus(newSeatStatus));

        // If the seat's new status is chosen, increment tickets num
        if (newSeatStatus == Utils.SeatStatus.CHOSEN)
            this.ticketsNum++;
        // If the seat's new status is available, decrement tickets num
        else
            this.ticketsNum--;

        // Handle tickets num + total price text view
        updateTicketsNumUI();

        // Handle chosen seats UI
        updateChosenSeatsUI();
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

    private void updateChosenSeatsUI() {
        // If there are no currently chosen seats, hide chosen seats text
        if (ticketsNum == 0)
            binding.tvChosenSeats.setVisibility(View.INVISIBLE);
        // If there are currently chosen seats, show chosen seats text and handle its contents
        else {
            binding.tvChosenSeats.setVisibility(View.VISIBLE);
            binding.tvChosenSeats.setText(OrderDataService.generateOrderSeatsUIString(getChosenSeatsArray()));
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

    private Order createNewOrderFromUI(String newOrderUid) {
        int newOrderTotalPrice = isCouponUsed
                ? ticketsNum * event.getDiscountedPrice()
                : ticketsNum * event.getPrice();

        return new Order(newOrderUid, ticketsNum, isCouponUsed, newOrderTotalPrice,
                DataUtils.OrderStatus.IN_PROGRESS);
    }

    private void saveNewOrderSeats(final Order newOrder, final String[][] chosenSeatsStringArray) {
        eventSeatsDatabaseReference.child(event.getUid()).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {

                // We are going through all event rows and seats in DB
                for (MutableData row : mutableData.getChildren()) {
                    int rowNumber = Integer.parseInt(row.getKey());

                    for (MutableData seat : row.getChildren()) {
                        int seatNumber = Integer.parseInt(seat.getKey());

                        // Only if the current seat has been selected in UI by the user, we have work to do
                        if (seats[rowNumber - 1][seatNumber - 1].getStatus() == Utils.SeatStatus.CHOSEN) {

                            // If the chosen seat is already marked as occupied in DB, we cancel the order.
                            // This supports the case that multiple customers reached this code at the same time
                            // Makes sure no seat can be taken by more than one customer
                            boolean isSeatTaken = (boolean) seat.getValue();
                            if (isSeatTaken) {
                                abortOrder(newOrder);
                                return Transaction.abort();
                            }

                            seat.setValue(true);
                        }
                    }
                }

                // Report transaction success
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                // If the transaction aborted, do nothing here
                if (!committed) return;

                // If we reached here, the chosen seats are safe and updated in DB
                // We create a service to return the tickets if after 10 min order is not finished
                OrderDataService.fireCancelOrderService(SeatsActivity.this, newOrder.getUid(), event.getUid(),
                        true);

                // Proceed to PaymentActivity
                proceedToPayment(SeatsActivity.this, newOrder, chosenSeatsStringArray);

                isOrderSaveInProgress = false;
            }
        });

        // We also save the new order's seats in DB; this can be asynchronous and is not dependent on other users,
        // so we don't need a transaction
        Map orderSeatsData = new HashMap();

        // For every row
        for (int i = 0; i < this.seats.length; i++) {
            // For every seat from the row
            for (int j = 0; j < this.seats[i].length; j++)
                if (this.seats[i][j].getStatus() == Utils.SeatStatus.CHOSEN)
                    orderSeatsData.put(String.format("%d/%d", i+1, j+1), true);
        }

        orderSeatsDatabaseReference.child(newOrder.getUid()).updateChildren(orderSeatsData);
    }

    private void abortOrder(Order order) {
        OrderDataService.cancelOrder(event.getUid(), true, order);
        isOrderSaveInProgress = false;
        updateTicketsNumUI();
        updateChosenSeatsUI();
        String toastMessage = "שים לב! המושבים שבחרת נתפסו ע\"י לקוח אחר. אנא בחר מחדש";
        Toast.makeText(SeatsActivity.this, toastMessage, Toast.LENGTH_LONG).show();
    }

    @NonNull
    private boolean[][] getChosenSeatsArray() {
        boolean[][] chosenSeats = new boolean[seats.length][seats[0].length];
        for (int i = 0; i < seats.length; i++) {
            for (int j = 0; j < seats[0].length; j++) {
                boolean isSeatChosen = seats[i][j].getStatus() == Utils.SeatStatus.CHOSEN;
                chosenSeats[i][j] = isSeatChosen;
            }
        }
        return chosenSeats;
    }

    @NonNull
    private String[][] getChosenSeatsStringArray() {
        String[][] chosenSeatsStrings = new String[seats.length][seats[0].length];
        for (int i = 0; i < seats.length; i++) {
            for (int j = 0; j < seats[0].length; j++) {
                boolean isSeatChosen = seats[i][j].getStatus() == Utils.SeatStatus.CHOSEN;
                chosenSeatsStrings[i][j] = String.valueOf(isSeatChosen);
            }
        }
        return chosenSeatsStrings;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
}