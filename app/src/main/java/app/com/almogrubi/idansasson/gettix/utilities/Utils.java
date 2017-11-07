package app.com.almogrubi.idansasson.gettix.utilities;

import android.text.SpannableString;
import android.text.style.LeadingMarginSpan;
import android.widget.TextView;

import app.com.almogrubi.idansasson.gettix.R;

/**
 * Created by idans on 22/10/2017.
 */

public class Utils {

    public static String INDEXED_KEY_DIVIDER = "~";
    public static int FIRST_LINE_INDENT = 0;
    public static int PARAGRAPH_INDENT = 10;

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

    // Useful for validation of text input views (EditText, AutoCompleteTextView etc.)
    public static boolean isTextViewEmpty(TextView textView) {
        return textView.getText().toString().trim().isEmpty();
    }

    public static SpannableString createIndentedText(String text, int marginFirstLine, int marginNextLines) {
        SpannableString result = new SpannableString(text);
        result.setSpan(new LeadingMarginSpan.Standard(marginFirstLine, marginNextLines),0, text.length(),0);
        return result;
    }
}
