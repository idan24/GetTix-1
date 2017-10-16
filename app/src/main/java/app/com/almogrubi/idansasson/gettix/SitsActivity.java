package app.com.almogrubi.idansasson.gettix;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.util.Log;

public class SitsActivity extends AppCompatActivity{


    Show show;
    int rows;
    int columns;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        Intent intent = getIntent();
        if (intent != null) {

            show = (Show) intent.getSerializableExtra("showObject");
            rows = show.getSitsArray().length;
            columns = show.getSitsArray().length;

            for (int i = 0; i < rows; i++) {
                LinearLayout row = new LinearLayout(this);
                row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                for (int j = 0; j < columns; j++) {
                    Button b = new Button(this);
                    b.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    b.setText("" + (j + 1 + (i * 10)));
                    b.setId(j + 1 + (i * 10));
                    row.addView(b);

                    b.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
//                                Intent intent = new Intent(v.getContext(), SitsActivity.class);
//                                intent.putExtra("showObject", show);
//                                startActivity(intent);
                                ((Button) v).setText("*");
                                ((Button) v).setEnabled(false);

                                Log.i("almog", "id is " + v.getId());
//                              add private grid for selected tickets

                            }
                        }

                    );
                }

                layout.addView(row);
            }
            setContentView(layout);
            //setContentView(R.layout.main);
        }
    }
}