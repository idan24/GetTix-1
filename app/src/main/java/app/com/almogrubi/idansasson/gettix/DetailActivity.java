package app.com.almogrubi.idansasson.gettix;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    ImageView imgTop;
    Button pickSitsButton;
    TextView showName;
    TextView showDateAndTime;
    TextView showLocation;
    TextView description;

    Show show;

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
            show = (Show) intent.getSerializableExtra("showObject");

            String imgName = show.getDrawNmae();




            int idImg = this.getResources().getIdentifier(imgName, "mipmap",
                    this.getPackageName());
            imgTop.setImageResource(idImg);
            showName.setText(show.getTitle());
            showDateAndTime.setText(show.getDate() + " and " + show.getTime());
            showLocation.setText(show.getLocation());
            description.setText(show.getDescreption());



        }

        pickSitsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                Intent intent;

                Log.i("almog","" + show.getType());

                if (show.getType().toString().equals("Theatre"))
                    {
                    intent = new Intent(v.getContext(), NoSitsActivity.class);
                        intent.putExtra("showObject", show);
                        startActivity(intent);

                    }
                    if (show.getType().toString().equals("Movie")){
                    intent = new Intent(v.getContext(), SitsActivity.class);
                    intent.putExtra("showObject", show);
                    startActivity(intent);
                    }
                }


        });

    }

}