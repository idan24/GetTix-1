package app.com.almogrubi.idansasson.gettix;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.entities.Order;
import app.com.almogrubi.idansasson.gettix.utilities.DataUtils;

/**
 * Created by idans on 18/11/2017.
 *
 * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
 * a cache of the child views for a forecast item. It's also a convenient place to set an
 * OnClickListener, since it has access to the adapter and the views.
 */
public class EventIncomeViewHolder extends RecyclerView.ViewHolder {

    private TextView tvEventTitle;
    private TextView tvEventDateTime;
    private TextView tvEventHall;
    private TextView tvEventSoldTickets;
    private TextView tvEventIncome;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference ordersDatabaseReference;

    public EventIncomeViewHolder(View itemView) {
        super(itemView);
        this.tvEventTitle = itemView.findViewById(R.id.tv_event_item_title);
        this.tvEventDateTime = itemView.findViewById(R.id.tv_event_item_datetime);
        this.tvEventHall = itemView.findViewById(R.id.tv_event_item_hall);
        this.tvEventSoldTickets = itemView.findViewById(R.id.tv_event_item_sold_tickets);
        this.tvEventIncome = itemView.findViewById(R.id.tv_event_item_income);

        firebaseDatabase = FirebaseDatabase.getInstance();
        ordersDatabaseReference = firebaseDatabase.getReference().child("orders");
    }

    public void bindEvent(Event event) {
        tvEventTitle.setText(event.getTitle());
        tvEventDateTime.setText(DataUtils.convertToUiDateFormat(event.getDate()));
        tvEventHall.setText(String.format("%s, %s", event.getEventHall().getName(), event.getCity()));

        ordersDatabaseReference.child(event.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int soldTicketsNum = 0;
                int ordersCount = 0;
                int totalIncome = 0;

                if (dataSnapshot.exists()) {
                    for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                        Order order = orderSnapshot.getValue(Order.class);

                        // For don't take non-final orders into account in calculations
                        if (order.getStatusAsEnum() != DataUtils.OrderStatus.FINAL)
                            continue;

                        soldTicketsNum += order.getTicketsNum();
                        ordersCount++;
                        totalIncome += order.getTotalPrice();
                    }
                }

                tvEventSoldTickets.setText(String.format("%d כרטיסים נמכרו ל-%d לקוחות", soldTicketsNum, ordersCount));
                tvEventIncome.setText(String.format("הכנסה כוללת: %d₪", totalIncome));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}
