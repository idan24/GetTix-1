package app.com.flighter.foo.gettix;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button DatePickerButton;
    private EditText LocationEditText;
    private EditText NameEditText;
    private EditText TypeEditText;
    private Button SearchButton;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    List<Show> showList = new ArrayList<>();


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



        Show show = new Show("Makbet", "Haifa,Sderot zion", "Movie", "Godd", "45/6/2016", "21:45", 30, 1, "macbeth");
        showList.add(show );
        showList.add(show );
        showList.add(show );

        mAdapter = new RecyclerViewAdapter(showList, this);
        recyclerView.setAdapter(mAdapter);
    }
}
