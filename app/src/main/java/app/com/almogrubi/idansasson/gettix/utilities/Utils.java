package app.com.almogrubi.idansasson.gettix.utilities;

import android.text.SpannableString;
import android.text.style.LeadingMarginSpan;
import android.widget.TextView;

import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;

import java.util.ArrayList;
import java.util.Random;

import app.com.almogrubi.idansasson.gettix.dataservices.DataUtils;
import app.com.almogrubi.idansasson.gettix.R;

public class Utils {

    // This enum represents all seat statuses
    public enum SeatStatus {
        AVAILABLE,
        OCCUPIED,
        CHOSEN
    }

    // A pre-determined sign that divides between indexed keys in the event indexed tables
    public static String INDEXED_KEY_DIVIDER = "~";
    public static int FIRST_LINE_INDENT = 0;
    public static int PARAGRAPH_INDENT = 10;

    // This is the key our app uses for Stripe (credit card server)
    public static String STRIPE_PUBLISHABLE_KEY = "pk_test_n0iIsmn8y14YOk399U74T4sO";

    // This is the preset our app uses for loading responsive images from Cloudinary
    public static String CLOUDINARY_PRESET = "idansass_preset";

    /*
     * Loads a responsive image from Cloudinary
     */
    public static String getTransformedCloudinaryImageUrl(DataUtils.Category eventCategory, int width, int height,
                                                          String imageUrl, String cropMode) {
        String gravity = eventCategory == DataUtils.Category.SPORTS ? "center" : "faces";

        return MediaManager.get().url()
                .transformation(new Transformation().width(width).height(height).crop(cropMode).gravity(gravity))
                .generate(imageUrl);
    }

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
     * Returns image resource according to seat status
     */
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

    /*
     * Returns image resource according to event category
     */
    public static int lookupImageByCategory(DataUtils.Category category) {
        switch (category) {
            case MUSIC:
                return R.drawable.ic_guitar;
            case THEATER:
                return R.drawable.ic_mask;
            case DANCE:
                return R.drawable.ic_dance;
            case CHILDREN:
                return R.drawable.ic_kids;
            case COMEDY:
                return R.drawable.ic_comedy;
            case LECTURES:
                return R.drawable.ic_lecture;
            case SPORTS:
                return R.drawable.ic_football;
            default:
                return -1;
        }
    }
}
