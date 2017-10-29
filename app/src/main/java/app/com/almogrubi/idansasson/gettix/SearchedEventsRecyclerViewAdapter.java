package app.com.almogrubi.idansasson.gettix;

import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.entities.Hall;
import app.com.almogrubi.idansasson.gettix.utilities.DataUtils;
import app.com.almogrubi.idansasson.gettix.utilities.Utils;

/**
 * Created by almogrubi on 10/14/17.
 */

//    public void filter(Date date, String hallName, DataUtils.Category category, String city, String keyword) {

//        keyword = keyword.toLowerCase(Locale.getDefault());
//        eventList.clear();
//
//        if (date == null && hallName.isEmpty() && category == null && city.isEmpty() && keyword.isEmpty()) {
//            eventList.addAll(eventArrayList);
//        }
//        else {
//            for (final Event event : eventArrayList) {
//                if ((date != null) && (!date.equals(event.getDateTime())))
//                    continue;
//                else if (!hallName.isEmpty()) {
//                    DatabaseReference hallsDatabaseReference = firebaseDatabase.getReference().child("halls");
//                    Query query = hallsDatabaseReference.orderByChild("name").equalTo(hallName).limitToFirst(1);
//                    query.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            if (dataSnapshot.exists()) {
//                                //Hall hall = (Hall)dataSnapshot.getValue();
//                                Hall hall = dataSnapshot.getChildren().iterator().next().getValue(Hall.class);
//                                if (event.getHallId().equals(hall.getUid()))
//                                    eventList.add(event);
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {}
//                    });
//                    continue;
//                }
//                else if ((category != null) && (category != event.getCategory()))
//                    continue;
//                else if ((!city.isEmpty()) && (!event.getCity().contains(city)))
//                    continue;
//                else if ((!keyword.isEmpty()) &&
//                         (!event.getTitle().contains(keyword)) &&
//                         (!event.getDescription().contains(keyword)) &&
//                         (!event.getPerformer().contains(keyword)))
//                    continue;
//
//                eventList.add(event);
//            }
//        }
//
//        notifyDataSetChanged();
//    }
//}
