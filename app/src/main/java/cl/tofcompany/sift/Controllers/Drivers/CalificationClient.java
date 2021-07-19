package cl.tofcompany.sift.Controllers.Drivers;

import android.content.Intent;
import android.os.Bundle;
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
    //definimos los variable de origin
private TextView mteTextViewOrigin;
    //definimos los variable de destino
private TextView mTextViewDestination;
    //definimos los variable de precio
    private TextView mTextViewprice;
    //definimos los variable de barra de calificacion
private RatingBar mRatingBar;
    //definimos los variable de button de calificacion
private Button mButtonCalification;
    //definimos la clase de clientbookingprovider
private ClientBookingProvider mClientBookingProvider;
    //variable para recibir el idcliente del extra intent
private String mExtraClientId;
    //definemos la clase de historyBooking
private HistoryBooking mHistoryBooking;
    //definemos la clase de HistoryBookingProvider
private HistoryBookingProvider mHistoryBookingProvider;
    //iniciamos la calificacion en 0
private float mCalification =0;
    //iniciamos el mExtraPrice en 0
private double mExtraPrice = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calification_client);
        init();
    }
    //metodo donde se inicializo los variables y metodos
    private void init(){
        //llamado del origin con su id
        mteTextViewOrigin = findViewById(R.id.textvieworiginclientcalification);
        //llamado del destino con su id
        mTextViewDestination = findViewById(R.id.textviewdestinationclientcalification);
        //llamado del barra de calificacion con su id
        mRatingBar = findViewById(R.id.ratingBarcalificationclient);
        //llamado del button calificacion con su id
        mButtonCalification = findViewById(R.id.btncalificationclient);
        //iniciamos el objeto de la clase clientebookingprovider
        mClientBookingProvider = new ClientBookingProvider();
        //recibiendo el id del cliente por el intent extra
        mExtraClientId = getIntent().getStringExtra("idClient");
        //iniciamos el objeto de la clase HistoryBookingProvider
        mHistoryBookingProvider = new HistoryBookingProvider();
        //llamado el precio con su id
        mTextViewprice = findViewById(R.id.textviewprecio);
        //recibiendo el precio por el intent extra
        mExtraPrice = getIntent().getDoubleExtra("price",0);
        //pasamos al textView el precio extra
        mTextViewprice.setText(String.format("%.1f",mExtraPrice) + " CLP ");
        //agregamos un evento en la barra de calificacion
        mRatingBar.setOnRatingBarChangeListener((ratingBar, calification, b) -> mCalification = calification);
        mButtonCalification.setOnClickListener(v -> calificate());
        getClientBooking();
    }
    //metodo para tomar los datos del cliente que esta solicitando el servicio y mostrarlo al conductor
    private void getClientBooking() {
        //llamando la clase ClientBookingProvider y tomamos el metodo get clientbooking y eso recibe el id del usuario
        //eso lo tenemos en mExtraClientId y tomamos el id y agregamos un evento de firebase addlisternerforsingleresul
        mClientBookingProvider.getClientBooking(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //preguntamos si existe el datos que solicitamos
                if (dataSnapshot.exists()) {
                    //tomando los que vienes en clientbooking en firebase
                    ClientBooking clientBooking = dataSnapshot.getValue(ClientBooking.class);
                    //aseguremos que clientbooking no viene vacio
                    assert clientBooking != null;
                    //enviamos el origen que encontramos en firebase en la vista
                    mteTextViewOrigin.setText(clientBooking.getOrigin());
                    //enviamos el destino que encontramos en firebase en la vista
                    mTextViewDestination.setText(clientBooking.getDestination());
                    //ahora  entramos en la clase de historybooking para tomar los datos que tiene agregado
                    mHistoryBooking = new HistoryBooking(
                            //tomamos el id de historia del viaje
                            clientBooking.getIdHistoryBooking(),
                            //tomamos el id de del cliente que solicito el viaje
                            clientBooking.getIdClient(),
                            //tomamos el id de del conductor que acepto el viaje
                            clientBooking.getIdDriver(),
                            //tomamos el destino
                            clientBooking.getDestination(),
                            //tomamos el origen
                            clientBooking.getOrigin(),
                            //tomamos el tiempo
                            clientBooking.getTime(),
                            //tomamos la distancia
                            clientBooking.getKm(),
                            //tomamos el estado
                            clientBooking.getStatus(),
                            //tomamos la latitud del origen
                            clientBooking.getOriginLat(),
                            //tomamos la longitud del origen
                            clientBooking.getOriginLng(),
                            //tomamos la latitud del delstino
                            clientBooking.getDestinationLat(),
                            //tomamos la longitud del destino
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
        //aseguremos que la califiacion es mayor a 0
        if (mCalification  > 0) {
            //enviamos la calificacion que dio el cliente en la vista
            mHistoryBooking.setCalificationClient(mCalification);
            //enviamos el tiempo y la hora y la fecha en la vista
            mHistoryBooking.setTimestamp(new Date().getTime());
            //ahora entramos en la clase de HistoryBookingProvider tomamos el metodo gethistorybooking
            //eso recibe el id de historybooking lo tenemos en mHistoryBooking y tomamos su id y luego agregamos
            //un metodo de firebase addListenerForSingleValue
            mHistoryBookingProvider.getHistoryBooking(mHistoryBooking.getIdHistoryBooking()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //aseguremos que existe el datos en firebase
                    if (dataSnapshot.exists()) {
                        //invocamos de nuevo la clase de HistoryBookingProvider y ejecutamos el metodo updatecalificactionClient
                        //eso recibe el id HistoryBooking lo tenemos en mHistoryBooking y tomamos el id lo pasamos la calificacion que iniciabamos en 0
                        //y luego llamamos el metodo addOnSuccessListener
                        mHistoryBookingProvider.updatecalificactionClient(mHistoryBooking.getIdHistoryBooking(), mCalification).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //mensaje para decir al conductor que la calificacion se guardo sorrectamente
                                Toast.makeText(CalificationClient.this, "La calificacion se guardo correctamente", Toast.LENGTH_LONG).show();
                                //hacemos un intent para enviarlo de vuelta en la actividad MapsDriverActivity
                                Intent intent = new Intent(CalificationClient.this, MapsDriverActivity.class);
                                //iniciamos el intent
                                startActivity(intent);
                                //ponemos finish para finalizar la actividad
                                finish();
                            }
                        });
                    }
                    else {
                        //en caso contrario llamamos la clase HistoryBookingProvider y creamos el history de viaje
                        //y agregamos un addOnSuccessListener de firebase
                        mHistoryBookingProvider.create(mHistoryBooking).addOnSuccessListener(aVoid -> {
                            //mensaje para decir al driver que no se guardo la calificacion
                            Toast.makeText(CalificationClient.this, "La calificacion se guardo correctamente", Toast.LENGTH_LONG).show();
                            //hacemos un intent para enviarlo de vuelta en la actividad MapsDriverActivity
                            Intent intent = new Intent(CalificationClient.this, MapsDriverActivity.class);
                            //iniciamos el intent
                            startActivity(intent);
                            //ponemos finish para finalizar la actividad
                            finish();
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }
        else {
            //si el conductor no pone una calificacion lo avisamos que debe ingresar una calificacion
            Toast.makeText(this, "Debes ingresar la calificacion", Toast.LENGTH_SHORT).show();
        }
    }

}