package app.com.almogrubi.idansasson.gettix;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import app.com.almogrubi.idansasson.gettix.entities.Event;

public class DetailActivity extends AppCompatActivity {

    private ImageView imgTop;
    private Button pickSitsButton;
    private TextView showName;
    private TextView showDateAndTime;
    private TextView showLocation;
    private TextView description;

    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        imgTop = (ImageView) findViewById(R.id.image_top);
        showName = (TextView) findViewById (R.id.show_name);
        showDateAndTime = (TextView) findViewById (R.id.show_date_and_time);
        showLocation = (TextView) findViewById (R.id.show_location);
        description = (TextView) findViewById (R.id.description);
        pickSitsButton = (Button) findViewById(R.id.button_pick_sits);


        Intent intent = this.getIntent();

        if (intent != null){
            event = (Event) intent.getSerializableExtra("eventObject");

            String imgName = event.getPosterUri().toString();

            int idImg = this.getResources().getIdentifier(imgName, "mipmap",
                    this.getPackageName());
            imgTop.setImageResource(idImg);
            showName.setText(event.getTitle());
            showDateAndTime.setText(event.getDateTime().toString());
            showLocation.setText(event.getEventHall().getName());
            description.setText(event.getDescription());

        }

        pickSitsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                Intent intent;

                if (event.hasMarkedSeats())
                    {
                    intent = new Intent(v.getContext(), NoSeatsActivity.class);
                        intent.putExtra("eventObject", event);
                        startActivity(intent);

                    }
                if (!event.hasMarkedSeats())
                    {
                    intent = new Intent(v.getContext(), SeatsActivity.class);
                    intent.putExtra("eventObject", event);
                    startActivity(intent);
                    }
                }


        });

    }

}