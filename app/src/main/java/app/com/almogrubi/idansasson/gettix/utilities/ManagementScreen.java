package app.com.almogrubi.idansasson.gettix.utilities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
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
import app.com.almogrubi.idansasson.gettix.MainActivity;
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

    protected void onSignedInInitialize(FirebaseUser user) {
        this.user = user;
    }

    protected void onSignedOutCleanup() {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_manager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sign_out) {
            startActivity(new Intent(this, MainActivity.class));
            AuthUI.getInstance().signOut(this);
            return true;
        }
        else if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
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
