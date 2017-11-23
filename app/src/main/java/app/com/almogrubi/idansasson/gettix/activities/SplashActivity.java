package app.com.almogrubi.idansasson.gettix.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start main activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        // Close splash activity
        finish();
    }
}
