package app.com.almogrubi.idansasson.gettix;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.entities.Order;


public class NoSeatsActivity extends AppCompatActivity {


    private Event event;

    private TextView sitsNumber;
    private TextView details;
    private Button add;
    private Button remove;
    private Button next;
    private Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_sits);

        Intent intent = this.getIntent();
        event = (Event) intent.getSerializableExtra("eventObject");

        sitsNumber = (TextView) findViewById (R.id.sitsNumber);
        details = (TextView) findViewById (R.id.details);
        remove = (Button) findViewById (R.id.removeButton);
        add = (Button) findViewById (R.id.addButton);
        next = (Button) findViewById (R.id.next);


        order = new Order();

        add.setOnClickListener(new View.OnClickListener()
           {
               @Override
               public void onClick(View v){
                   order.setTicketsNum(order.getTicketsNum()+1);
                   sitsNumber.setText(String.valueOf(order.getTicketsNum()));

           }
        }

        );

        remove.setOnClickListener(new View.OnClickListener()
           {
                public void onClick(View v){
                    if (order.getTicketsNum()!=0){
                        order.setTicketsNum(order.getTicketsNum()+1);}
                        sitsNumber.setText(String.valueOf(order.getTicketsNum()));
                    }
           }

        );

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), PayActivity.class);
                intent.putExtra("eventObject", event);
                intent.putExtra("orderObject", order);

                startActivity(intent);

            }
        }

        );

    }
}
