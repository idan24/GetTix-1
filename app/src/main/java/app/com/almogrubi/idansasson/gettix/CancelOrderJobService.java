package app.com.almogrubi.idansasson.gettix;

import android.os.AsyncTask;
import android.os.Bundle;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.entities.Order;
import app.com.almogrubi.idansasson.gettix.utilities.DataUtils;

public class CancelOrderJobService extends JobService {

    private AsyncTask backgroundTask;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference eventsDatabaseReference;
    private DatabaseReference eventSeatsDatabaseReference;
    private DatabaseReference ordersDatabaseReference;
    private DatabaseReference orderSeatsDatabaseReference;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize all needed Firebase database references
        firebaseDatabase = FirebaseDatabase.getInstance();
        eventsDatabaseReference = firebaseDatabase.getReference().child("events");
        eventSeatsDatabaseReference = firebaseDatabase.getReference().child("event_seats");
        ordersDatabaseReference = firebaseDatabase.getReference().child("orders");
        orderSeatsDatabaseReference = firebaseDatabase.getReference().child("order_seats");
    }

    @Override
    public boolean onStartJob(final JobParameters job) {

        backgroundTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {

                Bundle extras = job.getExtras();
                final String orderUid = extras.getString("order_uid");
                final String eventUid = extras.getString("event_uid");
                final boolean isEventMarkedSeats = extras.getBoolean("event_marked_seats");

                // Query the order that needs to be checked
                ordersDatabaseReference
                        .child(eventUid)
                        .child(orderUid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // If we have a null result, the order was somehow not found in the database
                                if (dataSnapshot == null || !dataSnapshot.exists() || dataSnapshot.getValue() == null) {
                                    return;
                                }

                                // If we reached here then the order was found
                                Order order = dataSnapshot.getValue(Order.class);

                                // We only have work to do if the order is still marked as "in process" after 10 min
                                // If the order is final or cancelled by now, we can abort
                                if (order.getStatusAsEnum() == DataUtils.OrderStatus.IN_PROGRESS) {

                                    // Update: "orders / $ eventUid / $ orderUid / status = FINAL"
                                    ordersDatabaseReference
                                            .child(eventUid)
                                            .child(orderUid)
                                            .child("status").setValue(DataUtils.OrderStatus.CANCELLED.name());

                                    updateEventLeftTicketsNum(eventUid, order.getTicketsNum());

                                    if (isEventMarkedSeats) {
                                        // TODO: free event seats (according to order seats)
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {}
                        });

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                jobFinished(job, false);
            }
        };

        backgroundTask.execute();
        return true;
    }

    private void updateEventLeftTicketsNum(final String eventUid, final int orderTicketsNum) {
        // Query the event that needs to be updated
        eventsDatabaseReference
                .child(eventUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // If we have a null result, the event was somehow not found in the database
                        if (dataSnapshot == null || !dataSnapshot.exists() || dataSnapshot.getValue() == null) {
                            return;
                        }

                        // If we reached here then the event was found
                        Event event = dataSnapshot.getValue(Event.class);
                        int eventLeftTicketsNum = event.getLeftTicketsNum();

                        // Update: "events / $ eventUid / leftTicketNum += orderTicketsNum
                        eventsDatabaseReference
                                .child(eventUid)
                                .child("leftTicketsNum").setValue(eventLeftTicketsNum + orderTicketsNum);

                        // If the event was marked as sold-out before, now that tickets were returned it is back
                        // to selling state
                        if (event.isSoldOut()) {
                            eventsDatabaseReference
                                    .child(eventUid)
                                    .child("soldOut").setValue(false);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        if (backgroundTask != null)
            backgroundTask.cancel(true);
        return true;
    }
}
