package app.com.almogrubi.idansasson.gettix.utilities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import app.com.almogrubi.idansasson.gettix.EventEditActivity;
import app.com.almogrubi.idansasson.gettix.R;
import app.com.almogrubi.idansasson.gettix.entities.Hall;
import app.com.almogrubi.idansasson.gettix.entities.Seat;

/**
 * Created by idans on 25/10/2017.
 *
 * Base class for Management screens - handles user sign-in/sign-out.
 * Client should implement onSignedInInitialize(user) and onSignedOutCleanup() if he needs to do something.
 */

public class ManagementScreen extends AppCompatActivity {

    // Arbitrary request code value for FirebaseUI
    public static final int RC_SIGN_IN = 1;

    protected FirebaseUser user;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    onSignedInInitialize(user);
                }
                else {
                    // User is signed out
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(
                                            Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    protected void onSignedInInitialize(FirebaseUser user) {
        this.user = user;
    }

    protected void onSignedOutCleanup() {}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // RC_SIGN_IN is the request code passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                Toast.makeText(this, "המשתמש מחובר למערכת", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                // Sign in failed
                Toast.makeText(this, "תהליך ההתחברות בוטל", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_manager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sign_out) {
            AuthUI.getInstance().signOut(this);
            return true;
        }
        else if (item.getItemId() == R.id.action_add_event) {
            startActivity(new Intent(this, EventEditActivity.class));

//            addNewHall("זאפה הרצליה",
//                    "מדינת היהודים 85",
//                    "הרצליה",
//                    "https://www.zappa-club.co.il/%D7%9E%D7%95%D7%A2%D7%93%D7%95%D7%9F/%D7%96%D7%90%D7%A4%D7%94-%D7%94%D7%A8%D7%A6%D7%9C%D7%99%D7%94/",
//                    30, 15);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addNewHall(String name, String address, String city, String officialWebsite,
                            int rows, int columns) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference hallsDatabaseReference = firebaseDatabase.getReference().child("halls");

        String hallUid = hallsDatabaseReference.push().getKey();

        Map<String, Seat> seats = new HashMap<>();

        for (int i=0; i<rows; i++)
            for (int j=0; j<columns; j++) {
                String seatUid = String.format("%sSEAT%d-%d", hallUid, i+1, j+1);
                seats.put(seatUid, new Seat(seatUid, i+1, j+1));
//                hallsDatabaseReference.child(hallUid).child("seats")
//                        .child(seatUid).setValue(new Seat(seatUid, i+1, j+1));
            }

        Hall newHall = new Hall(hallUid, name, address, city, officialWebsite, rows, columns, seats);
        hallsDatabaseReference.child(hallUid).setValue(newHall);
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (authStateListener != null)
            firebaseAuth.removeAuthStateListener(authStateListener);
    }
}
