package app.com.almogrubi.idansasson.gettix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;

import app.com.almogrubi.idansasson.gettix.R;
import app.com.almogrubi.idansasson.gettix.authentication.ManagementScreen;

public class ManagementActivity extends ManagementScreen {

    private TextView tvWelcome;
    private Button btEvents;
    private Button btHalls;
    private Button btReports;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management);

        tvWelcome = findViewById(R.id.tv_welcome_manager);
        btEvents = findViewById(R.id.bt_events);
        btHalls = findViewById(R.id.bt_halls);
        btReports = findViewById(R.id.bt_reports);

        btEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), EventsActivity.class));
            }
        });

        btHalls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), HallsActivity.class));
            }
        });

        btReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), ReportsActivity.class));
            }
        });
    }

    @Override
    protected void onSignedInInitialize(FirebaseUser user) {
        super.onSignedInInitialize(user);

        tvWelcome.setText(String.format("שלום, %s", user.getDisplayName()));
    }
}
