package app.com.almogrubi.idansasson.gettix.dataservices;

import android.content.Context;
import android.os.Bundle;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.entities.Order;
import app.com.almogrubi.idansasson.gettix.services.CancelOrderJobService;

/**
 * Created by idans on 19/11/2017.
 */

public class OrderDataService {

    // Used for generating order confirmation number when an order is final
    public static int ORDER_CONFIRMATION_NUMBER_LENGTH = 5;

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

                if (event.isSoldOut() && newLeftTicketsNum > 0)
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
}
