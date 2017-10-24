package app.com.almogrubi.idansasson.gettix;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import app.com.almogrubi.idansasson.gettix.entities.Event;


public class FinishingActivity extends AppCompatActivity {


    Event event;
    int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finishing);


        Intent intent = this.getIntent();
        event = (Event) intent.getSerializableExtra("eventObject");
        counter = (int) intent.getSerializableExtra("counter");

    }
}
