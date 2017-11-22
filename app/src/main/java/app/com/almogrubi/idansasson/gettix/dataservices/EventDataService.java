package app.com.almogrubi.idansasson.gettix.dataservices;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import app.com.almogrubi.idansasson.gettix.entities.Event;

import static app.com.almogrubi.idansasson.gettix.utilities.Utils.INDEXED_KEY_DIVIDER;

public class EventDataService {

    public static void RemoveEventFromDB(Event event, String producerUid) {

        // Firebase database references
        DatabaseReference firebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference eventsDatabaseReference = firebaseDatabaseReference.child("events");
        DatabaseReference producerEventsDatabaseReference = firebaseDatabaseReference.child("producer_events");
        DatabaseReference hallEventsDatabaseReference = firebaseDatabaseReference.child("hall_events");
        DatabaseReference hallEventDatesDatabaseReference = firebaseDatabaseReference.child("hall_eventDates");
        DatabaseReference dateEventsDatabaseReference = firebaseDatabaseReference.child("date_events");
        DatabaseReference cityEventsDatabaseReference = firebaseDatabaseReference.child("city_events");
        DatabaseReference categoryEventsDatabaseReference = firebaseDatabaseReference.child("category_events");
        DatabaseReference categoryDateEventsDatabaseReference = firebaseDatabaseReference.child("category_date_events");
        DatabaseReference categoryCityEventsDatabaseReference = firebaseDatabaseReference.child("category_city_events");
        DatabaseReference categoryHallEventsDatabaseReference = firebaseDatabaseReference.child("category_hall_events");
        DatabaseReference eventSeatsDatabaseReference = firebaseDatabaseReference.child("event_seats");

        String eventUid = event.getUid();

        // Removing from event_seats if needed
        if (event.isMarkedSeats())
            eventSeatsDatabaseReference.child(eventUid).removeValue();

        // Removing from all indexed tables
        producerEventsDatabaseReference.child(producerUid).child(eventUid).removeValue();
        dateEventsDatabaseReference.child(event.getDate()).child(eventUid).removeValue();
        cityEventsDatabaseReference.child(event.getCity()).child(eventUid).removeValue();
        hallEventsDatabaseReference.child(event.getEventHall().getUid()).child(eventUid).removeValue();
        categoryEventsDatabaseReference.child(event.getCategory()).child(eventUid).removeValue();
        categoryDateEventsDatabaseReference.child(event.getCategory() + INDEXED_KEY_DIVIDER + event.getDate())
                .child(eventUid).removeValue();
        categoryCityEventsDatabaseReference.child(event.getCategory() + INDEXED_KEY_DIVIDER + event.getCity())
                .child(eventUid).removeValue();
        categoryHallEventsDatabaseReference.child(event.getCategory() + INDEXED_KEY_DIVIDER + event.getEventHall().getUid())
                .child(eventUid).removeValue();
        hallEventDatesDatabaseReference.child(event.getEventHall().getUid()).child(event.getDate()).removeValue();

        // Finally, removing from main events table
        eventsDatabaseReference.child(eventUid).removeValue();
    }
}
