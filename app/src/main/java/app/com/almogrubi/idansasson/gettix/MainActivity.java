package app.com.almogrubi.idansasson.gettix;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by almogrubi on 10/14/17.
 */

public class MainActivity extends AppCompatActivity {

    private Button DatePickerButton;
    private EditText LocationEditText;
    private EditText titleEditText;
    private EditText TypeEditText;
    private Button SearchButton;
    private String chosenDate;


    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    List<Show> showList = new ArrayList<>();

    Calendar myCalendar = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener(){
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            chosenDate= String.valueOf(dayOfMonth) + "/" + String.valueOf(monthOfYear) +"/"+ String.valueOf(year);
            Log.i("printing to log", chosenDate);

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        //date picker pop up via button click
        Button dateButton = (Button)  findViewById(R.id.date_button);
        dateButton.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v){ new DatePickerDialog(MainActivity.this, date, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                    }
                }
        );


        Show macbeth = new Show("macbeth", "Haifa,Sderot zion", "Movie", "Godd", "45/6/2016", "21:45", 30, 1, "macbeth");
        Show starwars = new Show("starwars", "Haifa,Sderot zion", "Movie", "Godd", "45/6/2016", "21:45", 30, 1, "starwars");

        showList.add(macbeth );
        showList.add(starwars );

        mAdapter = new RecyclerViewAdapter(showList, this);
        recyclerView.setAdapter(mAdapter);

    }
}