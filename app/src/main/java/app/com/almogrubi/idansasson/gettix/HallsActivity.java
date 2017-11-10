package app.com.almogrubi.idansasson.gettix;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import app.com.almogrubi.idansasson.gettix.entities.Event;
import app.com.almogrubi.idansasson.gettix.entities.Hall;
import app.com.almogrubi.idansasson.gettix.utilities.ManagementScreen;

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
    protected void onSignedInInitialize(FirebaseUser user) {
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
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Context context = v.getContext();
                        Intent hallEditActivityIntent = new Intent(context, HallEditActivity.class);
                        hallEditActivityIntent.putExtra("hallUid", hall.getUid());
                        context.startActivity(hallEditActivityIntent);
                    }
                });
            }
        };

        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {});

        hallsRecyclerView.setAdapter(firebaseRecyclerAdapter);

        firebaseRecyclerAdapter.startListening();
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
