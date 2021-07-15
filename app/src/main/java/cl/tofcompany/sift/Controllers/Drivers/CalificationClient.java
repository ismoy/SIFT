package cl.tofcompany.sift.Controllers.Drivers;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

import cl.tofcompany.sift.Model.ClientBooking;
import cl.tofcompany.sift.Model.HistoryBooking;
import cl.tofcompany.sift.Providers.ClientBookingProvider;
import cl.tofcompany.sift.Providers.HistoryBookingProvider;
import cl.tofcompany.sift.R;

public class CalificationClient extends AppCompatActivity {
private TextView mteTextViewOrigin;
private TextView mTextViewDestination;
    private TextView mTextViewprice;
private RatingBar mRatingBar;
private Button mButtonCalification;
private ClientBookingProvider mClientBookingProvider;
private String mExtraClientId;
private HistoryBooking mHistoryBooking;
private HistoryBookingProvider mHistoryBookingProvider;
private float mCalification =0;
private double mExtraPrice = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calification_client);
        mteTextViewOrigin = findViewById(R.id.textvieworiginclientcalification);
        mTextViewDestination = findViewById(R.id.textviewdestinationclientcalification);
        mRatingBar = findViewById(R.id.ratingBarcalificationclient);
        mButtonCalification = findViewById(R.id.btncalificationclient);
        mClientBookingProvider = new ClientBookingProvider();
        mExtraClientId = getIntent().getStringExtra("idClient");
        mHistoryBookingProvider = new HistoryBookingProvider();
        mTextViewprice = findViewById(R.id.textviewprecio);
        mExtraPrice = getIntent().getDoubleExtra("price",0);
        mTextViewprice.setText(String.format("%.1f",mExtraPrice) + " CLP ");
        mRatingBar.setOnRatingBarChangeListener((ratingBar, calification, b) -> mCalification = calification);
        mButtonCalification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calificate();
            }
        });
        getClientBooking();
    }
    //metdod para tomar los datos del cliente que esta solicitando el servicio y mostrarlo al conductor
    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ClientBooking clientBooking = dataSnapshot.getValue(ClientBooking.class);
                    mteTextViewOrigin.setText(clientBooking.getOrigin());
                    mTextViewDestination.setText(clientBooking.getDestination());
                    mHistoryBooking = new HistoryBooking(
                            clientBooking.getIdHistoryBooking(),
                            clientBooking.getIdClient(),
                            clientBooking.getIdDriver(),
                            clientBooking.getDestination(),
                            clientBooking.getOrigin(),
                            clientBooking.getTime(),
                            clientBooking.getKm(),
                            clientBooking.getStatus(),
                            clientBooking.getOriginLat(),
                            clientBooking.getOriginLng(),
                            clientBooking.getDestinationLat(),
                            clientBooking.getDestinationLng()
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
//metodo para calificar el cliente
    private void calificate() {
        if (mCalification  > 0) {
            mHistoryBooking.setCalificationClient(mCalification);
            mHistoryBooking.setTimestamp(new Date().getTime());
            mHistoryBookingProvider.getHistoryBooking(mHistoryBooking.getIdHistoryBooking()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        mHistoryBookingProvider.updatecalificactionClient(mHistoryBooking.getIdHistoryBooking(), mCalification).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(CalificationClient.this, "La calificacion se guardo correctamente", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(CalificationClient.this, MapsDriverActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                    else {
                        mHistoryBookingProvider.create(mHistoryBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(CalificationClient.this, "La calificacion se guardo correctamente", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(CalificationClient.this, MapsDriverActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }
        else {
            Toast.makeText(this, "Debes ingresar la calificacion", Toast.LENGTH_SHORT).show();
        }
    }

}