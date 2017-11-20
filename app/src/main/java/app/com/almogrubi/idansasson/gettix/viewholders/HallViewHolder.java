package app.com.almogrubi.idansasson.gettix.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import app.com.almogrubi.idansasson.gettix.R;
import app.com.almogrubi.idansasson.gettix.entities.Hall;

/**
 * Created by idans on 10/11/2017.
 *
 * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
 * a cache of the child views for a forecast item. It's also a convenient place to set an
 * OnClickListener, since it has access to the adapter and the views.
 */
public class HallViewHolder extends RecyclerView.ViewHolder {

    private TextView tvHallName;
    private TextView tvHallAddress;

    public HallViewHolder(View itemView) {
        super(itemView);
        this.tvHallName = itemView.findViewById(R.id.tv_hall_item_name);
        this.tvHallAddress = itemView.findViewById(R.id.tv_hall_item_address);
    }

    public void bindHall(Hall hall) {
        tvHallName.setText(hall.getName());
        tvHallAddress.setText(String.format("%s, %s", hall.getAddress(), hall.getCity()));
    }
}
