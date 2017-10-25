package app.com.almogrubi.idansasson.gettix;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import app.com.almogrubi.idansasson.gettix.utilities.ManagementScreen;

public class ReportsActivity extends ManagementScreen {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
    }
}
