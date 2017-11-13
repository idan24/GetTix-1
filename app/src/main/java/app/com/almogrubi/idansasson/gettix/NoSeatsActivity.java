package app.com.almogrubi.idansasson.gettix;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.util.HashMap;
import java.util.Map;

import app.com.almogrubi.idansasson.gettix.databinding.ActivityNoSeatsBinding;
import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.entities.Order;
import app.com.almogrubi.idansasson.gettix.utilities.DataUtils;
import app.com.almogrubi.idansasson.gettix.utilities.Utils;


public class NoSeatsActivity extends AppCompatActivity {

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
                            updateTicketsNumUI(1);
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
                // Create a new order in database
                final String newOrderUid = ordersDatabaseReference.push().getKey();
                Order newOrder = createNewOrderFromUI(newOrderUid);
                // orders / $ eventUid / $ newOrderUid / newOrder
                ordersDatabaseReference.child(event.getUid()).child(newOrderUid).setValue(newOrder);

                int newLeftTicketsNum = event.getLeftTicketsNum() - newOrder.getTicketsNum();
                Map newEventData = new HashMap();
                newEventData.put("leftTicketsNum", newLeftTicketsNum);

                if (newLeftTicketsNum == 0) {
                    newEventData.put("soldOut", true);
                }

                eventsDatabaseReference.child(event.getUid()).updateChildren(newEventData);

                // We create a service to return the tickets if after 10 min order is
                // not finished
                fireCancelOrderService(newOrder);

                Intent paymentActivity = new Intent(v.getContext(), PaymentActivity.class);
                paymentActivity.putExtra("eventUid", event.getUid());
                paymentActivity.putExtra("eventTitle", event.getTitle());
                paymentActivity.putExtra("eventMarkedSeats", false);
                paymentActivity.putExtra("orderObject", newOrder);
                startActivity(paymentActivity);
            }
        });
    }

    private void abort() {
        String eventNotFoundErrorMessage = "המופע לא נמצא, נסה שנית";

        Toast.makeText(this, eventNotFoundErrorMessage, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, EventDetailsActivity.class));
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

    private void fireCancelOrderService(Order order) {

        // Create a new dispatcher using the Google Play driver.
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(NoSeatsActivity.this));

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
