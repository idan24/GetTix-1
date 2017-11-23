package app.com.almogrubi.idansasson.gettix.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import app.com.almogrubi.idansasson.gettix.R;
import app.com.almogrubi.idansasson.gettix.databinding.ActivityEventDetailsBinding;
import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.entities.Hall;
import app.com.almogrubi.idansasson.gettix.dataservices.DataUtils;
import app.com.almogrubi.idansasson.gettix.utilities.Utils;

public class EventDetailsActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference eventsDatabaseReference;
    private DatabaseReference hallsDatabaseReference;

    private ActivityEventDetailsBinding binding;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_details);

        // Initialize all needed Firebase database references
        firebaseDatabase = FirebaseDatabase.getInstance();
        eventsDatabaseReference = firebaseDatabase.getReference().child("events");
        hallsDatabaseReference = firebaseDatabase.getReference().child("halls");

        Intent intent = this.getIntent();
        // Lookup the event in the database and bind its data to UI
        if ((intent != null) && (intent.hasExtra("eventUid"))) {
            eventsDatabaseReference
                    .child(intent.getStringExtra("eventUid"))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // If we have a null result, the event was somehow not found in the database
                            if (dataSnapshot == null || !dataSnapshot.exists() || dataSnapshot.getValue() == null) {
                                abort();
                                return;
                            }

                            // If we reached here then the existing event was found, we'll bind it to UI
                            event = dataSnapshot.getValue(Event.class);
                            bindEventToUI(event);

                            binding.btBookTickets.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    onBookTicketsClick(event);
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            abort();
                        }
                    });
        } else {
            abort();
        }
    }

    private void bindEventToUI(final Event event) {

        // Use Cloudinary to transform event poster to fit the screen and Glide to load the transformed image to view
        Glide.with(binding.ivEventPoster.getContext())
                .load(Utils.getTransformedCloudinaryImageUrl(
                        event.getCategoryAsEnum(), 450, 200, event.getPosterUri(), "fill"))
                .into(binding.ivEventPoster);

        binding.tvEventTitle.setText(Utils.createIndentedText(event.getTitle(),
                Utils.FIRST_LINE_INDENT, Utils.PARAGRAPH_INDENT));
        binding.tvEventDatetime.setText(String.format("%s בשעה %s",
                DataUtils.convertToUiDateFormat(event.getDate()),
                event.getHour()));

        bindEventHallToUI(event.getEventHall().getUid());

        binding.ivEventCategory.setBackgroundResource(Utils.lookupImageByCategory(event.getCategoryAsEnum()));

        binding.tvEventPrice.setText(String.format("%s₪", event.getPrice()));

        if (event.getDuration() != 0) {
            String newLine = System.getProperty("line.separator");
            binding.tvEventDescription.setText(String.format("משך המופע: %d דקות%s%s%s",
                    event.getDuration(), newLine, newLine, event.getDescription()));
        }
        else {
            binding.tvEventDescription.setText(event.getDescription());
        }

        // Set left tickets text
        setLeftTicketsUI(event);
    }

    private void bindEventHallToUI(String eventHallUid) {
        // Retrieve hall information from DB
        hallsDatabaseReference
                .child(eventHallUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // If we have a null result, the hall was somehow not found in the database
                        if (dataSnapshot == null || !dataSnapshot.exists() || dataSnapshot.getValue() == null) {
                            return;
                        }

                        // If we reached here then the hall was found, we'll bind it to UI
                        final Hall hall = dataSnapshot.getValue(Hall.class);

                        // Set hall address with the hall name being a hyperlink to the hall's official website
                        final SpannableStringBuilder hallAddress = new SpannableStringBuilder(String.format("%s, %s, %s",
                                hall.getName(), hall.getAddress(), hall.getCity()));
                        ClickableSpan clickableSpan = new ClickableSpan() {
                            @Override
                            public void onClick(View textView) {
                                openHallOfficialWebsite(hall.getOfficialWebsite());
                            }
                        };
                        hallAddress.setSpan(clickableSpan, 0, hall.getName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        hallAddress.setSpan(new LeadingMarginSpan.Standard(
                                Utils.FIRST_LINE_INDENT, Utils.PARAGRAPH_INDENT),0, hallAddress.length(),0);
                        binding.tvEventHallAddress.setText(hallAddress);
                        binding.tvEventHallAddress.setMovementMethod(LinkMovementMethod.getInstance());
                        binding.tvEventHallAddress.setHighlightColor(Color.TRANSPARENT);

                        // Set location map link
                        binding.ivEventLocation.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openMapLocation(hall);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
    }

    private void onBookTicketsClick(Event event) {
        Intent nextActivity;

        // If the event is with marked seats, go to seats picking activity
        if (event.isMarkedSeats()) {
            nextActivity = new Intent(EventDetailsActivity.this, SeatsActivity.class);
        }
        // If the event is not seat-marked, go to no-seats activity
        else {
            nextActivity = new Intent(EventDetailsActivity.this, NoSeatsActivity.class);
        }

        nextActivity.putExtra("eventUid", event.getUid());
        startActivity(nextActivity);
    }

    private void abort() {
        String eventNotFoundErrorMessage = "המופע לא נמצא, נסה שנית";
        Toast.makeText(EventDetailsActivity.this, eventNotFoundErrorMessage, Toast.LENGTH_LONG).show();
        final Intent mainActivity = new Intent(EventDetailsActivity.this, MainActivity.class);

        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(2500);
                    startActivity(mainActivity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }

    private void openHallOfficialWebsite(String hallWebsiteUrl) {
        // Ensure the url is valid with HTTP prefix
        if (!hallWebsiteUrl.startsWith("http://") && !hallWebsiteUrl.startsWith("https://"))
            hallWebsiteUrl = "http://" + hallWebsiteUrl;

        // Open browser with hall's official website
        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        browserIntent.setData(Uri.parse(hallWebsiteUrl));
        startActivity(browserIntent);
    }

    private void openMapLocation(Hall hall) {
        Uri addressUri = Uri.parse("geo:0,0?q=" + Uri.encode(hall.getAddress() + " " + hall.getCity()));
        Intent intent = new Intent(Intent.ACTION_VIEW, addressUri);
        if (intent.resolveActivity(getPackageManager()) != null)
            startActivity(intent);
        else
            Toast.makeText(EventDetailsActivity.this, "יש להתקין יישום מפות", Toast.LENGTH_LONG).show();
    }

    private void setLeftTicketsUI(Event event) {
        final String noTicketsLeft = "לא נותרו כרטיסים למופע זה";
        final String someTicketsLeft = "נותרו %d כרטיסים למופע !";
        final String manyTicketsLeft = "נותרו כרטיסים למופע !";

        if (event.isSoldOut()) {
            binding.ivEventSoldout.setVisibility(View.VISIBLE);
            binding.tvTicketsLeft.setText(noTicketsLeft + ".");
            binding.btBookTickets.setVisibility(View.GONE);
            Toast.makeText(EventDetailsActivity.this, noTicketsLeft, Toast.LENGTH_LONG).show();

        } else if (event.getLeftTicketsNum() < 100) {
            binding.tvTicketsLeft.setText(String.format(someTicketsLeft, event.getLeftTicketsNum()));
        } else {
            binding.tvTicketsLeft.setText(manyTicketsLeft);
        }
    }

    private Intent createShareEventIntent() {
        String newLine = System.getProperty("line.separator");
        String sharedMessage = String.format("היי! רציתי שתסתכל על המופע הזה:%s%s%sחפש אותו ב-GetTix!",
                newLine + newLine, this.event.toString(), newLine + newLine);

        Intent shareIntent =
                ShareCompat.IntentBuilder
                        .from(this)
                        .setType("text/plain")
                        .setChooserTitle("שתף אירוע")
                        .setText(sharedMessage)
                        .getIntent();

        return shareIntent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_event_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        else if (item.getItemId() == R.id.action_share) {
            startActivity(createShareEventIntent());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * These two methods are for handling native Android back button the way we need
     * for keeping the app and order state valid
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
}