package cl.tofcompany.sift.Controllers.Drivers;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import cl.tofcompany.sift.Providers.AuthProvider;
import cl.tofcompany.sift.Providers.ClientBookingProvider;
import cl.tofcompany.sift.Providers.GeofireProvider;
import cl.tofcompany.sift.R;

public class NotificationBooking extends AppCompatActivity {
private TextView mTextViewDestination;
private TextView mTextViewOrigen;
private TextView mTextViewMin;
private TextView mTextViewDistance;
private Button mButtonAceptar;
private Button mButtonRechazar;
private ClientBookingProvider mClientBookingProvider;
private GeofireProvider mGeofireProvider;
private AuthProvider mAuthProvider;
private String mExtraIdClient;
private String mExtraOrigen;
private String mExtraDestination;
private String mExtraMinute;
private String mExtraDistance;
private TextView mTextViewCounter;
private Handler mHandler;
private MediaPlayer mediaPlayer;
private int mcounter = 60;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mcounter = mcounter -1;
            mTextViewCounter.setText(String.valueOf(mcounter));
            if (mcounter > 0) {
                initTimer();
            }
            else {
                cancelBooking();
            }
        }
    };
    private ValueEventListener mListener;

    private void initTimer() {
        mHandler = new Handler();
        mHandler.postDelayed(runnable, 1000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_booking);

        mTextViewDestination = findViewById(R.id.textviewdestinationclientnotification);
        mTextViewOrigen = findViewById(R.id.textvieworiginclientnotification);
        mTextViewMin = findViewById(R.id.textviewMin);
        mTextViewDistance = findViewById(R.id.textviewdistance);
        mTextViewCounter = findViewById(R.id.textviewcounter);
        mButtonAceptar = findViewById(R.id.btnaceptar);
        mButtonRechazar = findViewById(R.id.btnrechazar);

        mExtraIdClient = getIntent().getStringExtra("idClient");
        mExtraOrigen = getIntent().getStringExtra("origin");
        mExtraDestination = getIntent().getStringExtra("destination");
        mExtraMinute = getIntent().getStringExtra("min");
        mExtraDistance = getIntent().getStringExtra("distance");

        mTextViewDestination.setText(mExtraDestination);
        mTextViewOrigen.setText(mExtraOrigen);
        mTextViewMin.setText(mExtraMinute);
        mTextViewDistance.setText(mExtraDistance);

        mediaPlayer = MediaPlayer.create(this, R.raw.alert);
        mediaPlayer.setLooping(true);

        mClientBookingProvider = new ClientBookingProvider();

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

        initTimer();

        checkIfClientCancelBooking();

        mButtonAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptBooking();
            }
        });

        mButtonRechazar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelBooking();
            }
        });
    }
    //metodo que va a chequear si el cliente se cancelo el viaje
    private void checkIfClientCancelBooking() {
        mListener = mClientBookingProvider.getClientBooking(mExtraIdClient).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(NotificationBooking.this, "El cliente cancelo el viaje", Toast.LENGTH_LONG).show();
                    if (mHandler != null) mHandler.removeCallbacks(runnable);
                    Intent intent = new Intent(NotificationBooking.this, MapsDriverActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
//metodo para enviar notification de solicitud cancelada
    private void cancelBooking() {
        if (mHandler != null) mHandler.removeCallbacks(runnable);
        mClientBookingProvider.updateStatus(mExtraIdClient, "cancel");

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);
        Intent intent = new Intent(NotificationBooking.this, MapsDriverActivity.class);
        startActivity(intent);
        finish();
    }
//metodo para enviar notification de solicitud aceptadas
    private void acceptBooking() {
        if (mHandler != null) mHandler.removeCallbacks(runnable);
        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("active_drivers");
        mGeofireProvider.removelocation(mAuthProvider.getId());

        mClientBookingProvider = new ClientBookingProvider();
        mClientBookingProvider.updateStatus(mExtraIdClient, "accept");

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);

        Intent intent1 = new Intent(NotificationBooking.this, MapDriverBookingActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.setAction(Intent.ACTION_RUN);
        intent1.putExtra("idClient", mExtraIdClient);
        startActivity(intent1);
    }

    @Override
    //metodo para saber si esta pausada el sonido de la notificacion
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        }
    }

    @Override
    //metodo para verificar no esta reproduciendo el sonido de la notificacion
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.release();
            }
        }
    }

    @Override
    //metodo para inciar el sonido de la notificacion cuando llegue
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null) {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        }
    }

    @Override
    //matamos todos los listener de firebase para no se queda tarear ejecutando en segundo plano inecesario
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) mHandler.removeCallbacks(runnable);

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        }
        if (mListener != null) {
            mClientBookingProvider.getClientBooking(mExtraIdClient).removeEventListener(mListener);
        }
    }
}