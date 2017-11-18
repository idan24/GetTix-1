package app.com.almogrubi.idansasson.gettix.utilities;

import android.content.Context;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.LeadingMarginSpan;
import android.widget.TextView;

import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import java.util.ArrayList;
import java.util.Random;

import app.com.almogrubi.idansasson.gettix.CancelOrderJobService;
import app.com.almogrubi.idansasson.gettix.R;

/**
 * Created by idans on 22/10/2017.
 */

public class Utils {

    public static String INDEXED_KEY_DIVIDER = "~";
    public static int FIRST_LINE_INDENT = 0;
    public static int PARAGRAPH_INDENT = 10;

    public enum SeatStatus {
        AVAILABLE,
        OCCUPIED,
        CHOSEN
    }

    public static int lookupImageBySeatStatus(SeatStatus seatStatus) {
        switch (seatStatus) {
            case AVAILABLE:
                return R.drawable.ic_seat_available;
            case OCCUPIED:
                return R.drawable.ic_seat_occupied;
            case CHOSEN:
                return R.drawable.ic_seat_chosen;
            default:
                return -1;
        }
    }

    public static int lookupImageByCategory(DataUtils.Category category) {
        switch (category) {
            case MUSIC:
                return R.drawable.ic_guitar;
            case THEATER:
                return R.drawable.ic_mask;
            case DANCE:
                return R.drawable.ic_dance;
            case CHILDREN:
                return R.drawable.ic_teddybear;
            case COMEDY:
                return R.drawable.ic_lol;
            case LECTURES:
                return R.drawable.ic_lecture;
            case SPORTS:
                return R.drawable.ic_football;
            default:
                return -1;
        }
    }

    public static String CLOUDINARY_PRESET = "idansass_preset";

    /*
     * Loads a responsive image from Cloudinary
     */
    public static String getTransformedCloudinaryImageUrl(int width, int height, String imageUrl, String cropMode) {
        return MediaManager.get().url()
                .transformation(new Transformation().width(width).height(height).crop(cropMode).gravity("faces"))
                .generate(imageUrl);
    }

    // This is the key our app uses for Stripe (credit card server)
    public static String STRIPE_PUBLISHABLE_KEY = "pk_test_n0iIsmn8y14YOk399U74T4sO";

    /*
     * Useful for validation of text input views (EditText, AutoCompleteTextView etc.)
     */
    public static boolean isTextViewEmpty(TextView textView) {
        return textView.getText().toString().trim().isEmpty();
    }

    /*
     * Converts a string to a paragraph with indentation
     */
    public static SpannableString createIndentedText(String text, int marginFirstLine, int marginNextLines) {
        SpannableString result = new SpannableString(text);
        result.setSpan(new LeadingMarginSpan.Standard(marginFirstLine, marginNextLines),0, text.length(),0);
        return result;
    }

    // Used for generating order confirmation number when an order is final
    public static int ORDER_CONFIRMATION_NUMBER_LENGTH = 5;

    /*
     * Generates a random numeric String of given length
     */
    public static String generateRandomString(int length, Random random) {
        if (length < 1) throw new IllegalArgumentException();

        char[] digits = "0123456789".toCharArray();
        char[] buffer = new char[length];

        for (int i = 0; i < buffer.length; i++)
            buffer[i] = digits[random.nextInt(digits.length)];
        return new String(buffer);
    }

    /*
     * Generates a representable string for an order's seats
     */
    public static String generateOrderSeatsUIString(boolean[][] orderSeats) {
        StringBuilder completeUIString = new StringBuilder();
        String newLine = System.getProperty("line.separator");

        // For every row
        for (int i = 0; i < orderSeats.length; i++) {
            StringBuilder rowSeatsString = new StringBuilder();
            ArrayList<Integer> rowChosenSeats = new ArrayList<>();

            // Add the chosen seats from the row to list
            for (int j = 0; j < orderSeats[i].length; j++)
                if (orderSeats[i][j])
                    rowChosenSeats.add(j);

            // If no seats were chosen from this row, move on to next row
            if (rowChosenSeats.isEmpty()) continue;

            // If there is one chosen seat in this row
            if (rowChosenSeats.size() == 1)
                rowSeatsString.append(String.format("שורה %d מושב %d", i + 1, rowChosenSeats.get(0) + 1));
                // If there are more than 1 chosen seats in this row
            else {
                rowSeatsString.append(String.format("שורה %d מושבים ", i + 1));

                // Go through row's chosen seats
                for (int k = 0; k < rowChosenSeats.size(); k++) {
                    // We distinct first chosen seat from others in same row for friendly UI purposes
                    if (k == 0)
                        rowSeatsString.append(String.format("%d", rowChosenSeats.get(k) + 1));
                    else
                        rowSeatsString.append(String.format(",%d", rowChosenSeats.get(k) + 1));
                }
            }

            // Add row string to final string
            completeUIString.append(rowSeatsString);
            completeUIString.append(newLine);
        }

        return completeUIString.toString();
    }

    public static void fireCancelOrderService(Context context, String orderUid, String eventUid, boolean isMarkedSeats) {
        // Create a new dispatcher using the Google Play driver.
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));

        Bundle cancelOrderJobServiceExtrasBundle = new Bundle();
        cancelOrderJobServiceExtrasBundle.putString("order_uid", orderUid);
        cancelOrderJobServiceExtrasBundle.putString("event_uid", eventUid);
        cancelOrderJobServiceExtrasBundle.putBoolean("event_marked_seats", isMarkedSeats);

        Job myJob = dispatcher.newJobBuilder()
                // the JobService that will be called
                .setService(CancelOrderJobService.class)
                // uniquely identifies the job
                .setTag("cancel-order-" + orderUid)
                // one-off job
                .setRecurring(false)
                // persist past a device reboot
                .setLifetime(Lifetime.FOREVER)
                // start in 10-11 minutes from now
                .setTrigger(Trigger.executionWindow(600, 660))
                //.setTrigger(Trigger.executionWindow(5, 15))
                // don't overwrite an existing job with the same tag
                .setReplaceCurrent(false)
                // retry with exponential backoff
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                // constraints that need to be satisfied for the job to run
                .setConstraints(
                        // run on any network
                        Constraint.ON_ANY_NETWORK
                )
                .setExtras(cancelOrderJobServiceExtrasBundle)
                .build();

        dispatcher.mustSchedule(myJob);
    }

    /*
     * Converts an Iterable of elements into an ArrayList
     */
    public static <E> ArrayList<E> toArrayList(Iterable<E> iterable) {
        if (iterable instanceof ArrayList) {
            return (ArrayList<E>) iterable;
        }

        ArrayList<E> list = new ArrayList<E>();

        if (iterable != null) {
            for(E e: iterable) {
                list.add(e);
            }
        }

        return list;
    }
}
