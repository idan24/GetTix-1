package app.com.almogrubi.idansasson.gettix;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import app.com.almogrubi.idansasson.gettix.databinding.ActivityHallEditBinding;
import app.com.almogrubi.idansasson.gettix.entities.Hall;
import app.com.almogrubi.idansasson.gettix.utilities.ManagementScreen;
import app.com.almogrubi.idansasson.gettix.utilities.Utils;

public class HallEditActivity extends ManagementScreen {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference eventsDatabaseReference;
    private DatabaseReference hallsDatabaseReference;
    private DatabaseReference hallSeatsDatabaseReference;
    private DatabaseReference hallEventsDatabaseReference;
    private DatabaseReference hallEventDatesDatabaseReference;

    private ActivityHallEditBinding binding;

    private boolean isEdit = false;
    private Hall editedHall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hall_edit);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_hall_edit);

        // Initialization of all needed Firebase database references
        initializeDatabaseReferences();

        // Initialization actions that should happen whether this is a new event or an edited existing hall
        initializeUIViews();

        Intent intent = this.getIntent();
        // If we should be in edit mode, lookup the hall in the database and bind its data to UI
        if ((intent != null) && (intent.hasExtra("hallUid"))) {

            isEdit = true;

            hallsDatabaseReference
                    .child(intent.getStringExtra("hallUid"))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // If we have a null result, the hall was somehow not found in the database
                            if (dataSnapshot == null || !dataSnapshot.exists() || dataSnapshot.getValue() == null) {
                                abort();
                                return;
                            }

                            // If we reached here then the existing hall was found, we'll bind it to UI
                            editedHall = dataSnapshot.getValue(Hall.class);
                            binding.tvHallEditTitle.setText(R.string.hall_edit_title);
                            bindExistingHallInfo();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            abort();
                        }
                    });
        }
        // If we should be in new/create mode, initialize views accordingly
        else {
            binding.etHallName.requestFocus();
        }
    }

    private void initializeDatabaseReferences() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        eventsDatabaseReference = firebaseDatabase.getReference().child("events");
        hallsDatabaseReference = firebaseDatabase.getReference().child("halls");
        hallSeatsDatabaseReference = firebaseDatabase.getReference().child("hall_seats");
        hallEventsDatabaseReference = firebaseDatabase.getReference().child("hall_events");
        hallEventDatesDatabaseReference = firebaseDatabase.getReference().child("hall_eventDates");
    }

    private void initializeUIViews() {
        binding.btSaveHall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveHallIfInputValid();
            }
        });
    }

    private void abort() {
        String hallNotFoundErrorMessage = "האולם לא נמצא, נסה שנית";

        Toast.makeText(HallEditActivity.this, hallNotFoundErrorMessage, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(HallEditActivity.this, HallsActivity.class));
    }

    private void bindExistingHallInfo() {
        binding.etHallName.setText(this.editedHall.getName());
        binding.etHallAddress.setText(this.editedHall.getAddress());
        binding.etHallCity.setText(this.editedHall.getCity());
        binding.etHallWebsite.setText(this.editedHall.getOfficialWebsite());
        binding.etHallRows.setText(String.valueOf(this.editedHall.getRows()));
        binding.etHallColumns.setText(String.valueOf(this.editedHall.getColumns()));
    }

    /*
     * The save flow is like this:
     * - Check validity of entered fields
     * - If new hall:
     *   - Save hall with seats
     * - If edited hall:
     *   - If hall/date were changed:
     *     - Update hall with seats
     */
    private void saveHallIfInputValid() {
        if (checkInstantInputValidity()) {
            // This is a new hall
            if (!isEdit) {
                // we trigger hall save directly from here
                saveNewHallAndExit();
            }
            // This is an edited existing hall
            else {
                // we also trigger hall save directly from here
                updateExistingHallAndExit();
            }
        }
    }

    private boolean checkInstantInputValidity() {
        final String emptyFieldErrorMessage = "יש למלא את השדה";
        boolean isValid = true;

        // Checking name was filled
        if (Utils.isTextViewEmpty(binding.etHallName)) {
            binding.etHallName.setError(emptyFieldErrorMessage);
            isValid = false;
        }
        // Checking address was filled
        if (Utils.isTextViewEmpty(binding.etHallAddress)) {
            binding.etHallAddress.setError(emptyFieldErrorMessage);
            isValid = false;
        }
        // Checking city was filled
        if (Utils.isTextViewEmpty(binding.etHallCity)) {
            binding.etHallCity.setError(emptyFieldErrorMessage);
            isValid = false;
        }
        // Checking official website was filled
        if (Utils.isTextViewEmpty(binding.etHallWebsite)) {
            binding.etHallWebsite.setError(emptyFieldErrorMessage);
            isValid = false;
        }
        // Checking rows number was filled
        if (Utils.isTextViewEmpty(binding.etHallRows)) {
            binding.etHallRows.setError(emptyFieldErrorMessage);
            isValid = false;
        }
        // Checking number of seats in row was filled
        if (Utils.isTextViewEmpty(binding.etHallColumns)) {
            binding.etHallColumns.setError(emptyFieldErrorMessage);
            isValid = false;
        }

        return isValid;
    }

    private void saveNewHallAndExit() {
        final String newHallUid = hallsDatabaseReference.push().getKey();
        Hall newHall = createHallFromUI(newHallUid);
        hallsDatabaseReference.child(newHallUid).setValue(newHall);

        // Create hall seat objects in firebase
        createHallSeats(newHall);

        Toast.makeText(this, "האולם נשמר בהצלחה!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, HallsActivity.class));
    }

    private void updateExistingHallAndExit() {
        final String hallUid = this.editedHall.getUid();
        Hall updatedHall = createHallFromUI(hallUid);
        hallsDatabaseReference.child(hallUid).setValue(updatedHall);

        // Create hall seat objects in firebase
        createHallSeats(updatedHall);

        Toast.makeText(this, "השינויים נשמרו בהצלחה!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, HallsActivity.class));
    }

    private Hall createHallFromUI(String hallUid) {
        String newHallName = binding.etHallName.getText().toString();
        String newHallAddress = binding.etHallAddress.getText().toString();
        String newHallCity = binding.etHallCity.getText().toString();
        String newHallOfficialWebsite = binding.etHallWebsite.getText().toString();
        int newHallRows = Integer.parseInt(binding.etHallRows.getText().toString());
        int newHallColumns = Integer.parseInt(binding.etHallColumns.getText().toString());
        String newHallProducerId = HallEditActivity.super.user.getUid();

        return new Hall(hallUid, newHallName, newHallAddress, newHallCity, newHallOfficialWebsite,
                newHallRows, newHallColumns, newHallProducerId);
    }

    private void createHallSeats(Hall hall) {

        hallSeatsDatabaseReference.child(hall.getUid()).removeValue();

        Map hallSeatsData = new HashMap();

        for (int i=0; i < hall.getRows(); i++)
            for (int j=0; j < hall.getColumns(); j++) {
                hallSeatsData.put(String.format("%d/%d", i+1, j+1), false);
            }

        hallSeatsDatabaseReference.child(hall.getUid()).updateChildren(hallSeatsData);
    }
}
