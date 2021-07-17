package cl.tofcompany.sift.Controllers.Clients;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

import cl.tofcompany.sift.Model.ClientBooking;
import cl.tofcompany.sift.Model.HistoryBooking;
import cl.tofcompany.sift.Providers.AuthProvider;
import cl.tofcompany.sift.Providers.ClientBookingProvider;
import cl.tofcompany.sift.Providers.HistoryBookingProvider;
import cl.tofcompany.sift.R;

public class CalificationDriver extends AppCompatActivity {
   //definimos los variable de origin
    private TextView mTextViewOrigin;
    //definimos los variable de destino
    private TextView mTextViewDestination;
    //definimos los variable de barra de calificacion
    private RatingBar mRatinBar;
    //definimos los variable de button de calificacion
    private Button mButtonCalification;
    //definimos los variable de precio
    private TextView mTextViewprice ;
    //definimos la clase de clientbookingprovider
    private ClientBookingProvider mClientBookingProvider;
    //defimos la clase de Authprovider
    private AuthProvider mAuthProvider;
    //defimos la clase de historyBooking
    private HistoryBooking mHistoryBooking;
    //defimos la clase de HistoryBookingProvider
    private HistoryBookingProvider mHistoryBookingProvider;
   //iniciamos la calificacion en 0
    private float mCalification = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calification_driver);
        //llamado del destino con su id
        mTextViewDestination = findViewById(R.id.textviewdestinationdrivercalification);
        //llamado del origen con su id
        mTextViewOrigin = findViewById(R.id.textvieworigindrivercalification);
        //llamado del barra de calificacion con su id
        mRatinBar = findViewById(R.id.ratingBarcalificationdriver);
        //llamado del button calificacion con su id
        mButtonCalification = findViewById(R.id.btncalificationdriver);
        //llamado del precio con su id
        mTextViewprice = findViewById(R.id.textviewpreciodriver);
        //iniciamos el objeto de la clase clientebookingprovider
        mClientBookingProvider = new ClientBookingProvider();
        //iniciamos el objeto de la clase hustorybookingprovider
        mHistoryBookingProvider = new HistoryBookingProvider();
        //iniciamos el objeto de la clase mauthprovider
        mAuthProvider = new AuthProvider();
        //llamamos el metodo setonratinbarchangeListener para la barra de calificacion
        mRatinBar.setOnRatingBarChangeListener((ratingBar, calification, b) -> mCalification = calification);
       //llamado el metodo set onclick listener para el button
        mButtonCalification.setOnClickListener(view -> calificate());
        //metodo para tomar los datos de los clientes que estan buscando
        getClientBooking();
    }
    //metodo para tomar los datos de los clientes que estan buscando
    private void getClientBooking() {
        //llamando la clase clientbookingprovider y tomamos el metodo get clientbooking y eso recibe el id del usuario
        //eso lo tenemos en mauthprovider y tomamos el id y agregamos un evento de firebase addlisternerforsingleresul
        mClientBookingProvider.getClientBooking(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //preguntamos si existe el datos que solicitamos
                if (dataSnapshot.exists()) {
                    //tomando los que vienes en clientbooking en firebase
                    ClientBooking clientBooking = dataSnapshot.getValue(ClientBooking.class);
                    //aseguremos que clientbooking no viene vacio
                    assert clientBooking != null;
                    //enviamos el origen que encontramos en firebase en la vista
                    mTextViewOrigin.setText(clientBooking.getOrigin());
                    //enviamos el precio que encontramos en firebase en la vista
                    mTextViewprice.setText(String.format("%.1f",clientBooking.getPrice()) + " CLP ");
                    //enviamos el destino que encontramos en firebase en la vista
                    mTextViewDestination.setText(clientBooking.getDestination());
                    //ahora en entramos en la clase de historybooking para tomar los datos que tiene agregado
                    mHistoryBooking = new HistoryBooking(
                            //tomamos el id de historia del viaje
                            clientBooking.getIdHistoryBooking(),
                            //tomamos el id de del cliente que solicito el viaje
                            clientBooking.getIdClient(),
                            //tomamos el id de del conductor que acepto el viaje
                            clientBooking.getIdDriver(),
                            //tomamos el destino
                            clientBooking.getDestination(),
                            //tomamos en origen
                            clientBooking.getOrigin(),
                            //tomamos el tiempo que demoro
                            clientBooking.getTime(),
                            //tomamoos la distancia
                            clientBooking.getKm(),
                            //tomamos el estado
                            clientBooking.getStatus(),
                            //tomamos la latitud del origen
                            clientBooking.getOriginLat(),
                            //tomamos la longitude del origen
                            clientBooking.getOriginLng(),
                            //tomamos la latitude del destino
                            clientBooking.getDestinationLat(),
                            //tomamos la logitude del destino
                            clientBooking.getDestinationLng()
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //metodo para calificar
    private void calificate() {
        //aseguremos que la califiacion es mayor a 0
        if (mCalification > 0) {
            //enviamos la calificacion que dio el conductos en la vista
            mHistoryBooking.setCalificationDriver(mCalification);
            //enviamos el tiempo y la hora y la fecha en la vista
            mHistoryBooking.setTimestamp(new Date().getTime());
            //ahora entramos en la clase de historybookingprovider tomamos el metodo gethistorybooking
            //eso recibe el id de historybooking lo tenemos en mHistoryBooking y tomamos su id y luego agrregamos
            //un metodo de firebase addListenerForSingleValue
            mHistoryBookingProvider.getHistoryBooking(mHistoryBooking.getIdHistoryBooking()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //aseguremos que existe el datos en firebase
                    if (dataSnapshot.exists()) {
                        //invocamos de nuevo la classe de HistoryBookingProvider y ejecutamos el metodo updatecalificactionDriver
                        //eso recibe el id HistoryBooking lo tenemos en mHistoryBooking y tomamos el id lo pasamos la calificacion que iniciabamos en 0
                        //y luego llamamos el metodo addOnSuccessListener
                        mHistoryBookingProvider.updatecalificactionDriver(mHistoryBooking.getIdHistoryBooking(), mCalification).addOnSuccessListener(aVoid -> {
                            //mensaje para decir al cliente que la calificacion se guardo sorrectamente
                            Toast.makeText(CalificationDriver.this, "La calificacion se guardo correctamente", Toast.LENGTH_LONG).show();
                            //hacemos un intent para enviarlo de vuelta en la actividad MapsClientActivity
                            Intent intent = new Intent(CalificationDriver.this, MapsClientActivity.class);
                            //iniciamos el intent
                            startActivity(intent);
                            //ponemos finsh para finalizar la actividad
                            finish();
                        });
                    } else {
                        //en caso contrario llamamos la clase HistoryBookingProvider y creamos el history de viaje
                        //y agregamos un addOnSuccessListener de firebase
                        mHistoryBookingProvider.create(mHistoryBooking).addOnSuccessListener(aVoid -> {
                            //mensaje para decir al cliente que no se guardo la calificacion
                            Toast.makeText(CalificationDriver.this, "La calificacion se guardo correctamente", Toast.LENGTH_LONG).show();
                            //hacemos un intent para enviarlo de vuelta en la actividad MapsClientActivity
                            Intent intent = new Intent(CalificationDriver.this, MapsClientActivity.class);
                            //iniciamos el intent
                            startActivity(intent);
                            //ponemos finsh para finalizar la actividad
                            finish();
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        } else {
            //si el usuario no pone una calificacion lo avisamos que debe ingresar una calificacion
            Toast.makeText(this, "Debes ingresar la calificacion", Toast.LENGTH_SHORT).show();
        }
    }
}