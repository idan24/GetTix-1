package app.com.almogrubi.idansasson.gettix.utilities;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import app.com.almogrubi.idansasson.gettix.entities.Hall;

/**
 * Created by idans on 29/10/2017.
 */

public class HallSpinnerAdapter extends ArrayAdapter<Hall> {

    private Context context;
    private ArrayList<Hall> halls;

    public HallSpinnerAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Hall> halls) {
        super(context, resource, halls);
        this.context = context;
        this.halls = halls;
    }

    @Override
    public int getCount() {
        return halls.size();
    }

    public ArrayList<Hall> getValues() {
        ArrayList<Hall> returnedValues = new ArrayList<>(halls.size());
        for (Hall hall : halls)
            returnedValues.add(hall);
        return returnedValues;
    }

    @Nullable
    @Override
    public Hall getItem(int position) {
        return halls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getPosition(@Nullable Hall item) {
        return halls.indexOf(item);
    }

    // Returns position of a hall by its uid
    public int getPosition(String hallUid) {
        for (int i = 0; i < halls.size(); i++) {
            if (halls.get(i).getUid().equals(hallUid))
                return i;
        }

        return -1;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView label = new TextView(context);
        label.setText(halls.get(position).getName());
        return label;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView label = new TextView(context);
        label.setText(halls.get(position).getName());
        return label;
    }
}
