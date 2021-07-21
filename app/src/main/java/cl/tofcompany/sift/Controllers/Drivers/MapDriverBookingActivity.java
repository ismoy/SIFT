package cl.tofcompany.sift.Controllers.Drivers;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.tofcompany.sift.Model.FCMBody;
import cl.tofcompany.sift.Model.FCMResponse;
import cl.tofcompany.sift.Model.Info;
import cl.tofcompany.sift.Providers.AuthProvider;
import cl.tofcompany.sift.Providers.ClientBookingProvider;
import cl.tofcompany.sift.Providers.ClientProvider;
import cl.tofcompany.sift.Providers.GeofireProvider;
import cl.tofcompany.sift.Providers.GoogleApiProvider;
import cl.tofcompany.sift.Providers.InfoProvider;
import cl.tofcompany.sift.Providers.NotificationProvider;
import cl.tofcompany.sift.Providers.TokenProvider;
import cl.tofcompany.sift.R;
import cl.tofcompany.sift.Utils.DecodePoints;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
//implementemos el OnMapReadyCallback
public class MapDriverBookingActivity extends AppCompatActivity implements OnMapReadyCallback {
    //variable del google map
    private GoogleMap mMap;
    //variable del supportfragment
    private SupportMapFragment mMapFragment;
    //variable de la clase Authprovider
    private AuthProvider mAuthProvider;
    //variable de la clase GeofireProvider
    private GeofireProvider mGeofireProvider;
    //variable de la clase TokenProvider
    private TokenProvider mTokenProvider;
    //variable del LocationRequest
    private LocationRequest mLocationRequest;
    //variable del fusedlocationproviderclient
    private FusedLocationProviderClient mFusedLocation;
    //variable definido para inicializar el codigo de la locacion en 1
    private final static int LOCATION_REQUEST_CODE = 1;
    //variable definido para inicializar el settings code en 2
    private final static int SETTINGS_REQUEST_CODE = 2;
    //variable del marcador del cliente
    private Marker mMarker;
    //variable para tomar la latitude y la longitude en tiempo real
    private LatLng mCurrentLatLng;
    //variable TextView name
    private TextView mTextViewClientBooking;
    //variable TextView email
    private TextView mTextViewEmailClientBooking;
    //variable para recibir el id del cliente a traves de un intent extra
    private String mExtraClientId;
    //variable de la clase ClientProvider
    private ClientProvider mcClientProvider;
    //variable de la clase ClientBookingProvider
    private ClientBookingProvider mClientBookingProvider;
    //variable list laltlong
    private List<LatLng> mpolyllineList;
    //variable polylyneoption proviene de la api de google
    private PolylineOptions mPolylineOptions;
    //variable de la latitude y longitude del origen
    private LatLng mOriginLatLng;
    //variable de la latitude y longitude del destino
    private LatLng mDestinationLatLng;
    //variable de la clase GoogleApiProvider
    private GoogleApiProvider mGoogleApiProvider;
    //variable del textview de origen
    private TextView mtextVieworigin;
    //variable del textview de destino
    private TextView mTextViewdestination;
    //variable boleana para saber si entro por primera vez esta inicializado en true
    private boolean mIsFirstTime = true;
    //variable btn empezar el viaje
    private Button mButtonstarbooking;
    //variable btn finalizar viaje
    private Button mButtonendbooking;
    //variable boleana para saber si el conductor esta cerca de cliente lo inicie en false
    private boolean mIsCloseToClient = false;
    //variable boleana para saber si el cliente ya monta en el carro lo inicie en false
    private boolean mRideStart = false;
    //variable de la clase NotificationProvider
    private NotificationProvider mNotificationProvider;
    //variable ImageView para la foto del cliente
    private ImageView mimageviewclientbooking;
    //variable de la clase InfoProvider
    private InfoProvider mInfoProvider;
    //variable TextView para almacenar el tiempo
    private TextView mTextViewTime;
    //variable TextView para almacenar la distancia
    private TextView mTextViewkm;
    //variable de la clase Info
    //Aviso en mi caso la clase info aqui esta las informaciones del costo del viaje
    private Info mInfo;
    //variable de la distancia en metro lo iniciamos en 1
    double mDistanceMeters  = 1;
    //variable del minutos lo iniciamos en 0
    int mMinutes = 0;
    //variable del segundo lo iniciamos en 0
    int mSeconds = 0;
    //variable boleana para saber si el el segundo se termino lo inicie en false
    boolean mSecondsIsOver = false;
    Handler mHandler = new Handler();
    //sacamos la locacion
    Location mpreviewLocation = new Location("");
    //metodo para el contador de tiempo
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //para incrementar los segundo
            mSeconds++;
            //preguntamos si todavia estamos en segundos es decir aun no hemos llegado a 60 segundos
            if (!mSecondsIsOver){
                //muestramos los segundos desde que empezo el viaje en la vista
                mTextViewTime.setText(mSeconds + " Seg ");
            }else {
                //si ya pasamos los 60 segundos ahora enviamos los minutos mas los segundo que inicio el viaje
                mTextViewTime.setText(mMinutes + "Min " + mSeconds);
            }
            //pregunstamos si el segundo esta en 59
            if (mSeconds == 59){
                //ponemos el segundo en estado 0
                mSeconds = 0;
                //y decimos que ya no hay segundo y ponemos en true
                mSecondsIsOver = true;
                //y ahora mostramos minutos qye va incrementado
                mMinutes++;
            }
            //decimos que nuestro handle tendra un postDelayed eso recibe un runnable y el tiempo que quiere que
            //se ejecuta en mi caso quiero que cada 1 segundo
          mHandler.postDelayed(runnable,1000);
        }
    };
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            //recorremos el Location
            for (Location location : locationResult.getLocations()) {
                //preguntamos si esa location que llega en este contexto en distinto a null
                if (getApplicationContext() != null) {
                    //pasamos a mCurrentLatLng la latitude y la longitude de la location que recibi
                    mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    //aseguramos que el marcador no esta vacio
                    if (mMarker != null) {
                        //si viene con datos los eliminamos
                        mMarker.remove();
                    }
                    //preguntamos si el el conductor esta cerca
                    if (mRideStart){
                        //pasamos al mDistanceMeters la distancia en metro mas el previsto de la locacion
                        //junto con la distancia del cliente
                        mDistanceMeters = mDistanceMeters + mpreviewLocation.distanceTo(location);
                        Log.d("ENTRO", "Distancia recorrido: " +mDistanceMeters);
                        //enviamos en la vista la distancia pero en km
                        mTextViewkm.setText(String.format("%.1f", mDistanceMeters) + " KM ");
                    }
                    //pasamos a mpreviewLocation la locacion actual
                    mpreviewLocation = location;
                    //pasamos el nuevo marcador a nuestra mMarker junto con la position actual
                    //y la latitude y la longitude de la location
                    mMarker = mMap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(), location.getLongitude())
                            )
                               //titulo que va aparecer si el conductor da click en el punto
                                    .title("Tu posicion")
                            //el icono que va va a ver el conductor en su pantalla
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.camion_grua))
                    );
                    // OBTENER LA LOCALIZACION DEL USUARIO EN TIEMPO REAL
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(16f)
                                    .build()
                    ));
                    //actualizamos la location
                    updateLocation();
                    //aseguramos que es por primera vez
                    if (mIsFirstTime) {
                        //y lo ponemos en false
                        mIsFirstTime = false;
                        //metodo para tomar los datos
                        getClientBooking();
                    }

                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_driver_booking);
    init();
    }

    private void init(){
        //iniciando la clase authprovider
        mAuthProvider = new AuthProvider();
        //iniciando la clase GeofireProvider
        mGeofireProvider = new GeofireProvider("drivers_working");
        //iniciando la clase TokenProvider
        mTokenProvider = new TokenProvider();
        //iniciando la clase ClientProvider
        mcClientProvider = new ClientProvider();
        //iniciando la clase ClientBookingProvider
        mClientBookingProvider = new ClientBookingProvider();
        //iniciando la clase NotificationProvider
        mNotificationProvider = new NotificationProvider();
        //iniciando la clase LocationServices y tomamos el metodo FusedLocationProviderClient y
        // decimos que se muestra en esta actividad this
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        //inciando el variable de supportfragment con su id
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //lo pasamos el metro de getMapsAsync recibe un callback que es esta actividad this
        mMapFragment.getMapAsync(this);
        //iniciando el variable de txt name con su id
        mTextViewClientBooking = findViewById(R.id.textviewclientbooking);
        //iniciando el variable de txt email con su id
        mTextViewEmailClientBooking = findViewById(R.id.textviewemailclientbooking);
        //iniciando el variable de txt origen con su id
        mtextVieworigin = findViewById(R.id.textvieworiginclientbooking);
        //iniciando el variable de txt destino con su id
        mTextViewdestination = findViewById(R.id.textviewdestinationclientbooking);
        //iniciando el variable btn iniciar viaje con su id
        mButtonstarbooking = findViewById(R.id.btnstartbooking);
        //iniciando el variable btn finalizar con su id
        mButtonendbooking = findViewById(R.id.btnendtbooking);
        //iniciando el variable de imageView  con su id
        mimageviewclientbooking = findViewById(R.id.imageviewclientbooking);
        //recibiendo el id del cliente mediante de un intent extra
        mExtraClientId = getIntent().getStringExtra("idClient");
        //iniciando la clase GoogleApiProvider recibe un contexte que es this
        mGoogleApiProvider = new GoogleApiProvider(MapDriverBookingActivity.this);
        //iniciando el variable del tiempo  con su id
        mTextViewTime = findViewById(R.id.textviewtime);
        //iniciando la clase InfoProvider
        mInfoProvider = new InfoProvider();
        //iniciando el variable del distancia  con su id
        mTextViewkm = findViewById(R.id.textviewkm);
        //metodo de info aqui esta las informaciones del costo
        getInfo();
        //metodo para tomar los datos del cliente
        getClient();
        //agregamos un evento onclick en el button de iniciar viaje
        mButtonstarbooking.setOnClickListener(view -> {
            //preguntamos si esta cerca del cliente
            if (mIsCloseToClient) {
                //iniciamos el viaje
                startBooking();
            } else {
                //si no esta cerca del muestramos un mensaje
                Toast.makeText(MapDriverBookingActivity.this, "Debes estar mas cerca a la posicion de recogida", Toast.LENGTH_SHORT).show();
            }
        });
        //agregamos un evento onclick en el button de finalizar viaje
        mButtonendbooking.setOnClickListener(view ->
                //metodo para finalizar viaje
                finishBooking());
    }
//metodo para calcular el ride
    public void calculateRide(){
        //preguntamos si el mMinutes es igual la 0
        if (mMinutes == 0){
            // si es asi lo iniciamos en 1 para el minutos
            mMinutes = 1;
        }
        //calculate el precio por minutos
        double priceMin = mMinutes * mInfo.getMin();
        //calculate el precio por km
        double priceKm = (mDistanceMeters / 1000) * mInfo.getKm();
        Log.d("Valores","Min total: " + mMinutes);
        Log.d("Valores","Km total: " + (mDistanceMeters / 1000));
        //calculate el total del viaje
        double total = priceMin + priceKm;
        //entramos en la clase ClientBookingProvider ejecutamos el metodo updatePrice
        //eso recibe el id del cliente lo tenemos en mExtraClientId,tb recibe el total
        mClientBookingProvider.updatePrice(mExtraClientId,total).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //entramos en la clase ClientBookingProvider ejecutamos el metodo updateStatus
                //eso recibe el id cliente lo tenemos en mExtraClientId tb recibe un estado
                //en mi caso es finalizado
                mClientBookingProvider.updateStatus(mExtraClientId, "finish");
                //creamos un intent
                Intent intent = new Intent(MapDriverBookingActivity.this, CalificationClient.class);
                //ese intent va enviar datos mediante putExtra
                //enviando el mExtraClientId
                intent.putExtra("idClient", mExtraClientId);
                //enviando el precio
                intent.putExtra("price", total);
                //iniciamos el intent
                startActivity(intent);
                //finalizamos todas las actividades
                finish();
            }
        });

    }
    //metodo para tomar el valor del costo del minutos y km
    private void getInfo() {
        //entramos en la clase InfoProvider ejecutamos el metodo getInfo
        mInfoProvider.getInfo().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                //aseguramos que el datos existe
                if (snapshot.exists()){
                    //enviamos en la variable de mInfo el valor que costo que encontro en firebase
                     mInfo = snapshot.getValue(Info.class);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    //metodo para terminar un viaje
    private void finishBooking() {
        //entramos en la clase ClientBookingProvider ejecutamos el metodo updateIdHistoryBooking
        //eso recibe el id del cliente lo tenemos en mExtraClientId
        mClientBookingProvider.updateIdHistoryBooking(mExtraClientId).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //si la solicitud sea exitoso cambiamos en estado en viaje finalizado
                sendNotification("Viaje finalizado");
                //aseguramos que el mFusedLocation no esta vacio
                if (mFusedLocation != null) {
                    //si viene con datos ejecutamos el metodo removeLocationUpdates
                    mFusedLocation.removeLocationUpdates(mLocationCallback);
                }
                //entramos en GeofireProvider ejecutamos el metodo removelocation
                //esos ricibe un id lo tenemod en mAuthProvider
                mGeofireProvider.removelocation(mAuthProvider.getId());
                //pregunta si el mHandler no esta vacio
                if (mHandler != null) {
                    //ejecutamos el metodo removeCallbacks y lo pasamos el runnable
                    mHandler.removeCallbacks(runnable);
                }
                //recien calculamos el precio
                calculateRide();
                //desconectar el conductor
                disconnect();
            }
        });

    }
    //metodo para iniciar un viaje
    private void startBooking() {
        //entramos en la clase de ClientBookingProvider y ejecutamos el metodo updateStatus
        //eso recibe el id cliente lo tenemos en mExtraClientId tb el estado
        // en este caso va ser iniciado
        mClientBookingProvider.updateStatus(mExtraClientId, "start");
        //ocultamos el boton iniciar viaje
        mButtonstarbooking.setVisibility(View.GONE);
        //mostramos el boton finalizar viaje
        mButtonendbooking.setVisibility(View.VISIBLE);
        //limpiamos todos los marcadores
        mMap.clear();
        //agregamos el punto de destino
        mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.mappinblue)));
        //trazar la ruta y lo pasamos la latitude y la longitude del destino
        drawRoute(mDestinationLatLng);
        //enviamos un mensaje al clente para decirle el estado de su viaje
        sendNotification("Viaje iniciado");
        //iniciamos el contador de los minutos
        mRideStart = true;
        //cada 1 segundo se ejecutara mi metodo
        mHandler.postDelayed(runnable,1000);

    }
    //metodo para obtener cuantas distancias hay entre el conductor y el cliente
    private double getDistanceBetween(LatLng clientLatLng, LatLng driverLatLng) {
        //inciamos la distancia en o
        double distance = 0;
        //tomamos la ubicacion del cliente
        Location clientLocation = new Location("");
        //tomamos la ubicacion del conductor
        Location driverLocation = new Location("");
        //enviamos la latitude del cliente
        clientLocation.setLatitude(clientLatLng.latitude);
        //enviamos la longitude del cliente
        clientLocation.setLongitude(clientLatLng.longitude);
        //enviamos la latitude del conductor
        driverLocation.setLatitude(driverLatLng.latitude);
        //enviamos la longitude del conductor
        driverLocation.setLongitude(driverLatLng.longitude);
        //pasamos al distance el clientLocation distanceTo nos da un aproximado entre la distancia
        //del clientLocation y el driverLocation
        distance = clientLocation.distanceTo(driverLocation);
        //retornamos la distancia
        return distance;
    }
    //metodo para obtener informacion de la solicitud del viaje
    private void getClientBooking() {
        //entramos en la clase ClientBookingProvider ejecutamos el metodo getClientBooking eso
        //recibe el id del cliente lo tenemos en mExtraClientId
        mClientBookingProvider.getClientBooking(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //aseguramos que el datos existe
                if (dataSnapshot.exists()) {
                    //creamos un variable destination para almacenar el destino que proviene de firebase
                    //mediante dataSnapshot.child("destination")
                    String destination = dataSnapshot.child("destination").getValue().toString();
                    //creamos un variable origin para almacenar el origen que proviene de firebase
                    //mediante dataSnapshot.child("origin")
                    String origin = dataSnapshot.child("origin").getValue().toString();
                    //creamos un variable destinatioLat para almacenar la latitude del destino
                    // que proviene de firebase
                    //mediante dataSnapshot.child("destinationLat")
                    double destinatioLat = Double.parseDouble(dataSnapshot.child("destinationLat").getValue().toString());
                    //creamos un variable destinatioLng para almacenar la longitude del destino
                    // que proviene de firebase
                    //mediante dataSnapshot.child("destinationLng")
                    double destinatioLng = Double.parseDouble(dataSnapshot.child("destinationLng").getValue().toString());
                    //creamos un variable originLat para almacenar la latitude del origen
                    // que proviene de firebase
                    //mediante dataSnapshot.child("originLat")
                    double originLat = Double.parseDouble(dataSnapshot.child("originLat").getValue().toString());
                    //creamos un variable originLng para almacenar la longitude del origen
                    // que proviene de firebase
                    //mediante dataSnapshot.child("originLng")
                    double originLng = Double.parseDouble(dataSnapshot.child("originLng").getValue().toString());
                    //iniciamos mOriginLatLng con un nuevo LatLng y lo pasamos la latitude del origen y la longitude
                    mOriginLatLng = new LatLng(originLat, originLng);
                    //iniciamos mDestinationLatLng con un nuevo LatLng y lo pasamos la latitude del destino y la destino
                    mDestinationLatLng = new LatLng(destinatioLat, destinatioLng);
                    //enviamos en la vista el origen
                    mtextVieworigin.setText("recoger en: " + origin);
                    //enviamos en la vista el destino
                    mTextViewdestination.setText("destino: " + destination);
                    //agremamos el marcador del punto de la posicion
                    mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.mappin)));
                    //trazar la ruta con el nuevo latitude y longitude del origen
                    drawRoute(mOriginLatLng);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    //metodo para trazar la ruta
    private void drawRoute(LatLng latLng) {
        //llamamos la clase de GoogleApiProvider y tomamos el metodo getDirections recibe un orienlatlon,
        //un destinolatlng y ejecutamos el metodo enqueue y yn new callback
        mGoogleApiProvider.getDirections(mCurrentLatLng, latLng).enqueue(new Callback<String>() {
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
                    //asignamos en nuestra variable mpolyllineList el points
                    mpolyllineList = DecodePoints.decodePoly(points);
                    //iniciamos la clase polilineOptions
                    mPolylineOptions = new PolylineOptions();
                    //recibe un color
                    mPolylineOptions.color(Color.DKGRAY);
                    //recibe un ancho en float
                    mPolylineOptions.width(13f);
                    //solicitamos el metodo startcap
                    mPolylineOptions.startCap(new SquareCap());
                    //el jointType esta  usado por todos los polyline excepto el inicio y el final del vertice
                    mPolylineOptions.jointType(JointType.ROUND);
                    //iniciar todas las opciones al polilynelist
                    mPolylineOptions.addAll(mpolyllineList);
                    //agregamos los polyline en el mapa
                    mMap.addPolyline(mPolylineOptions);
                    //aviso los que dicen distance. duration text son de la Api
                    //recibimos el response de legs del objeto json
                    JSONArray legs = route.getJSONArray("legs");
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


                } catch (Exception e) {
                    Log.d("Error", "Error encontrado " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }
    //metodo para tomar los datos de los clientes
    private void getClient() {
        //entramos en la clase de ClientProvider y tomamos el metodo de getClient eso reciber el idClient
        //lo tenemos en mExtraClientId y luego ejecutamos un metodo de firebase
        mcClientProvider.getClient(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //preguntamos si el datos existe
                if (dataSnapshot.exists()) {
                    //creamos un variable de email y lo pasamos lo que viene en firebase mediande de datasapshot
                   String email = dataSnapshot.child("email").getValue().toString();
                    //creamos un variable de name y lo pasamos lo que viene en firebase mediande de datasapshot
                    String name = dataSnapshot.child("name").getValue().toString();
                    //creamos un variable de image lo iniciamos en vacio
                    String image = "";
                    //preguntamos si en el nodo image hay un imagen ya agregado
                    if (dataSnapshot.hasChild("image")) {
                        // pasamos a image lo que viene en firebase mediande de datasapshot
                        image = dataSnapshot.child("image").getValue().toString();
                        //invocamos a picasso para processar el imagen y luego enviarlo en la vista
                        Picasso.get().load(image).into(mimageviewclientbooking);
                    }
                    //enviar lo que hay en variable name en la vista
                    mTextViewClientBooking.setText(name);
                    //enviar lo que hay en variable email en la vista
                    mTextViewEmailClientBooking.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //metodo para actualizar la ubicacion del conductor en tiempo real
    private void updateLocation() {
        //aseguramos que existe una session y que el usuario esta activo y que no sea vacio
        if (mAuthProvider.existSession() && mCurrentLatLng != null) {
            //entramos en la clase GeofireProvider ejecutamos el metodo savelocation eso recibe
            //el id lo tenemos en mAuthProvider tb la latitude y la logitude actual
            // lo tenemos en  mCurrentLatLng
            mGeofireProvider.savelocation(mAuthProvider.getId(), mCurrentLatLng);
            //si no esta cerca del cliente
            if (!mIsCloseToClient) {
                //si la latitude y la longitude de origin no esta vacio y la latitude
                // y la longitude de la ubicacion del cliente no esta vacio
                if (mOriginLatLng != null && mCurrentLatLng != null) {
                    //creamos un variable distance para guardar la distancia aproximado en metros
                    // que nos llego de mOriginLatLng y mCurrentLatLng
                    double distance = getDistanceBetween(mOriginLatLng, mCurrentLatLng); // METROS
                    //preguntamos si la distancia aproximado es mayor o igual a 200 metros
                    if (distance <= 200) {
                        //mButtonStartBooking.setEnabled(true);
                        //ponemos que estar cerca del cliente en true
                        mIsCloseToClient = true;
                        //enviamos un mensaje al conductor
                        Toast.makeText(this, "Estas cerca a la posicion de recogida", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
    //metodo para desconectar
    private void disconnect() {
        //saber si la locacion esta vacio
        if (mFusedLocation != null) {
            //si viene con datos eliminamos la location
            mFusedLocation.removeLocationUpdates(mLocationCallback);
            //preguntamos si la existe session
            if (mAuthProvider.existSession()) {
                //en caso que si eliminamos la session con el id lo tenomos en mAuthProvider
                mGeofireProvider.removelocation(mAuthProvider.getId());
            }
        }
        else {
            //mensjae de error
            Toast.makeText(this, "No te puedes desconectar", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    //metodo onmapready para el mapa
    public void onMapReady(GoogleMap googleMap) {
        // pasar al mapa el googlemap
        mMap = googleMap;
        //enviar el tipo de mapa
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //activar que el conductor puede hacer zoom
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //aseguramos que los permisos ya han conseguidos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
         return;
        }
        //enviamos nuestra ubicacion actual al mapa
        mMap.setMyLocationEnabled(false);
        //iniciamos el location request
        mLocationRequest = new LocationRequest();
        //enviamos un interval de 1 segundo
        mLocationRequest.setInterval(1000);
        //tb enviamos el interval mas rapido en 1 segundo
        mLocationRequest.setFastestInterval(1000);
        //la prioridad en mi caso es alta
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);
        //iniciamos la location
        startLocation();
    }

    @Override
    //metodo para saber el resultado de los permisos
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //preguntamos si el requestCode es igual a LOCATION_REQUEST_CODE recuerda ese variable
        // lo tenemos definido arriba y iniciado en 1
        if (requestCode == LOCATION_REQUEST_CODE) {
            //aplicar los permisos
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //chequear los permisos
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //saber si el gps esta activado
                    if (gpsActived()) {
                        //en caso si tomamos su ubicacion
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    } else {
                        //en caso no muestramos un alert
                        showAlertDialogNOGPS();
                    }
                } else {
                    //chequear permisos
                    checkLocationPermissions();
                }
            } else {
                // TODO: 19/07/2021   chequear permisos
                checkLocationPermissions();
            }
        }
    }

    @Override
    //metdod para el resultado de requestCode
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //validar su el requescode es igual a SETTINGS_REQUEST_CODE ese variable lo tenemos
        // inicializado arriba en 2 y tb validamos que el gps esta activado
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived()) {
            //chequear los permisos
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //en caso todos ok su ubicacion
            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
        else {
            //en caso no muestramos alert
            showAlertDialogNOGPS();
        }
    }
     //metodo para mostrar la alerta cuando el gps no esta activado
    private void showAlertDialogNOGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //cuerpo del mensaje
        builder.setMessage("Por favor activa tu ubicacion para continuar")
                //boton positivo
                .setPositiveButton("Configuraciones", (dialogInterface, i) ->
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                                SETTINGS_REQUEST_CODE)).create().show();
    }
    //metodo para activar el gps
    private boolean gpsActived() {
        //creamos un variable boleana y lo iniciamos en false
        boolean isActive = false;
        //solicitando la location
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //preguntamos si la location ya esta acrivado
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //pues ponemos isActive en true
            isActive = true;
        }
        return isActive;
    }
    //metodo para empezar a tomar la locacion
    private void startLocation() {
        //verificamos si el sdk que esta corriendo es mayor o igual al version android Marshmellow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //chequear permisos
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //preguntar si el gps esta activado
                if (gpsActived()) {
                    //si esta activado tomamos su ubicacion
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                }
                else {
                    //en caso no alert error
                    showAlertDialogNOGPS();
                }
            }
            else {
                // TODO: 19/07/2021   chequear permisos
                checkLocationPermissions();
            }
        } else {
            //preguntar si el gps esta activado
            if (gpsActived()) {
                //en caso si tomamos la ubicacion
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            }
            else {
                //en caso no muestamos el alert error
                showAlertDialogNOGPS();
            }
        }
    }
    //metodo para chequear el permiso de la localicacion
    private void checkLocationPermissions() {
        // TODO: 19/07/2021   chequear permisos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        //titulo
                        .setTitle("Proporciona los permisos para continuar")
                        //cuerpo message
                        .setMessage("Esta aplicacion requiere de los permisos de ubicacion para poder utilizarse")
                        //buton positivo
                        .setPositiveButton("OK", (dialogInterface, i) ->
                                ActivityCompat.requestPermissions(MapDriverBookingActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE))
                        .create()
                        .show();
            }
            else {
                ActivityCompat.requestPermissions(MapDriverBookingActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }
    //metodo para enviar la notificacio eso recibe un estado
    private void sendNotification(final String status) {
        //entamos en la clase TokenProvider ejecutamos el metodo getToken eso recibe in id cliente
        //eso lo tenemos en mExtraClientId
        mTokenProvider.getToken(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //aseguramos que el dato existe
                if (dataSnapshot.exists()) {
                    //creamos un variable token para almacenar los que proviene de firebase
                    //mediante dataSnapshot.child("token")
                    String token = dataSnapshot.child("token").getValue().toString();
                    //creamos un hasmap
                    Map<String, String> map = new HashMap<>();
                    //enviamos el titulo a traves de un map.put
                    map.put("title", "ESTADO DE TU VIAJE");
                    //enviamos el body a traves de un map.put
                    map.put("body",
                            "Tu estado del viaje es: " + status
                    );
                    //iniciamos la clase FCMBody como un nuevo objeto eso recibe el token
                    // tb una prioridad en mi caso en alto, tb un tiempo en mi caso es 4.5s
                    //tb recibe el map
                    FCMBody fcmBody = new FCMBody(token, "high", "4500s", map);
                    //entramos en la clase NotificationProvider ejecutamos el metodo sendNotification
                    //eso recibe el fcmBody
                    mNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            //verificar si en body no llega vacio
                            if (response.body() != null) {
                                //pregunta si el exito del body es distinto a 1
                                if (response.body().getSuccess() != 1) {
                                    //enviamos un mensaje
                                    Toast.makeText(MapDriverBookingActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                //enviamos un mensaje
                                Toast.makeText(MapDriverBookingActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
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
                    //aviso fijense que necesito que el token esta en los 2 dispositivo
                    //tanto conductor y usuario porque es atraves del token vamos a comunicarnos
                    //con las notificaciones es un algorithmo que desarrolle para esta funcion
                    //y aplicar mejor la opcion de push mensaje de firebase
                    Toast.makeText(MapDriverBookingActivity.this, "No se pudo enviar la notificacion porque el conductor no tiene un token de sesion", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



}