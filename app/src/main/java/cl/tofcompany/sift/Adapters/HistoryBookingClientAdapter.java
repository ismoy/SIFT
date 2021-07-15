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

public class HistoryBookingClientAdapter extends FirebaseRecyclerAdapter<HistoryBooking, HistoryBookingClientAdapter.ViewHolder>{

    private DriverProvider mDriverProvider;
    private Context mContext;
    public HistoryBookingClientAdapter(@NonNull @NotNull FirebaseRecyclerOptions<HistoryBooking> options, Context context ) {
        super(options);
        mDriverProvider = new DriverProvider();
        mContext = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull @NotNull HistoryBookingClientAdapter.ViewHolder holder, int position, @NonNull @NotNull HistoryBooking historyBooking) {

        String id = getRef(position).getKey();
        holder.textViewOrigen.setText(historyBooking.getOrigin());
        holder.textViewDestination.setText(historyBooking.getDestination());
        holder.textViewCalification.setText(String.valueOf(historyBooking.getCalificationClient()));
        mDriverProvider.getDriver(historyBooking.getIdDriver()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String name = snapshot.child("name").getValue().toString();
                    if (snapshot.hasChild("image")){
                        String image = snapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(holder.imageViewuser);
                    }
                    holder.textViewName.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
       holder.mview.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent intent = new Intent(mContext, HistoryBookingDetaillClient.class);
               intent.putExtra("idHistoryBooking",id);
               mContext.startActivity(intent);
           }
       });

    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_history_booking,parent,false);

        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView textViewName;
        private TextView textViewOrigen;
        private TextView textViewDestination;
        private TextView textViewCalification;
        private ImageView imageViewuser;
        private View mview;
        public ViewHolder(@NonNull @NotNull View view) {
            super(view);
            mview = view;
            textViewName = view.findViewById(R.id.textviewnamehistory);
            textViewOrigen = view.findViewById(R.id.textvieworigenhistory);
            textViewDestination = view.findViewById(R.id.textviewdestinationhistory);
            textViewCalification = view.findViewById(R.id.textviewcalificationhistory);
            imageViewuser = view.findViewById(R.id.imageviewuserhistory);

        }
    }

}
