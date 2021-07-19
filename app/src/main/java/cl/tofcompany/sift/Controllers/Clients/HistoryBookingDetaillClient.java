package cl.tofcompany.sift.Controllers.Clients;

import android.os.Bundle;
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
    //variable textview name
    private TextView mTextViewName;
    //variable textview origen
    private TextView mTextViewOrigin;
    //variable textview destino
    private TextView mTextViewDestination;
    //variable textview califiacicion
    private TextView mTextViewCalification;
    //variable ratinbar barra de calificacion
    private RatingBar mRatingBar;
    //variable cicleimage del imagen
    private CircleImageView mcircleImageView;
    //variable cicle image
    private CircleImageView mcircleImageViewback;
    //variable para recibir el intent de idextra
    private String mExtraId;
    // variable de la clase HistoryBookingProvider
    private HistoryBookingProvider mHistoryBookingProvider;
    // variable de la clase DriverProvider
    private DriverProvider mDriverProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_booking_detaill_client);
       init();

    }

    //metodo donde esta las initialisation de los variables y clases
    private void init(){
        //iniciamos el variable name con su id
        mTextViewName = findViewById(R.id.textviewnamebookingdetailclient);
        //iniciamos el variable origen con su id
        mTextViewOrigin = findViewById(R.id.textvieworigenhistorybookingdetaillclient);
        //iniciamos el variable destino con su id
        mTextViewDestination = findViewById(R.id.textviewdestinationhistorybookingdetaillclient);
        //iniciamos el variable calificacion con su id
        mTextViewCalification = findViewById(R.id.textviewcalificationhistorybookingdetaillclient);
        //iniciamos el barra de calificacion name con su id
        mRatingBar = findViewById(R.id.ratingBarhistorybookingdetailclient);
        //iniciamos el variable image view con su id
        mcircleImageView = findViewById(R.id.circle_imagehistorybookingdetaill);
        //recibimos el intent de idextra
        mExtraId = getIntent().getStringExtra("idHistoryBooking");
        //iniciamos el objeto de la clase HistoryBookingProvider
        mHistoryBookingProvider = new HistoryBookingProvider();
        //iniciamos el objeto de la clase DriverProvider
        mDriverProvider = new DriverProvider();
        //iniciamos el variable image view con su id
        mcircleImageViewback = findViewById(R.id.circle_imagebackhistorybookingdetaill);
        //aplicamos un evento click sobre el imagenviewback
        mcircleImageViewback.setOnClickListener(v ->
                finish());
        //metodo para tomar las historias
        getHistoryBooking();
    }
    //metodo para tomar las historias del cliente
    private void getHistoryBooking() {
        //entramos en la clase de HistoryBookingProvider y tomamos en metodo de getHistoryBooking
        //eso recibe un id y el id lo tenemos mExtraId y agregamos un evento de firebase
        mHistoryBookingProvider.getHistoryBooking(mExtraId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                //aseguramos que el datos existe
                if (snapshot.exists()){
                    //asignamos la clase de HistoryBooking a los datos que vienen en firebase mediante el snapshot
                    //y tomamos su valor y lo enviamos al HistoryBooking.class
                    HistoryBooking historyBooking = snapshot.getValue(HistoryBooking.class);
                    //enviamos en la vista del campo origen el origen que encuentro
                    mTextViewOrigin.setText(historyBooking.getOrigin());
                    //enviamos ne la vista del campo destino el destino que encuentro
                    mTextViewDestination.setText(historyBooking.getDestination());
                    //enviamos ne la vista del campo calification la calification que encuentro
                    mTextViewCalification.setText("Tu calificacion: " +historyBooking.getCalificationDriver());
                    //preguntamos si en el nodo de calificationClient si ya hay una calificacion
                    if (snapshot.hasChild("calificationClient")) {
                        //simplemente enviamos la calificacion que hay en la barra de calificacion
                        mRatingBar.setRating((float) historyBooking.getCalificationClient());
                    }
                    //entramos en la classe de DriverProvider y tomamos el metodo getDriver eso recibe un
                    // id de conductor lo tenemos en historyBooking y sacamos el id del conductor y agregamos un evento firebase
                    mDriverProvider.getDriver(historyBooking.getIdDriver()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            //aseguramos que el datos existe
                            if (snapshot.exists()){
                                //creamos una variable name para sacar el nombre en firebase
                                String name = snapshot.child("name").getValue().toString();
                                //enviamos el nombre que encuentro en la vista
                                mTextViewName.setText(name);
                                //aseguramos que hay imagen en el nodo de image
                                if (snapshot.hasChild("image")){
                                    //creamos una variable image para sacar el imagen en firebase
                                    String image = snapshot.child("image").getValue().toString();
                                    //primero lo procesamos con picasso y luego lo enviamos en la vista
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