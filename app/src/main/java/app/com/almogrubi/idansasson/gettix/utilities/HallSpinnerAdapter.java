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
    private ArrayList<Hall> values;

    public HallSpinnerAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Hall> values) {
        super(context, resource, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public int getCount() {
        return values.size();
    }

    public ArrayList<Hall> getValues() {
        ArrayList<Hall> returnedValues = new ArrayList<>(values.size());
        for (Hall hall : values)
            returnedValues.add(hall);
        return returnedValues;
    }

    @Nullable
    @Override
    public Hall getItem(int position) {
        return values.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getPosition(@Nullable Hall item) {
        return values.indexOf(item);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView label = new TextView(context);
        label.setText(values.get(position).getName());
        return label;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView label = new TextView(context);
        label.setText(values.get(position).getName());
        return label;
    }
}
