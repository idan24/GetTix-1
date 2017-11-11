package app.com.almogrubi.idansasson.gettix;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import app.com.almogrubi.idansasson.gettix.databinding.ActivityPaymentBinding;
import app.com.almogrubi.idansasson.gettix.entities.Customer;
import app.com.almogrubi.idansasson.gettix.entities.Order;
import app.com.almogrubi.idansasson.gettix.utilities.DataUtils;
import app.com.almogrubi.idansasson.gettix.utilities.Utils;

/**
 * Created by almogrubi on 10/20/17.
 */

public class PaymentActivity extends AppCompatActivity {

    private ActivityPaymentBinding binding;
    private String eventUid;
    private boolean isMarkedSeats;
    private Order order;

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
            else abort();
        }

        binding.tvTicketsNum.setText(
                String.format("רכישת %d כרטיסים: %d ₪",
                        order.getTicketsNum(), order.getTotalPrice()));

        binding.btPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInputValidity()) {
                    // Order save and continue to next screen will be triggered from callback
                    // inside fireCreditCardTokenCreation()
                    fireCreditCardTokenCreation();
                }
            }
        });
    }

    private void abort() {
        String orderNotFoundErrorMessage = "ההזמנה לא נמצאה, נסה שנית";

        Toast.makeText(this, orderNotFoundErrorMessage, Toast.LENGTH_SHORT).show();

        if (this.isMarkedSeats)
            startActivity(new Intent(this, SeatsActivity.class));
        else
            startActivity(new Intent(this, NoSeatsActivity.class));
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

                        ordersDatabaseReference.child(eventUid).child(order.getUid()).setValue(order);

                        // Continue to confirmation activity
                        Intent confirmationActivity = new Intent(PaymentActivity.this, ConfirmationActivity.class);
                        confirmationActivity.putExtra("eventUid", eventUid);
                        confirmationActivity.putExtra("orderObject", order);
                        startActivity(confirmationActivity);
                    }
                    public void onError(Exception error) {
                        // Show localized error message
                        Toast.makeText(PaymentActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG)
                                .show();
                    }
                });
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