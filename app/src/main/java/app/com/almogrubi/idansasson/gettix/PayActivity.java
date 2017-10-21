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

/**
 * Created by almogrubi on 10/20/17.
 */

public class PayActivity extends AppCompatActivity {


    Show show;
    int price;
    int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final EditText to = (EditText) findViewById(R.id.email_edit_text);
        Button next = (Button) findViewById(R.id.approveButton);


        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(v.getContext(), FinishingActivity.class);
                intent.putExtra("price", price);
                intent.putExtra("showObject", show);
                startActivity(intent);

            }
         }
        );




    }

}
