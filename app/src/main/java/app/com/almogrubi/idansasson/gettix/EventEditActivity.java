package app.com.almogrubi.idansasson.gettix;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import app.com.almogrubi.idansasson.gettix.databinding.ActivityEventEditBinding;
import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.utilities.ManagementScreen;

public class EventEditActivity extends ManagementScreen {

    private ActivityEventEditBinding binding;
    private Event event;

    public EventEditActivity(Event event) {
        this.event = event;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_edit);
    }

    private void bindEventInfo() {
        //binding
    }
}
