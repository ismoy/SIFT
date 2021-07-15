package cl.tofcompany.sift.Controllers.Clients;

import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import cl.tofcompany.sift.Model.HistoryBooking;
import cl.tofcompany.sift.Providers.DriverProvider;
import cl.tofcompany.sift.Providers.HistoryBookingProvider;
import cl.tofcompany.sift.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class HistoryBookingDetaillClient extends AppCompatActivity {

    private TextView mTextViewName;
    private TextView mTextViewOrigin;
    private TextView mTextViewDestination;
    private TextView mTextViewCalification;
    private RatingBar mRatingBar;
    private CircleImageView mcircleImageView;
    private CircleImageView mcircleImageViewback;
    private String mExtraId;
    private HistoryBookingProvider mHistoryBookingProvider;
    private DriverProvider mDriverProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_booking_detaill_client);
        mTextViewName = findViewById(R.id.textviewnamebookingdetailclient);
        mTextViewOrigin = findViewById(R.id.textvieworigenhistorybookingdetaillclient);
        mTextViewDestination = findViewById(R.id.textviewdestinationhistorybookingdetaillclient);
        mTextViewCalification = findViewById(R.id.textviewcalificationhistorybookingdetaillclient);
        mRatingBar = findViewById(R.id.ratingBarhistorybookingdetailclient);
        mcircleImageView = findViewById(R.id.circle_imagehistorybookingdetaill);
        mExtraId = getIntent().getStringExtra("idHistoryBooking");
        mHistoryBookingProvider = new HistoryBookingProvider();
        mDriverProvider = new DriverProvider();
        mcircleImageViewback = findViewById(R.id.circle_imagebackhistorybookingdetaill);
        mcircleImageViewback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getHistoryBooking();

    }

    //metodo para tomar las historias del cliente
    private void getHistoryBooking() {
        mHistoryBookingProvider.getHistoryBooking(mExtraId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    HistoryBooking historyBooking = snapshot.getValue(HistoryBooking.class);
                    mTextViewOrigin.setText(historyBooking.getOrigin());
                    mTextViewDestination.setText(historyBooking.getDestination());
                    mTextViewCalification.setText("Tu calificacion: " +historyBooking.getCalificationDriver());
                    if (snapshot.hasChild("calificationClient")) {
                        mRatingBar.setRating((float) historyBooking.getCalificationClient());
                    }
                    mDriverProvider.getDriver(historyBooking.getIdDriver()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                String name = snapshot.child("name").getValue().toString();
                                mTextViewName.setText(name);
                                if (snapshot.hasChild("image")){
                                    String image = snapshot.child("image").getValue().toString();
                                    Picasso.get().load(image).into(mcircleImageView);
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}