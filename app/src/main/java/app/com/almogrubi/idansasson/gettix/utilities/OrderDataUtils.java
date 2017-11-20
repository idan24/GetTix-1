package app.com.almogrubi.idansasson.gettix.utilities;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.entities.Order;

/**
 * Created by idans on 19/11/2017.
 */

public class OrderDataUtils {

    public static void cancelOrder(String eventUid, boolean isEventMarkedSeats, Order order) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference ordersDatabaseReference = firebaseDatabase.getReference().child("orders");

        // Update: "orders / $ eventUid / $ orderUid / status = FINAL"
        ordersDatabaseReference
                .child(eventUid)
                .child(order.getUid())
                .child("status").setValue(DataUtils.OrderStatus.CANCELLED.name());

        updateEventLeftTicketsNum(eventUid, order.getTicketsNum());

        if (isEventMarkedSeats) {
            freeEventAndOrderSeats(eventUid, order.getUid());
        }
    }

    private static void updateEventLeftTicketsNum(final String eventUid, final int orderTicketsNum) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference eventsDatabaseReference = firebaseDatabase.getReference().child("events");

        eventsDatabaseReference.child(eventUid).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Event event = mutableData.getValue(Event.class);
                if (event == null)
                    return Transaction.success(mutableData);

                int newLeftTicketsNum = event.getLeftTicketsNum() + orderTicketsNum;
                mutableData.child("leftTicketsNum").setValue(newLeftTicketsNum);

                if (event.isSoldOut())
                    mutableData.child("soldOut").setValue(false);

                // Report transaction success
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {}
        });
    }

    private static void freeEventAndOrderSeats(final String eventUid, final String orderUid) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference eventSeatsDatabaseReference = firebaseDatabase.getReference().child("event_seats");
        final DatabaseReference orderSeatsDatabaseReference = firebaseDatabase.getReference().child("order_seats");

        orderSeatsDatabaseReference.child(orderUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Map orderSeatsData = new HashMap();

                            // Going through all order's rows
                            for (DataSnapshot rowSnapshot : dataSnapshot.getChildren()) {
                                // Going through all order's seats in this row
                                for (DataSnapshot seatSnapshot : rowSnapshot.getChildren()) {
                                    // Free the seat, as we have cancelled the order
                                    orderSeatsData.put(
                                            String.format("%s/%s", rowSnapshot.getKey(), seatSnapshot.getKey()),
                                            false);
                                }
                            }

                            eventSeatsDatabaseReference.child(eventUid).updateChildren(orderSeatsData);

                            orderSeatsDatabaseReference.child(orderUid).removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
    }
}
