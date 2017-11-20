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

/**
 * Created by almogrubi on 10/20/17.
 */

public class PaymentActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    private ActivityPaymentBinding binding;
    private String eventUid;
    private boolean isMarkedSeats;
    private Order order;

    // We keep the order seats as strings as they are only kept here to be passed from SeatsActivity
    // to ConfirmationActivity
    private String[][] orderSeats;

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
        }

        // Retrieve event's up-to-date leftTicketsNum from DB
        eventsDatabaseReference
                .child(intent.getStringExtra("eventUid"))
                .child("leftTicketsNum").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                int leftTicketsNum = Integer.parseInt(mutableData.getValue().toString());

                // For the rare scenario where multiple customers ordered the last tickets for the event
                // at the same time, we identify it by checking if its left tickets num is negative and
                // therefore invalid. In that case, we abort the orders of all clients that booked their
                // orders at the same time (sometimes besides the first one to get here, depending on timing)
                if (leftTicketsNum < 0)
                    abortOrder();

                // Report transaction success
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {}
        });
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        // If we have a null result, the event was somehow not found in the database
//                        if (dataSnapshot == null || !dataSnapshot.exists() || dataSnapshot.getValue() == null) {
//                            abort();
//                            return;
//                        }
//
//                        // If we reached here then the existing event was found
//                        Event event = dataSnapshot.getValue(Event.class);
//
//                        // For the rare scenario where multiple customers ordered the last tickets for the event
//                        // at the same time, we identify it by checking if its left tickets num is negative and
//                        // therefore invalid. In that case, we abort the orders of all clients that booked their
//                        // orders at the same time
//                        if (event.getLeftTicketsNum() < 0)
//                            abortOrder();
//
////                        int eventLeftTicketsNum = Integer.parseInt(dataSnapshot.getValue().toString());
////                        if (eventLeftTicketsNum < 0)
////                            abortOrder();
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {}
//                });

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
                    // inside fireCreditCardTokenCreation()
                    fireCreditCardTokenCreation();
                }
            }
        });

        binding.etFullName.requestFocus();
    }

    private void abortOrder() {
        OrderDataService.cancelOrder(eventUid, isMarkedSeats, order);
        String toastMessage = "מצטערים, הזמנתך נחסמה ע\"י לקוח אחר. אם לא אזלו הכרטיסים, נסה שנית!";
        Toast.makeText(PaymentActivity.this, toastMessage, Toast.LENGTH_LONG).show();

        // Send user back to event details activity
        Intent detailActivityIntent = new Intent(this, EventDetailsActivity.class);
        detailActivityIntent.putExtra("eventUid", eventUid);
        startActivity(detailActivityIntent);
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
        if (Utils.isTextViewEmpty(binding.etPhone)) {
            binding.etPhone.setError(emptyFieldErrorMessage);
            isValid = false;
        }
        // Checking email was filled
        if (Utils.isTextViewEmpty(binding.etEmail)) {
            binding.etEmail.setError(emptyFieldErrorMessage);
            isValid = false;
        }

        if (isValid) {
            Card creditCard = binding.creditCardInputWidget.getCard();
            // By Stripe design, the card object will be null if the user inputs invalid data
            if ((creditCard == null) || (!creditCard.validateCard())) {
                Toast.makeText(this, invalidCreditCardErrorMessage, Toast.LENGTH_LONG).show();
                isValid = false;
            }
        }

        return isValid;
    }

    private void fireCreditCardTokenCreation() {
        Card creditCard = binding.creditCardInputWidget.getCard();

        Stripe stripe = new Stripe(this, Utils.STRIPE_PUBLISHABLE_KEY);
        stripe.createToken(
                creditCard,
                new TokenCallback() {
                    public void onSuccess(Token token) {
                        // Update order object in database
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
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
        // Cancel the order the was created in seats/no-seats activity
        OrderDataService.cancelOrder(eventUid, isMarkedSeats, order);
        finish();
    }
}