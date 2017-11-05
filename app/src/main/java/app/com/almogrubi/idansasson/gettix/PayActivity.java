package app.com.almogrubi.idansasson.gettix;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
        next = (Button) findViewById(R.id.approveButton);

        Intent intent = this.getIntent();
        event = (Event) intent.getSerializableExtra("eventObject");
        order = (Order) intent.getSerializableExtra("orderObject");

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
                intent.putExtra("eventObject", event);
                startActivity(intent);

            }
         }
        );

    }

}
