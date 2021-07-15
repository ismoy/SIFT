package cl.tofcompany.sift.Controllers.Clients;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import cl.tofcompany.sift.Adapters.HistoryBookingClientAdapter;
import cl.tofcompany.sift.Model.HistoryBooking;
import cl.tofcompany.sift.Providers.AuthProvider;
import cl.tofcompany.sift.R;
import cl.tofcompany.sift.includes.MyToolbar;

public class HistoryBookingClient extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private HistoryBookingClientAdapter mAdapter;
    private AuthProvider mAuthProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_booking_client);
        MyToolbar.show(this,"SIFT",true);
        mRecyclerView = findViewById(R.id.recyclerviewhistoryclient);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuthProvider = new AuthProvider();
        Query query = FirebaseDatabase.getInstance().getReference()
                .child("HistoryBooking")
                .orderByChild("idClient")
                .equalTo(mAuthProvider.getId());
        FirebaseRecyclerOptions<HistoryBooking> options = new FirebaseRecyclerOptions.Builder<HistoryBooking>()
                .setQuery(query, HistoryBooking.class)
                .build();
        mAdapter = new HistoryBookingClientAdapter(options, HistoryBookingClient.this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }
}