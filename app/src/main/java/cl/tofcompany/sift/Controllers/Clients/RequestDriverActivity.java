package cl.tofcompany.sift.Controllers.Clients;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cl.tofcompany.sift.Model.ClientBooking;
import cl.tofcompany.sift.Model.FCMBody;
import cl.tofcompany.sift.Model.FCMResponse;
import cl.tofcompany.sift.Providers.AuthProvider;
import cl.tofcompany.sift.Providers.ClientBookingProvider;
import cl.tofcompany.sift.Providers.GeofireProvider;
import cl.tofcompany.sift.Providers.GoogleApiProvider;
import cl.tofcompany.sift.Providers.NotificationProvider;
import cl.tofcompany.sift.Providers.TokenProvider;
import cl.tofcompany.sift.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestDriverActivity extends AppCompatActivity {
    //variable para la animacion de lottie
    private LottieAnimationView mAnimation;
    //variable textview buscar
    private TextView mTextViewLookingFor;
    //variable btn cancelar
    private Button mButtonCancelRequest;
    //variable clase de GeofireProvider
    private GeofireProvider mGeofireProvider;
    //variable extra origen que vamos a recibir
    private String mExtraOrigin;
    //variable extra destino que vamos a recibir
    private String mExtraDestination;
    //variable extra origenlat que vamos a recibir
    private double mExtraOriginLat;
    //variable extra origenlong que vamos a recibir
    private double mExtraOriginLng;
    //variable extra destinolat que vamos a recibir
    private double mExtraDestinationLat;
    //variable extra destinolong que vamos a recibir
    private double mExtraDestinationLng;
    //variable  origenlatlng
    private LatLng mOriginLatLng;
    //variable  destinolatlng
    private LatLng mDestinationLatLng;
    //variable  radio lo iniciamos en 0.1s
    private double mRadius = 0.1;
    //variable  driverfound se inicializo en false
    private boolean mDriverFound = false;
    //variable  id del driver
    private String  mIdDriverFound = "";
    //variable  para saber la latitud y longitud de la posicion del conductor
    private LatLng mDriverFoundLatLng;
    // variable clase NotificationProvider
    private NotificationProvider mNotificationProvider;
    //variable clase TokenProvider
    private TokenProvider mTokenProvider;
    //variable clase ClientBookingProvider
    private ClientBookingProvider mClientBookingProvider;
    //variable clase AuthProvider
    private AuthProvider mAuthProvider;
    //variable clase GoogleApiProvider
    private GoogleApiProvider mGoogleApiProvider;
    //variable del valor de mi escuchador de firebase
    private ValueEventListener mListener;
    //variable btn cancelar
    private Button mButtonCancelar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_driver);
       init();
    }
    //aqui estan las inicializaciones de los variables
    private void init(){
        //iniciamos la animacion de lottie con su id
        mAnimation = findViewById(R.id.animation);
        //iniciamos el textView buscando con su id
        mTextViewLookingFor = findViewById(R.id.textviewbuscando);
        //iniciamos el boton con su id
        mButtonCancelRequest = findViewById(R.id.cancelar);
        //aqui la animacion empieza a funcionar
        mAnimation.playAnimation();
        //recibimos el extraOrigen por el intentExtra
        mExtraOrigin = getIntent().getStringExtra("origin");
        //recibimos el extradestino por el intentExtra
        mExtraDestination = getIntent().getStringExtra("destination");
        //recibimos el extraOrigenLatitud por el intentExtra
        mExtraOriginLat = getIntent().getDoubleExtra("origin_lat", 0);
        //recibimos el extraOrigenLongitud por el intentExtra
        mExtraOriginLng = getIntent().getDoubleExtra("origin_lng", 0);
        //recibimos el extraDestinoLatitud por el intentExtra
        mExtraDestinationLat = getIntent().getDoubleExtra("destination_lat", 0);
        //recibimos el extraDestinoLongitud por el intentExtra
        mExtraDestinationLng = getIntent().getDoubleExtra("destination_lng", 0);
        //pasamos al origenLatLng el extraorigenlat y el extraorigenlng que recibimos
        mOriginLatLng = new LatLng(mExtraOriginLat, mExtraOriginLng);
        //pasamos al destinoLatLng el extradestinolat y el extradestinolng que recibimos
        mDestinationLatLng= new LatLng(mExtraDestinationLat, mExtraDestinationLng);
        //iniciamos la clase de GeoFireProvider y creamos un nodo active_drivers en firebase
        mGeofireProvider = new GeofireProvider("active_drivers");
        //iniciamos la clase Token
        mTokenProvider = new TokenProvider();
        //iniciamos la clase NotificationProvider para las notificaciones
        mNotificationProvider = new NotificationProvider();
        //iniciamos la clase ClientBookingProvider
        mClientBookingProvider = new ClientBookingProvider();
        //iniciamos la clase AuthProvider
        mAuthProvider = new AuthProvider();
        //iniciamos la clase GoogleApiProvider eso recibe un contexto en mi caso sera aqui o this
        mGoogleApiProvider = new GoogleApiProvider(RequestDriverActivity.this);
        //iniciamos el btn con su id
        mButtonCancelar = findViewById(R.id.cancelar);
        //agregamos un evento onclick en el boton
        mButtonCancelar.setOnClickListener(v ->
                //metodo para cancelar solicitud
                cancelRequest());
        //metodo para buscar conductor
        getClosestDriver();
    }
    //metodo para permitir al cliente cancelar el viaje
    private void cancelRequest() {
        //entramos en la clase ClientBookingProvider ejecutamos el metodo delete eso recibe el id
        //lo tenemos en mAuthProvider y luego agregamos un evento addOnSuccessListener
        mClientBookingProvider.delete(mAuthProvider.getId()).addOnSuccessListener(aVoid ->
                sendNotificationCancel());

    }
//metodo para buscar conductores
    private void getClosestDriver() {
        //entramos en la clase GeofireProvider ejecutamos el metodo getActiveDrivers esos recibe
        //la latitud y la longitud del origen lo tenemos en mOriginLatLng  y tb el radio
        // lo tenemos en mRadius y luego agregamos un evento addGeoQueryEventListener
        mGeofireProvider.getActiveDrivers(mOriginLatLng, mRadius).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!mDriverFound) {
                    //activamos la busqueda
                    mDriverFound = true;
                    //pasamos al id la llave que proviene de firebase
                    mIdDriverFound = key;
                    //tb necesitamos la latitud y la longitud pasamos a location la lat y la lng
                    //lo guardamos en mDriverFoundLatLng
                    mDriverFoundLatLng = new LatLng(location.latitude, location.longitude);
                    //si encuentro conductor muestramos ese mensaje
                    mTextViewLookingFor.setText("CONDUCTOR ENCONTRADO\nESPERANDO RESPUESTA");
                    //creamos la busqueda
                    createClientBooking();
                    Log.d("DRIVER", "ID: " + mIdDriverFound);
                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                // INGRESA CUANDO TERMINA LA BUSQUEDA DEL CONDUCTOR EN UN RADIO DE 0.1 KM
                if (!mDriverFound) {
                    //cuando el cliente esta buscando el contador empezara en 0.1s y va aumentando en 0.1s
                    mRadius = mRadius + 0.1f;
                    //si el contador del radio es mayor a 5s mostrara un mensaje al cliente
                    //ojo ese evento nunca terminara hasta que encuentra un conductor
                    //es decir si no sales en la app y se conecto un conductor cerca tu ubicacion
                    //automaticamente el evento va a enviar la solicitud a ese conducto y si se acepto
                    //alli si va terminar el processo de su evento
                    if (mRadius > 5) {
                        // NO ENCONTRO NINGUN CONDUCTOR
                        mTextViewLookingFor.setText("NO SE ENCONTRO UN CONDUCTOR");
                        Toast.makeText(RequestDriverActivity.this, "NO SE ENCONTRO UN CONDUCTOR", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else {
                        //como decia si no encuentra conductor enviamos el mismo metodo para seguir
                        //buscando conductor hasta que se encuentra
                        getClosestDriver();
                    }
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
//metdo para crear en firebase en tiempo real el usuario que esta solicitando el servicio
    private void createClientBooking() {
        //entramos en la clase GoogleApiProvider ejecutamos el metodo getDirections recibe
        //la latitu y la longitud del origen eso lo tenemos en mOriginLatLng
        //tb recibe la latitud y la logitud del conductor que estan buscando eso lo tenemos en mDriverFoundLatLng
        mGoogleApiProvider.getDirections(mOriginLatLng, mDriverFoundLatLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    //Aviso esos datos como routes,polyline y point provienen de la api
                    //recibimos el response del body del objeto json
                    JSONObject jsonObject = new JSONObject(response.body());
                    //recibimos el response del array del objeto json
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    //pasamos al route el jsonArray
                    JSONObject route = jsonArray.getJSONObject(0);
                    //recibimos el response del polyline del objeto json
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    //pasamos al points el polyline
                    String points = polylines.getString("points");
                    //aviso los que dicen distance. duration text son de la Api
                    //recibimos el response de legs del objeto json
                    JSONArray legs =  route.getJSONArray("legs");
                    //pasamos al leg nuestra variable legs
                    JSONObject leg = legs.getJSONObject(0);
                    //recibimos la distancia
                    JSONObject distance = leg.getJSONObject("distance");
                    //recibimos la duracion
                    JSONObject duration = leg.getJSONObject("duration");
                    //pasamos la distancia en texto
                    String distanceText = distance.getString("text");
                    //pasamos la duracion en texto
                    String durationText = duration.getString("text");
                    //aqui enviamos la notificacion al conductor eso recibre la duracion y la distancia
                    //lo tenemos en durationText y distanceText
                    sendNotification(durationText, distanceText);

                } catch(Exception e) {
                    Log.d("Error", "Error encontrado " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });


    }
//metodo para notificar al conductor que el cliente se cancelo el viaje
    private void sendNotificationCancel() {
        //entramos en la clase TokenProvider ejecutamos el metodo getToken eso recibe el id
        //lo tenemos en mIdDriverFound y luego agregamos un envento de firebase
        mTokenProvider.getToken(mIdDriverFound).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //aseguremos que el dato existe
                if (dataSnapshot.exists()) {
                    //creamos un variable de token que va recibir el token proviene de firebase
                    //mediante dataSnapshot.child("token")
                    String token = dataSnapshot.child("token").getValue().toString();
                    //creamos un hashmap
                    Map<String, String> map = new HashMap<>();
                    //titulo
                    map.put("title", "VIAJE CANCELADO");
                    //cuerpo
                    map.put("body",
                            "El cliente cancelo la solicitud"
                    );
                    //iniciamos la clase del modelo FCMBody recibe el token, la prioridad en mi caso es alta
                    // tiempo en mi caso es 4500ms, tb recibe el map
                    FCMBody fcmBody = new FCMBody(token, "high", "4500s", map);
                    //entramos en la clase de NotificationProvider ejecutemos el metodo sendNotification eso recibe
                    //el fcmBody
                    mNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            //aseguramos que la respuesta del body no esta vacio
                            if (response.body() != null) {
                                //si el success es igual a 1
                                if (response.body().getSuccess() == 1) {
                                    //muestramos ese mensaje
                                    Toast.makeText(RequestDriverActivity.this, "La solicitud se cancelo correctamente", Toast.LENGTH_SHORT).show();
                                    //y creamos un intent
                                    Intent intent = new Intent(RequestDriverActivity.this, MapsClientActivity.class);
                                    //iniciamos el intent
                                    startActivity(intent);
                                    //terminamos todas las tareas
                                    finish();
                                    //Toast.makeText(RequestDriverActivity.this, "La notificacion se ha enviado correctamente", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    //mensaje de error
                                    Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                //mensaje error
                                Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("Error", "Error " + t.getMessage());
                        }
                    });
                }
                else {
                    //mensaje error
                    Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificacion porque el conductor no tiene un token de sesion", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
//metodo para enviar notificacion al conductor cuando el cliente solicito un servicio
    private void sendNotification(final String time, final String km) {
        //entramos en la clase de TokenProvider ejecutamos el metodo getToken esos recibe
        //un id lo tenemos en mIdDriverFound y lueguo pasamos un envento de firebase addListenerForSingleValueEvent
        mTokenProvider.getToken(mIdDriverFound).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //aseguramos que el dato existe
                if (dataSnapshot.exists()) {
                    //creamos un variable de token que va recibir el token proviene de firebase
                    //mediante dataSnapshot.child("token")
                    String token = dataSnapshot.child("token").getValue().toString();
                    //creamos un hashmap
                    Map<String, String> map = new HashMap<>();
                    //titulo
                    map.put("title", "SOLICITUD DE SERVICIO A " + time + " DE TU POSICION");
                    //cuerpo
                    map.put("body",
                            "Un cliente esta solicitando un servicio a una distancia de " + km + "\n" +
                                    "Recoger en: " + mExtraOrigin + "\n" +
                                    "Destino: " + mExtraDestination
                    );
                    //eviamos a traves de un put el idcliente
                    map.put("idClient", mAuthProvider.getId());
                    //eviamos a traves de un put el origenextra
                    map.put("origin", mExtraOrigin);
                    //eviamos a traves de un put el destinoextra
                    map.put("destination", mExtraDestination);
                    //eviamos a traves de un put el tiempo
                    map.put("min", time);
                    //eviamos a traves de un put la distancia
                    map.put("distance", km);
                    //iniciamos la clase del modelo FCMBody recibe el token, la prioridad en mi caso es alta
                    // tiempo en mi caso es 4500ms, tb recibe el map
                    FCMBody fcmBody = new FCMBody(token, "high", "4500s", map);
                    //entramos en la clase de NotificationProvider ejecutemos el metodo sendNotification eso recibe
                    //el fcmBody
                    mNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            //aseguramos que la respuesta del body no esta vacio
                            if (response.body() != null) {
                                //si el success es igual a 1
                                if (response.body().getSuccess() == 1) {
                                    //llamamos la clase ClientBooking y lo pasamos los datos correspondientes
                                    ClientBooking clientBooking = new ClientBooking(
                                            //el id del usuario
                                            mAuthProvider.getId(),
                                            //id conductos que busco
                                            mIdDriverFound,
                                            //el destino extra
                                            mExtraDestination,
                                            //el origen extra
                                            mExtraOrigin,
                                            //el tiempo
                                            time,
                                            //la distancia
                                            km,
                                            //el estado
                                            "create",
                                            //el origin extralat
                                            mExtraOriginLat,
                                            //el origin extralng
                                            mExtraOriginLng,
                                            //el destino extralat
                                            mExtraDestinationLat,
                                            //el destino extralng
                                            mExtraDestinationLng
                                    );
                                    //entramos en la clase ClientBookingProvider ejecutamos el metodo create
                                    //eso recibe los datos del cliente que esta buscando lo tenemos en
                                    //clientBooking
                                    mClientBookingProvider.create(clientBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //check el estado del clente que esta buncando
                                            checkStatusClientBooking();
                                        }
                                    });
                                    //Toast.makeText(RequestDriverActivity.this, "La notificacion se ha enviado correctamente", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    //mensaje error
                                    Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                //mensaje error
                                Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("Error", "Error " + t.getMessage());
                        }
                    });
                }
                else {
                    //memsaje error
                    Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificacion porque el conductor no tiene un token de sesion", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
//metodo chequear el estado del solicitud en tiempo real
    private void checkStatusClientBooking() {
        //llamamos nuestro escuchador global y lo pasamos la clase ClientBookingProvider entramos en esa clase
        //ejecutamos el metodo getStatus eso recibe un id lo tenemos en mAuthProvider
        mListener = mClientBookingProvider.getStatus(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //aseguramos que el dato existe
                if (dataSnapshot.exists()) {
                    //creamos una variable de status para guardar lo que va a traer firebase
                    //mediante dataSnapshot.getValue()
                    String status = dataSnapshot.getValue().toString();
                    //preguntamos si el estado es aceotado
                    if (status.equals("accept")) {
                        //creamos ese intent
                        Intent intent = new Intent(RequestDriverActivity.this, MapClientBookingActivity.class);
                        //iniciamos el intent
                        startActivity(intent);
                        //terminamos todas las tareas
                        finish();
                    }
                    //si el estado es igual a cancelado
                    else if (status.equals("cancel")) {
                        //enviamos ese mensaje
                        Toast.makeText(RequestDriverActivity.this, "El conductor no acepto el viaje", Toast.LENGTH_SHORT).show();
                        //creamos un intent
                        Intent intent = new Intent(RequestDriverActivity.this, MapsClientActivity.class);
                        //iniciamos el intent
                        startActivity(intent);
                        //finaliza todas las tareas
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    //destruimos todos los escuchadores de firebase despues que terminan las actividades
    protected void onDestroy() {
        super.onDestroy();
        //aseguramos que nuestro escuchador no esta vacio
        if (mListener != null) {
            //entramos en la clase ClientBookingProvider ejecutamos el metodo getStatus eso recibe
            //un id lo tenemos en mAuthProvider y luego pasamos el metodo para eliminar el escuchador
            mClientBookingProvider.getStatus(mAuthProvider.getId()).removeEventListener(mListener);
        }
    }
}