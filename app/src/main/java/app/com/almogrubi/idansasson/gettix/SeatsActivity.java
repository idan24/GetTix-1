package app.com.almogrubi.idansasson.gettix;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.entities.EventSeat;
import app.com.almogrubi.idansasson.gettix.entities.Order;
import app.com.almogrubi.idansasson.gettix.entities.Seat;

/**
 * Created by almogrubi on 10/14/17.
 */

public class SeatsActivity extends AppCompatActivity{


    private Event event;
    private Order order = new Order();
    private int rows = 0;
    private int columns;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        Intent intent = getIntent();
        if (intent != null) {

            event = (Event) intent.getSerializableExtra("eventObject");
            // TODO: uncomment and replace with query
            //List<EventSeat> seatsList = new ArrayList<>(event.getEventHall().getEventSeats().values());

            rows =  event.getEventHall().getRows();
            rows =  event.getEventHall().getColumns();

            for (int i = 0; i < rows; i++) {
                LinearLayout row = new LinearLayout(this);
                row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    //change to size in %
                for (int j = 0; j < columns; j++) {

                    Button b = new Button(this);
                    b.setLayoutParams(new LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.WRAP_CONTENT));
                    // TODO: uncomment after replacing with query
                    //b.setTag(seatsList.get(i+j*columns));
                    b.setText("" + (j + 1 + (i * 10)));
                    b.setId(j + 1 + (i * 10));
                    row.addView(b);

                    b.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
//                                Intent intent = new Intent(v.getContext(), SeatsActivity.class);
//                                intent.putExtra("eventObject", event);
//                                startActivity(intent);
                                ((Button) v).setText("*");
                                ((Button) v).setEnabled(false);

                                Log.i("almog", "id is " + v.getId());

                                order.setTicketsNum(order.getTicketsNum()+1);


                            }
                        }

                    );
                }

                layout.addView(row);
            }
            setContentView(layout);
            //setContentView(R.layout.main);
        }


        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        Button next = new Button(this);
        next.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        next.setText("next");
        next.setId(0);
        row.addView(next);
        layout.addView(row);
        setContentView(layout);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), PayActivity.class);
                intent.putExtra("orderObject", order);
                intent.putExtra("eventObject", event);
                startActivity(intent);
                Log.i("almog", "id is " + v.getId());

//                event.choseSit(v.getUid());

            }
        }

        );

    }
}