package app.com.almogrubi.idansasson.gettix.activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import app.com.almogrubi.idansasson.gettix.viewholders.OrderViewHolder;
import app.com.almogrubi.idansasson.gettix.R;
import app.com.almogrubi.idansasson.gettix.entities.Order;
import app.com.almogrubi.idansasson.gettix.dataservices.DataUtils;
import app.com.almogrubi.idansasson.gettix.authentication.ManagementScreen;

public class EventOrdersActivity extends ManagementScreen {

    private TextView tvEventTitle;
    private TextView tvTotalTicketsNum;
    private TextView tvLeftTicketsNum;
    private TextView tvOrdersTitle;
    private RecyclerView ordersRecyclerView;
    private LinearLayoutManager linearLayoutManager;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference ordersDatabaseReference;

    private FirebaseRecyclerAdapter<Order, OrderViewHolder> firebaseRecyclerAdapter;

    private String eventUid;
    private int eventTotalTicketsNum;
    private int eventLeftTicketsNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_orders);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        tvEventTitle = findViewById(R.id.tv_event_orders_headline);
        tvTotalTicketsNum = findViewById(R.id.tv_total_tickets_num);
        tvLeftTicketsNum = findViewById(R.id.tv_left_tickets_num);
        tvOrdersTitle = findViewById(R.id.tv_event_orders);
        ordersRecyclerView = findViewById(R.id.orders_recycler_view);

        linearLayoutManager = new LinearLayoutManager(this);
        ordersRecyclerView.setLayoutManager(linearLayoutManager);

        // Initialization of all needed Firebase database references
        initializeDatabaseReferences();

        Intent intent = this.getIntent();
        if (intent != null) {
            if (intent.hasExtra("eventUid"))
                this.eventUid = intent.getStringExtra("eventUid");
            if (intent.hasExtra("eventTitle"))
                tvEventTitle.setText(intent.getStringExtra("eventTitle"));
            if (intent.hasExtra("eventTotalTicketsNum")) {
                this.eventTotalTicketsNum = intent.getIntExtra("eventTotalTicketsNum", 0);
                tvTotalTicketsNum.setText("מספר כרטיסים כולל: " + this.eventTotalTicketsNum);
            }
            if (intent.hasExtra("eventLeftTicketsNum")) {
                this.eventLeftTicketsNum = intent.getIntExtra("eventLeftTicketsNum", 0);
                tvLeftTicketsNum.setText("מספר כרטיסים שנותרו: " + this.eventLeftTicketsNum);
            }
        }

        if (eventLeftTicketsNum == eventTotalTicketsNum)
            tvOrdersTitle.setText("לא קיימות הזמנות למופע כעת.");
    }

    private void initializeDatabaseReferences() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        ordersDatabaseReference = firebaseDatabase.getReference().child("orders");
    }

    @Override
    protected void onSignedInInitialize(FirebaseUser user) {
        super.onSignedInInitialize(user);

        tvOrdersTitle.setVisibility(View.VISIBLE);

        SnapshotParser<Order> parser = new SnapshotParser<Order>() {
            @Override
            public Order parseSnapshot(DataSnapshot dataSnapshot) {
                Order order = dataSnapshot.getValue(Order.class);
                if (order != null) {
                    order.setUid(dataSnapshot.getKey());
                }
                return order;
            }
        };

        // Displaying all final orders for the event
        FirebaseRecyclerOptions<Order> options =
                new FirebaseRecyclerOptions.Builder<Order>()
                        .setQuery(ordersDatabaseReference.child(this.eventUid)
                                        .orderByChild("status").equalTo(DataUtils.OrderStatus.FINAL.name()),
                                parser)
                        .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Order, OrderViewHolder>(options) {
            @Override
            public OrderViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new OrderViewHolder(inflater.inflate(R.layout.order_list_item, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(final OrderViewHolder viewHolder,
                                            int position,
                                            final Order order) {
                viewHolder.bindOrder(order);
            }
        };

        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {});

        ordersRecyclerView.setAdapter(firebaseRecyclerAdapter);

        firebaseRecyclerAdapter.startListening();
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
