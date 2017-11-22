package app.com.almogrubi.idansasson.gettix.views;

import android.content.Context;

import app.com.almogrubi.idansasson.gettix.utilities.Utils;

/*
 * An ImageView that represents a seat in an event with marked seats
 */
public class SeatImageView extends android.support.v7.widget.AppCompatImageView {

    private int row;
    private int number;
    private Utils.SeatStatus status;

    public SeatImageView(Context context, int row, int number, Utils.SeatStatus status) {
        super(context);
        this.row = row;
        this.number = number;
        this.status = status;
    }

    public int getRow() {
        return row;
    }

    public int getNumber() {
        return number;
    }

    public Utils.SeatStatus getStatus() {
        return status;
    }

    public void setStatus(Utils.SeatStatus status) {
        this.status = status;

        // Force UI update
        invalidate();
        requestLayout();
    }
}
