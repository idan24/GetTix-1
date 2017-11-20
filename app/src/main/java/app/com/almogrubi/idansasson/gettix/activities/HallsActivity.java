package app.com.almogrubi.idansasson.gettix.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import app.com.almogrubi.idansasson.gettix.viewholders.HallViewHolder;
import app.com.almogrubi.idansasson.gettix.R;
import app.com.almogrubi.idansasson.gettix.entities.Hall;
import app.com.almogrubi.idansasson.gettix.authentication.ManagementScreen;

public class HallsActivity extends ManagementScreen {

    private RecyclerView hallsRecyclerView;
    private LinearLayoutManager linearLayoutManager;

    private DatabaseReference firebaseDatabaseReference;
    private DatabaseReference hallsDatabaseReference;
    private FirebaseRecyclerAdapter<Hall, HallViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_halls);

        hallsRecyclerView = findViewById(R.id.halls_recycler_view);

        linearLayoutManager = new LinearLayoutManager(this);
        hallsRecyclerView.setLayoutManager(linearLayoutManager);

        // New child entries
        firebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        hallsDatabaseReference = firebaseDatabaseReference.child("halls");
    }

    @Override
    protected void onSignedInInitialize(final FirebaseUser user) {
        super.onSignedInInitialize(user);

        SnapshotParser<Hall> parser = new SnapshotParser<Hall>() {
            @Override
            public Hall parseSnapshot(DataSnapshot dataSnapshot) {
                Hall hall = dataSnapshot.getValue(Hall.class);
                if (hall != null) {
                    hall.setUid(dataSnapshot.getKey());
                }
                return hall;
            }
        };

        // Displaying all halls
        FirebaseRecyclerOptions<Hall> options =
                new FirebaseRecyclerOptions.Builder<Hall>()
                        .setQuery(hallsDatabaseReference.orderByChild("name"), parser)
                        .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Hall, HallViewHolder>(options) {
            @Override
            public HallViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new HallViewHolder(inflater.inflate(R.layout.hall_list_item, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(final HallViewHolder viewHolder,
                                            int position,
                                            final Hall hall) {
                viewHolder.bindHall(hall);

                // Only if the hall is owned by the logged-in user, allow editing option
                if (hall.getProducerId().equals(user.getUid())) {
                    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showHallEditPopup(v, hall);
                        }
                    });
                }
            }
        };

        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {});

        hallsRecyclerView.setAdapter(firebaseRecyclerAdapter);

        firebaseRecyclerAdapter.startListening();
    }

    private void showHallEditPopup(final View hallView, final Hall hall) {

        PopupMenu hallEditPopup = new PopupMenu(HallsActivity.this, hallView);
        hallEditPopup.getMenuInflater().inflate(R.menu.popup_menu_managed_hall, hallEditPopup.getMenu());

        hallEditPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                Context context = hallView.getContext();
                Intent hallEditActivityIntent = new Intent(context, HallEditActivity.class);

                if (itemId == R.id.action_edit_hall) {
                    hallEditActivityIntent.putExtra("hallUid", hall.getUid());
                    context.startActivity(hallEditActivityIntent);
                }

                return true;
            }
        });

        hallEditPopup.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_manager_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_item) {
            startActivity(new Intent(this, HallEditActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (firebaseRecyclerAdapter != null)
            firebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (firebaseRecyclerAdapter != null)
            firebaseRecyclerAdapter.stopListening();
    }
}
