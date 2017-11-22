package app.com.almogrubi.idansasson.gettix.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import java.security.SecureRandom;

import app.com.almogrubi.idansasson.gettix.R;
import app.com.almogrubi.idansasson.gettix.databinding.ActivityPaymentBinding;
import app.com.almogrubi.idansasson.gettix.entities.Customer;
import app.com.almogrubi.idansasson.gettix.entities.Order;
import app.com.almogrubi.idansasson.gettix.dataservices.DataUtils;
import app.com.almogrubi.idansasson.gettix.dataservices.OrderDataService;
import app.com.almogrubi.idansasson.gettix.utilities.Utils;

public class PaymentActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    private ActivityPaymentBinding binding;
    private String eventUid;
    private boolean isMarkedSeats;
    private Order order;

    // We keep the order seats as strings as they are only kept here to be passed from SeatsActivity
    // to ConfirmationActivity
    private String[][] orderSeats;

    // Firebase database references
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference eventsDatabaseReference;
    private DatabaseReference ordersDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_payment);

        // Initialize all needed Firebase database references
        firebaseDatabase = FirebaseDatabase.getInstance();
        eventsDatabaseReference = firebaseDatabase.getReference().child("events");
        ordersDatabaseReference = firebaseDatabase.getReference().child("orders");

        Intent intent = this.getIntent();
        if (intent != null) {
            if (intent.hasExtra("eventUid")) {
                this.eventUid = intent.getStringExtra("eventUid");
            }
            if (intent.hasExtra("eventTitle")) {
                binding.tvEventTitle.setText(intent.getStringExtra("eventTitle"));
            }
            if (intent.hasExtra("eventMarkedSeats")) {
                this.isMarkedSeats = intent.getBooleanExtra("eventMarkedSeats", false);
            }
            if (intent.hasExtra("orderObject")) {
                this.order = (Order)intent.getSerializableExtra("orderObject");
            }
            if (intent.hasExtra("orderSeats")) {
                orderSeats = (String[][]) intent.getSerializableExtra("orderSeats");
            }

            // We make sure the event's left tickets num is still valid when entering this screen
            // This covers the synchronization issue that could occur when multiple customers order the last
            // tickets at the same time
            checkLeftTicketsNum();
        }

        binding.tvTicketsNum.setText(
                String.format("רכישת %d כרטיסים: %d ₪",
                        order.getTicketsNum(), order.getTotalPrice()));

        binding.btPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInputValidity()) {
                    progressDialog = ProgressDialog.show(PaymentActivity.this, "המתן רק דקה",
                            "ההזמנה מתבצעת...", true, false);
                    // Order save and continue to next screen will be triggered from callback
                    // inside fireOrderValidityCheck()
                    fireOrderValidityCheck();
                }
            }
        });

        binding.etFullName.requestFocus();
    }

    private void checkLeftTicketsNum() {
        // Retrieve event's up-to-date leftTicketsNum from DB
        eventsDatabaseReference
                .child(this.eventUid)
                .child("leftTicketsNum").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {

                if (mutableData.getValue() == null)
                    return Transaction.success(mutableData);

                int leftTicketsNum = Integer.parseInt(mutableData.getValue().toString());

                // For the rare scenario where multiple customers ordered the last tickets for the event
                // at the same time, we identify it by checking if its left tickets num is negative and
                // therefore invalid. In that case, we abort the orders of all clients that booked their
                // orders at the same time (sometimes besides the first one to get here, depending on timing)
                if (leftTicketsNum < 0) {
                    abortOrder();
                    return Transaction.abort();
                }

                // Report transaction success
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {}
        });
    }

    /*
     * Aborting the order happens if the order somehow became invalid - for instance, if multiple customers ordered
     * last event tickets at the same time
     */
    private void abortOrder() {
        OrderDataService.cancelOrder(eventUid, isMarkedSeats, order);

        String orderInvalidMessage = "שים לב! לא נותרו כרטיסים בכמות שבחרת. במידה ולא אזלו הכרטיסים, נסה שוב";
        Toast.makeText(PaymentActivity.this, orderInvalidMessage, Toast.LENGTH_LONG).show();
        final Intent detailActivityIntent = new Intent(this, EventDetailsActivity.class);
        detailActivityIntent.putExtra("eventUid", eventUid);

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

    private boolean checkInputValidity() {
        final String emptyFieldErrorMessage = "יש למלא את פרטי המזמין";
        final String invalidCreditCardErrorMessage = "יש למלא פרטי אשראי תקינים!";
        boolean isValid = true;

        // Checking full name was filled
        if (Utils.isTextViewEmpty(binding.etFullName)) {
            binding.etFullName.setError(emptyFieldErrorMessage);
            isValid = false;
        }
        // Checking phone was filled
        else if (Utils.isTextViewEmpty(binding.etPhone)) {
            binding.etPhone.setError(emptyFieldErrorMessage);
            isValid = false;
        }
        // Checking email was filled
        else if (Utils.isTextViewEmpty(binding.etEmail)) {
            binding.etEmail.setError(emptyFieldErrorMessage);
            isValid = false;
        }
        else {
            Card creditCard = binding.creditCardInputWidget.getCard();

            // By Stripe design, the card object will be null if the user inputs invalid data
            if ((creditCard == null) || (!creditCard.validateCard())) {
                Toast.makeText(this, invalidCreditCardErrorMessage, Toast.LENGTH_LONG).show();
                isValid = false;
            }
        }

        return isValid;
    }

    /*
     * Check the order is still valid before placing it
     */
    private void fireOrderValidityCheck() {
        ordersDatabaseReference.child(eventUid).child(order.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && (dataSnapshot.getValue() != null)) {
                    order = dataSnapshot.getValue(Order.class);

                    // We check if by the time the user finished entering his details the order has become invalid
                    // If so, we send him back to main activity
                    if (order.getStatusAsEnum() == DataUtils.OrderStatus.CANCELLED) {
                        progressDialog.dismiss();
                        onOrderExpired();
                    }
                    // Otherwise continue the order process
                    else {
                        fireCreditCardTokenCreation();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    /*
     * Handles the credit card token creation from Stripe and proceeds with order placement
     */
    private void fireCreditCardTokenCreation() {
        Card creditCard = binding.creditCardInputWidget.getCard();

        Stripe stripe = new Stripe(this, Utils.STRIPE_PUBLISHABLE_KEY);
        stripe.createToken(
                creditCard,
                new TokenCallback() {
                    public void onSuccess(Token token) {
                        // Credit card token has been successfully created
                        // Update order object in database
                        placeOrder(token);

                        progressDialog.dismiss();

                        // Proceed to confirmation activity
                        proceedToConfirmation();
                    }
                    public void onError(Exception error) {
                        // Show localized error message
                        Toast.makeText(PaymentActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    private void placeOrder(Token token) {
        String orderCustomerName = binding.etFullName.getText().toString();
        String orderCustomerPhone = binding.etPhone.getText().toString();
        String orderCustomerEmail = binding.etEmail.getText().toString();
        Customer orderCustomer = new Customer(orderCustomerName, orderCustomerPhone, orderCustomerEmail);
        order.setCustomer(orderCustomer);
        order.setCreditCardToken(token.getCard().getLast4());
        order.setStatusAsEnum(DataUtils.OrderStatus.FINAL);
        order.setConfirmationNumber(
                Utils.generateRandomString(
                        OrderDataService.ORDER_CONFIRMATION_NUMBER_LENGTH,
                        new SecureRandom()));

        ordersDatabaseReference.child(eventUid).child(order.getUid()).setValue(order);
    }

    private void proceedToConfirmation() {
        Intent confirmationActivity = new Intent(PaymentActivity.this, ConfirmationActivity.class);
        confirmationActivity.putExtra("eventUid", eventUid);
        confirmationActivity.putExtra("eventMarkedSeats", isMarkedSeats);
        confirmationActivity.putExtra("orderObject", order);

        if (isMarkedSeats)
            confirmationActivity.putExtra("orderSeats", orderSeats);

        startActivity(confirmationActivity);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // We check order is not null in case order is somehow not initialized yet
        if (this.order != null) {
            ordersDatabaseReference.child(eventUid).child(order.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && (dataSnapshot.getValue() != null)) {
                        order = dataSnapshot.getValue(Order.class);

                        // We check if by the time the user resumed this activity the order has become invalid
                        // If so, we send him back to main activity
                        if (order.getStatusAsEnum() == DataUtils.OrderStatus.CANCELLED) {
                            onOrderExpired();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }
    }

    private void onOrderExpired() {
        String orderCancelledMessage = "שים לב! חלפו 10 דקות והזמנתך אינה תקפה. נסה שוב";
        Toast.makeText(PaymentActivity.this, orderCancelledMessage, Toast.LENGTH_LONG).show();
        final Intent mainActivity = new Intent(PaymentActivity.this, MainActivity.class);

        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(2500);
                    startActivity(mainActivity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * These two methods are for handling native Android back button the way we need
     * for keeping the app and order state valid
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
        // Cancel the order the was created in seats/no-seats activity
        OrderDataService.cancelOrder(eventUid, isMarkedSeats, order);

        // Go back to seats / no-seats activity
        finish();
    }
}