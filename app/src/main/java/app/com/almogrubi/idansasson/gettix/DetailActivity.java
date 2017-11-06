package app.com.almogrubi.idansasson.gettix;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import app.com.almogrubi.idansasson.gettix.entities.Event;

public class DetailActivity extends AppCompatActivity {


    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;
    private DatabaseReference eventsDatabaseReference;

    private ImageView imgTop;
    private Button pickSitsButton;
    private TextView showName;
    private TextView showDateAndTime;
    private TextView showLocation;
    private TextView description;

    private String eventUid;
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

        if (intent != null && (intent.hasExtra("eventUid"))){
        Log.i("almog", "not null");
        Log.i("almog", intent.getStringExtra("eventUid"));

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

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            abort();
                        }
                    });


            String imgName = event.getPosterUri().toString();

            int idImg = this.getResources().getIdentifier(imgName, "mipmap",
                    this.getPackageName());
            imgTop.setImageResource(idImg);
            showName.setText(event.getTitle());
            showDateAndTime.setText(event.getDate().toString());
            showLocation.setText(event.getEventHall().getName());
            description.setText(event.getDescription());

        }

        pickSitsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                Intent intent;

                if (event.isMarkedSeats())
                    {
                    intent = new Intent(v.getContext(), NoSeatsActivity.class);
                        intent.putExtra("eventUid", eventUid);
                        startActivity(intent);

                    }
                if (!event.isMarkedSeats())
                    {
                    intent = new Intent(v.getContext(), SeatsActivity.class);
                    intent.putExtra("eventUid", eventUid);
                    startActivity(intent);
                    }
                }


        });

    }

    private void abort() {
        String eventNotFoundErrorMessage = "המופע לא נמצא, נסה שנית";

        Toast.makeText(this, eventNotFoundErrorMessage, Toast.LENGTH_SHORT);
        startActivity(new Intent(this, MainActivity.class));
    }

}