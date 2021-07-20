package cl.tofcompany.sift.Controllers.Drivers;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
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
    //variable TextView destino
private TextView mTextViewDestination;
    //variable TextView origen
private TextView mTextViewOrigen;
    //variable TextView tiempo
private TextView mTextViewMin;
    //variable TextView distancia
private TextView mTextViewDistance;
    //variable  btn aceptar
private Button mButtonAceptar;
    //variable btn rechazar
private Button mButtonRechazar;
   //variable de la clase ClientBookingProvider
private ClientBookingProvider mClientBookingProvider;
    //variable de la clase GeofireProvider
private GeofireProvider mGeofireProvider;
    //variable de la clase AuthProvider
private AuthProvider mAuthProvider;
   //variable para guardar el id cliente que vamos a recibir por un intent extra
private String mExtraIdClient;
    //variable para guardar el origin extra que vamos a recibir por un intent extra
private String mExtraOrigen;
    //variable para guardar el destino extra que vamos a recibir por un intent extra
private String mExtraDestination;
    //variable para guardar el tiempo extra que vamos a recibir por un intent extra
private String mExtraMinute;
    //variable para guardar el distancia extra que vamos a recibir por un intent extra
private String mExtraDistance;
   //variable TextView para el contador
private TextView mTextViewCounter;
   //variable del objeto Handler
private Handler mHandler;
  //variable del Objeto MediaPlayer
private MediaPlayer mediaPlayer;
 //iniciamos el contador en 60 segundos
private int mcounter = 60;
    //clase de interface de Runnable
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //iniciar al mcounter  lo que viene en mcounter y lo restamos 1
            mcounter = mcounter -1;
            //enviamos en la vista el valor de mcounter
            mTextViewCounter.setText(String.valueOf(mcounter));
            //aseguramos que mcounter es mayor a 0
            if (mcounter > 0) {
                //si es asi iniciamos el contador
                initTimer();
            }
            else {
                //si llega a 0 el contador cancelamos el viaje
                cancelBooking();
            }
        }
    };
    //ese es un escuchador de firebase lo necesitamos para parar el escuchador despues un evento
    private ValueEventListener mListener;
    //iniciar el contador
    private void initTimer() {
        //recibe el Objeto mHandler
        mHandler = new Handler();
        //ejecutamos el metodo PostDelayed eso recibe el runnable y el tiempo en mi caso es cada segundo
        mHandler.postDelayed(runnable, 1000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_booking);
        init();
    }
    private void init(){
        //iniciamos TextView del destino con su id
        mTextViewDestination = findViewById(R.id.textviewdestinationclientnotification);
        //iniciamos TextView del origin con su id
        mTextViewOrigen = findViewById(R.id.textvieworiginclientnotification);
        //iniciamos TextView del tiempo con su id
        mTextViewMin = findViewById(R.id.textviewMin);
        //iniciamos TextView del distancia con su id
        mTextViewDistance = findViewById(R.id.textviewdistance);
        //iniciamos TextView del contador con su id
        mTextViewCounter = findViewById(R.id.textviewcounter);
        //iniciamos btn aceptar con su id
        mButtonAceptar = findViewById(R.id.btnaceptar);
        //iniciamos btn rechazar con su id
        mButtonRechazar = findViewById(R.id.btnrechazar);
        //recibiendo el id cliente por el intent extra
        mExtraIdClient = getIntent().getStringExtra("idClient");
        //recibiendo el origen por el intent extra
        mExtraOrigen = getIntent().getStringExtra("origin");
        //recibiendo el destino por el intent extra
        mExtraDestination = getIntent().getStringExtra("destination");
        //recibiendo el tiempo por el intent extra
        mExtraMinute = getIntent().getStringExtra("min");
        //recibiendo la distancia por el intent extra
        mExtraDistance = getIntent().getStringExtra("distance");
        //enviar en la vista el destino
        mTextViewDestination.setText(mExtraDestination);
        //enviar en la vista el origen
        mTextViewOrigen.setText(mExtraOrigen);
        //enviar en la vista el tiempo
        mTextViewMin.setText(mExtraMinute);
        //enviar en la vista la distancia
        mTextViewDistance.setText(mExtraDistance);
        //creamos el sonido de notificacion
        mediaPlayer = MediaPlayer.create(this, R.raw.alert);
        mediaPlayer.setLooping(true);
        mClientBookingProvider = new ClientBookingProvider();
        //accion para activar la pantalla si esta apagada
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );
        //metodo del contador
        initTimer();
        //metodo para saber si el cliente cancelo el viaje
        checkIfClientCancelBooking();
        //agregamos un envento onclick en el boton aceptar
        mButtonAceptar.setOnClickListener(view ->
                //metodo aceptar viaje
                acceptBooking());
        //agregamos un evento onclick en el boton rechazar
        mButtonRechazar.setOnClickListener(view ->
                //metodo por si el conductor rechazo la solicitud
                cancelBooking());
    }
    //metodo que va a chequear si el cliente se cancelo el viaje
    private void checkIfClientCancelBooking() {
        //primero llamamos nuestro escuchador de firebase lo iniciamos a la clase ClientBookingProvider
        //ejecutamos el metodo getClientBooking eso recibe un id lo tenemos en mExtraIdClient
        mListener = mClientBookingProvider.getClientBooking(mExtraIdClient).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //aseguramos que los datos existen
                if (!dataSnapshot.exists()) {
                    //si encontramos el id del cliente que se cancelo en viamos un mesaje alert
                    Toast.makeText(NotificationBooking.this, "El cliente cancelo el viaje", Toast.LENGTH_LONG).show();
                    //asegurar que el mHandler no es vacio
                    if (mHandler != null) mHandler.removeCallbacks(runnable);
                    //crear un intent si encuento datos en mi mHandler
                    Intent intent = new Intent(NotificationBooking.this, MapsDriverActivity.class);
                    //iniciar intent
                    startActivity(intent);
                    //terminar todas las actividades
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
        //asegurar que el mHandler no esta vacio
        if (mHandler != null) mHandler.removeCallbacks(runnable);
        //entramos en la clase de ClientBookingProvider ejecutamos el metodo updateStatus
        //recibe el id del cliente eso lo tenemos en mExtraIdClient tb un estado en mi caso cancelado
        mClientBookingProvider.updateStatus(mExtraIdClient, "cancel");
        //invocar el objeto de notificacion manager
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //pasamos el id al cancel
        manager.cancel(2);
        //crear un intent
        Intent intent = new Intent(NotificationBooking.this, MapsDriverActivity.class);
        //iniciar el intent
        startActivity(intent);
        //terminar todas las actividades
        finish();
    }
//metodo para enviar notification de solicitud aceptadas
    private void acceptBooking() {
        //asegurarv que el mHandler no esta vacio
        if (mHandler != null) mHandler.removeCallbacks(runnable);
        //iniciamos la clase de AuthProvider
        mAuthProvider = new AuthProvider();
        //iniciamos la clase de GeofireProvider y apuntamos al nodo active_drivers en firebase
        mGeofireProvider = new GeofireProvider("active_drivers");
        //entramos en la clase GeofireProvider ejecutamos el metodo removelocation recibe
        //el id del conducto que acepto eso lo tenemos en mAuthProvider
        mGeofireProvider.removelocation(mAuthProvider.getId());
        //inicamos la clase ClientBookingProvider
        mClientBookingProvider = new ClientBookingProvider();
        //entramos en la clase ClientBookingProvider ejecutamos el metodo updateStatus
        //recibe el id cliente eso lo tenemos en mExtraIdClient y tb recibe un estado
        //en mi caso en aceptado
        mClientBookingProvider.updateStatus(mExtraIdClient, "accept");
        //invocamos el objeto de NotificationManager
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //pasamos el id al metodo .cancel de NotificationManager
        manager.cancel(2);
        //crear el intent
        Intent intent1 = new Intent(NotificationBooking.this, MapDriverBookingActivity.class);
        //limpiamos todas las tareas de ese intent es decir no se puede volver atras
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.setAction(Intent.ACTION_RUN);
        //tb el intent tiene que ir con el id cliente lo tenemos en mExtraIdClient
        intent1.putExtra("idClient", mExtraIdClient);
        //iniciar el intent
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
        //aviso! revisa donde esto llamando el mListener porque ese sera el fin de todos
        //porque aqui cerramos todos los escuchadores que estan abiertos en los eventos
        if (mListener != null) {
            mClientBookingProvider.getClientBooking(mExtraIdClient).removeEventListener(mListener);
        }
    }
}