package cl.tofcompany.sift.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import cl.tofcompany.sift.Controllers.Clients.HistoryBookingDetaillClient;
import cl.tofcompany.sift.Model.HistoryBooking;
import cl.tofcompany.sift.Providers.DriverProvider;
import cl.tofcompany.sift.R;

//extendemos firebaseRecyclerAdapter para tener acceso a los datos de histocico enviarlo aal recycleview
public class HistoryBookingClientAdapter extends FirebaseRecyclerAdapter<HistoryBooking, HistoryBookingClientAdapter.ViewHolder>{
    //definicion de variables
    private DriverProvider mDriverProvider;
    private Context mContext;
    //metodo proviene de firebaserecyclerAdapter
    public HistoryBookingClientAdapter(@NonNull @NotNull FirebaseRecyclerOptions<HistoryBooking> options, Context context ) {
        super(options);
        //iniciamos los variables
        mDriverProvider = new DriverProvider();
        mContext = context;
    }

    @Override
    //metodo proviene de firebaserecyclerAdapter
    protected void onBindViewHolder(@NonNull @NotNull HistoryBookingClientAdapter.ViewHolder holder, int position, @NonNull @NotNull HistoryBooking historyBooking) {
        //capturando los datos para mostrarlo en el hictorico
        String id = getRef(position).getKey();
        holder.textViewOrigen.setText(historyBooking.getOrigin());
        holder.textViewDestination.setText(historyBooking.getDestination());
        holder.textViewCalification.setText(String.valueOf(historyBooking.getCalificationClient()));
        //llamamos el objeto driverprovider y utilizamos su metodo de getdriver y añadimos un addlistener de evento firebase
        mDriverProvider.getDriver(historyBooking.getIdDriver()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            //proviene del metodo addlistener
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                //preguntamos si el datos existe en firebase
                if (snapshot.exists()){
                    //iniciams el name y lo pasamos el valor que va a llegar en el nodo de "name" en firebase
                    String name = snapshot.child("name").getValue().toString();
                    //validamos si se hay imagen ya en el nodo image en firebase
                    if (snapshot.hasChild("image")){
                        //pasamos al image el valor que proviene del nodo "image" en firebase
                        String image = snapshot.child("image").getValue().toString();
                        //al final lo pasamos a picaso para el procesamiento del imagen y enviarlo en la vista
                        Picasso.get().load(image).into(holder.imageViewuser);
                    }
                    //enviamos al textview el nombre
                    holder.textViewName.setText(name);
                }
            }

            @Override
            //proviene del metodo addlistener
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        //el holder es la vista de recyclerview que nos dio firebase
        //solo decimos cuando da click sobre el recyclerview que nod envia a otra actividad
       holder.mview.setOnClickListener(v -> {
           //intent para enviarlo a otra actividad
           Intent intent = new Intent(mContext, HistoryBookingDetaillClient.class);
           //esa accion envia los datos de idHistoryBooking en un putExtra y pueden recibirlos en la otra actividad
           intent.putExtra("idHistoryBooking",id);
           //iniciamos la actividad
           mContext.startActivity(intent);
       });

    }

    @NonNull
    @NotNull
    @Override
    //proviene de firebaserecycleadapter
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
       // enviamos los datos del layout al layout de car_history
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_history_booking,parent,false);

        return new ViewHolder(view);
    }
    //proviene de firebaserecycleadapter
    public class ViewHolder extends RecyclerView.ViewHolder{
       //definimos los vatiables que proviene de reclyclerview
        private TextView textViewName;
        private TextView textViewOrigen;
        private TextView textViewDestination;
        private TextView textViewCalification;
        private ImageView imageViewuser;
        private View mview;
        //proviene de firebaserecycleadapter
        public ViewHolder(@NonNull @NotNull View view) {
            super(view);
            mview = view;
            //con el objeto de view podemos acceder a los id de card_history_booking
            textViewName = view.findViewById(R.id.textviewnamehistory);
            textViewOrigen = view.findViewById(R.id.textvieworigenhistory);
            textViewDestination = view.findViewById(R.id.textviewdestinationhistory);
            textViewCalification = view.findViewById(R.id.textviewcalificationhistory);
            imageViewuser = view.findViewById(R.id.imageviewuserhistory);

        }
    }

}
