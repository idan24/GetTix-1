package app.com.almogrubi.idansasson.gettix.activities;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import app.com.almogrubi.idansasson.gettix.R;
import app.com.almogrubi.idansasson.gettix.databinding.ActivityNoSeatsBinding;
import app.com.almogrubi.idansasson.gettix.dataservices.OrderDataService;
import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.entities.Order;
import app.com.almogrubi.idansasson.gettix.dataservices.DataUtils;
import app.com.almogrubi.idansasson.gettix.utilities.Utils;


public class NoSeatsActivity extends AppCompatActivity {

    // Firebase database references
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference eventsDatabaseReference;
    private DatabaseReference ordersDatabaseReference;

    private Event event;
    private boolean isCouponUsed = false;

    private ActivityNoSeatsBinding binding;
    private TextView tvCouponCode;
    private EditText etCouponCode;
    private Button btCheckCoupon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_seats);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_no_seats);
        tvCouponCode = binding.couponBox.findViewById(R.id.tv_coupon_code);
        etCouponCode = binding.couponBox.findViewById(R.id.et_coupon_code);
        btCheckCoupon = binding.couponBox.findViewById(R.id.bt_check_coupon);

        // Initialize all needed Firebase database references
        firebaseDatabase = FirebaseDatabase.getInstance();
        eventsDatabaseReference = firebaseDatabase.getReference().child("events");
        ordersDatabaseReference = firebaseDatabase.getReference().child("orders");

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
                            binding.tvEventTitle.setText(event.getTitle());

                            // To cover the slim chance that the event got sold out between the user first entered
                            // EventDetailsActivity and after he entered this activity, we notify him
                            if (event.isSoldOut()) {
                                onEventGotSoldOut();
                            }
                            else {
                                // Update tickets num with initial value of 1 ticket
                                updateTicketsNumUI(1);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            abort();
                        }
                    });
        } else {
            abort();
        }

        binding.ivPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementTicketsNum();
            }
        });

        binding.ivMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrementTicketsNum();
            }
        });

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
                onNext(v.getContext());
            }
        });
    }

    private void onEventGotSoldOut() {
        String orderInvalidMessage = "שים לב! ברגעים אלה אזלו הכרטיסים למופע זה. מצטערים!";
        Toast.makeText(NoSeatsActivity.this, orderInvalidMessage, Toast.LENGTH_LONG).show();
        final Intent detailActivityIntent = new Intent(NoSeatsActivity.this, EventDetailsActivity.class);
        detailActivityIntent.putExtra("eventUid", event.getUid());

        // Send user back to event details activity after toast was shown for long enough
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(2500);
                    startActivity(detailActivityIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }

    private void abort() {
        String eventNotFoundErrorMessage = "המופע לא נמצא, נסה שנית";
        Toast.makeText(this, eventNotFoundErrorMessage, Toast.LENGTH_SHORT).show();

        startActivity(new Intent(this, MainActivity.class));
    }

    private void onNext(Context context) {
        // Create a new order in database
        final String newOrderUid = ordersDatabaseReference.push().getKey();
        Order newOrder = createNewOrderFromUI(newOrderUid);
        // orders / $ eventUid / $ newOrderUid / newOrder
        ordersDatabaseReference.child(event.getUid()).child(newOrderUid).setValue(newOrder);

        // Update order's event's leftTicketsNum (and soldOut if necessary)
        updateEventTicketsNum(newOrder.getTicketsNum());

        // We create a service to return the tickets if after 10 min order is
        // not finished
        OrderDataService.fireCancelOrderService(NoSeatsActivity.this, newOrderUid, event.getUid(),
                false);

        // Proceed to PaymentActivity
        proceedToPayment(context, newOrder);
    }

    private void proceedToPayment(Context context, Order newOrder) {

        Intent paymentActivity = new Intent(context, PaymentActivity.class);
        paymentActivity.putExtra("eventUid", event.getUid());
        paymentActivity.putExtra("eventTitle", event.getTitle());
        paymentActivity.putExtra("eventMarkedSeats", false);
        paymentActivity.putExtra("orderObject", newOrder);

        startActivity(paymentActivity);
    }

    /*
     * We update the event's leftTicketsNum value in an atomic transaction for synchronization
     */
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

    private void incrementTicketsNum() {
        int previousTicketsNum = Integer.parseInt(binding.tvTicketsNum.getText().toString());

        if (previousTicketsNum < this.event.getLeftTicketsNum()) {
            updateTicketsNumUI(previousTicketsNum + 1);
        }
    }

    private void decrementTicketsNum() {
        int previousTicketsNum = Integer.parseInt(binding.tvTicketsNum.getText().toString());

        if (previousTicketsNum > 1) {
            updateTicketsNumUI(previousTicketsNum - 1);
        }
    }

    private void updateTicketsNumUI(int ticketsNums) {
        int newTotalPrice = isCouponUsed
                ? ticketsNums * event.getDiscountedPrice()
                : ticketsNums * event.getPrice();
        binding.tvTicketsNum.setText(String.valueOf(ticketsNums));
        binding.tvFriendlyTicketsNum.setText(String.format("רכישת %d כרטיסים: %d ₪", ticketsNums, newTotalPrice));
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
        Toast.makeText(NoSeatsActivity.this, validCouponMessage, Toast.LENGTH_LONG).show();

        // Should be set before updating tickets num UI
        isCouponUsed = true;

        // Now that a coupon has been entered, we update tickets num and price accordingly
        updateTicketsNumUI(Integer.parseInt(binding.tvTicketsNum.getText().toString()));

        tvCouponCode.setTextColor(Color.GRAY);
        etCouponCode.setEnabled(false);
        btCheckCoupon.setEnabled(false);
        btCheckCoupon.setBackgroundColor(Color.LTGRAY);
        btCheckCoupon.setTextColor(Color.GRAY);
    }

    private Order createNewOrderFromUI(String orderUid) {
        int newOrderTicketsNum = Integer.parseInt(binding.tvTicketsNum.getText().toString());
        int newOrderTotalPrice = isCouponUsed
                ? newOrderTicketsNum * event.getDiscountedPrice()
                : newOrderTicketsNum * event.getPrice();

        return new Order(orderUid, newOrderTicketsNum, isCouponUsed, newOrderTotalPrice,
                DataUtils.OrderStatus.IN_PROGRESS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * These two methods are for handling native Android back button the way we need
     * for keeping the app state valid
     */
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
