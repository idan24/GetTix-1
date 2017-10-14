package app.com.flighter.foo.gettix;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class DetailActivity extends AppCompatActivity {

    ImageView imgTop;
    Button pickSitsButton;

    Show show;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        imgTop = (ImageView) findViewById(R.id.image_top);
        pickSitsButton = (Button) findViewById(R.id.button_pick_sits);

        Intent intent = this.getIntent();

        if (intent != null){
            show = (Show) intent.getSerializableExtra("showObject");

            String showName = show.getDrawNmae();

            String imgName = show.getDrawNmae();

            int id = this.getResources().getIdentifier(imgName, "mipmap",
                    this.getPackageName());

            imgTop.setImageResource(id);

        }

        pickSitsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SitsActivity.class);
                intent.putExtra("showObject", show);
                startActivity(intent);
            }
        });

    }

}
