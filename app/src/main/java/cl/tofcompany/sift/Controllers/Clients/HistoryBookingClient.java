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
   //variable de recyclerview
    private RecyclerView mRecyclerView;
    //variable de la clase de HistoryBookingClientAdapter
    private HistoryBookingClientAdapter mAdapter;
    //variable de la clase de mAuthProvider
    private AuthProvider mAuthProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_booking_client);
        init();
    }
    //aqui estan inicializados los variable y metodos
    private void init(){
        //llamamos al toolbar
        MyToolbar.show(this,"SIFT",true);
        //iniciamos el recyclerview con si id
        mRecyclerView = findViewById(R.id.recyclerviewhistoryclient);
        //llamamos el LineaLayoutManager para mostrarlo en este contexto this
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //enviamos todos qye viene en recyclerview al layoutmanager
        mRecyclerView.setLayoutManager(linearLayoutManager);
    }
    @Override
    //metodo onstat
    protected void onStart() {
        super.onStart();
        //iniciando la classe de AuthProvider
        mAuthProvider = new AuthProvider();
        // creamos un query para solicitar datos en firebase
        Query query = FirebaseDatabase.getInstance().getReference()
                //entramos en el nodo HistoryBooking
                .child("HistoryBooking")
                //entramos en el nodo idClient
                .orderByChild("idClient")
                //igualamos al id
                .equalTo(mAuthProvider.getId());
        //alli empezamos a llenar firebaseRecyclerOptions
        FirebaseRecyclerOptions<HistoryBooking> options = new FirebaseRecyclerOptions.Builder<HistoryBooking>()
                //lo pasamos el query
                .setQuery(query, HistoryBooking.class)
                .build();
        //iniciamos Madapter a los valores de HistoryBookingClientAdapter en este contexto this
        mAdapter = new HistoryBookingClientAdapter(options, HistoryBookingClient.this);
        //enviamos los que estan en mrecyclerview al madapter
        mRecyclerView.setAdapter(mAdapter);
        //empezamos a escuchar el evento
        mAdapter.startListening();
    }

    @Override
    //metodo onstop
    protected void onStop() {
        super.onStop();
        //despues de cada consulta paramos el evento
        mAdapter.stopListening();
    }
}