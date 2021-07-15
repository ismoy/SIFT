package cl.tofcompany.sift.Controllers.Drivers;

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
import cl.tofcompany.sift.Providers.ClientProvider;
import cl.tofcompany.sift.Providers.HistoryBookingProvider;
import cl.tofcompany.sift.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class HistoryBookingDetaillDriver extends AppCompatActivity {

    private TextView mTextViewName;
    private TextView mTextViewOrigin;
    private TextView mTextViewDestination;
    private TextView mTextViewCalification;
    private RatingBar mRatingBar;
    private CircleImageView mcircleImageView;
    private CircleImageView mcircleImageViewback;
    private String mExtraId;
    private HistoryBookingProvider mHistoryBookingProvider;
    private ClientProvider mClientProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_booking_detaill_driver);
        mTextViewName = findViewById(R.id.textviewnamebookingdetaildriver);
        mTextViewOrigin = findViewById(R.id.textvieworigenhistorybookingdetailldriver);
        mTextViewDestination = findViewById(R.id.textviewdestinationhistorybookingdetailldriver);
        mTextViewCalification = findViewById(R.id.textviewcalificationhistorybookingdetailldriver);
        mRatingBar = findViewById(R.id.ratingBarhistorybookingdetaildriver);
        mcircleImageView = findViewById(R.id.circle_imagehistorybookingdetailldriver);
        mExtraId = getIntent().getStringExtra("idHistoryBooking");
        mHistoryBookingProvider = new HistoryBookingProvider();
        mClientProvider = new ClientProvider();
        mcircleImageViewback = findViewById(R.id.circle_imagebackhistorybookingdetailldriver);
        mcircleImageViewback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getHistoryBooking();

    }

    //metodo para tomar las historias del driver
    private void getHistoryBooking() {
        mHistoryBookingProvider.getHistoryBooking(mExtraId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    HistoryBooking historyBooking = snapshot.getValue(HistoryBooking.class);
                    mTextViewOrigin.setText(historyBooking.getOrigin());
                    mTextViewDestination.setText(historyBooking.getDestination());
                    mTextViewCalification.setText("Tu calificacion: " +historyBooking.getCalificationClient());
                    if (snapshot.hasChild("calificationDriver")) {
                        mRatingBar.setRating((float) historyBooking.getCalificationClient());
                    }
                    mClientProvider.getClient(historyBooking.getIdClient()).addListenerForSingleValueEvent(new ValueEventListener() {
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