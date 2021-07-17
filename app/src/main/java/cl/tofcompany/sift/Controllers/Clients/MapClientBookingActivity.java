package cl.tofcompany.sift.Controllers.Clients;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import cl.tofcompany.sift.Providers.AuthProvider;
import cl.tofcompany.sift.Providers.ClientBookingProvider;
import cl.tofcompany.sift.Providers.DriverProvider;
import cl.tofcompany.sift.Providers.GeofireProvider;
import cl.tofcompany.sift.Providers.GoogleApiProvider;
import cl.tofcompany.sift.Providers.TokenProvider;
import cl.tofcompany.sift.R;
import cl.tofcompany.sift.Utils.DecodePoints;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
//implementemos el OnMapReadyCallback
public class MapClientBookingActivity extends AppCompatActivity implements OnMapReadyCallback {
    //variable del google map
    private GoogleMap mmap;
    //variable del supportfragment
    private SupportMapFragment mapFragment;
    //variable del LocationRequest
    private LocationRequest mLocationRequest = new LocationRequest();
    //variable del fusedlocationproviderclient
    private FusedLocationProviderClient mfuLocationProviderClient;
    //variable del marker
    private Marker mMarkerDriver;
    //variable del Authprovider
    AuthProvider mAuthProvider;
    //variable del geofireprovider
    private GeofireProvider mGeofireProvider;
    //variable booleana para saber si entro por primera vez
    private boolean mIsFirstTime = true;
    //variable del placesclient
    private PlacesClient mPlacesClient;
    //variable del origen
    private String mOrigin;
    //variable del origenlatlong
    private LatLng mOriginLatLng;
    //variable del destino
    private String mDetination;
    //variable del destinolatlong
    private LatLng mDestinationLatLng;
    //variable del latlong
    private LatLng mDriverLatLng;
    //variable del tokenprovider
    private TokenProvider mTokenProvider;
    //variable textview driver
    private TextView mTextViewDriverBooking;
    //variable textview email driver
    private TextView mTextViewEmailDriverBooking;
    //variable textview origen
    private TextView mtextVieworigin;
    //variable textview destino
    private TextView mTextViewdestination;
    //variable clientbookingprovider
    private ClientBookingProvider mClientBookingProvider;
    //variable list laltlon
    private List<LatLng> mpolyllineList;
    //variable polylyneoption
    private PolylineOptions mPolylineOptions;
    //variable googleapiprovider
    private GoogleApiProvider mGoogleApiProvider;
    //variable driverprovider
    private DriverProvider mDriverProvider;
    //variable valor del evento del escuchador
    private ValueEventListener mListener;
    //variable para recibir el iddriver
    private String midDriver;
    //variable textview estado
    private TextView mTextViewStatusBooking;
    //variable valor del evento del escuchador del estado
    private ValueEventListener mListenerStatus;
    //variable de image
    private CircleImageView mimageviewdriverbooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_client_booking);
       init();
    }
    private void init(){
        //inciando el variable de supportfragment con su id
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //inciando la classe authprovider
        mAuthProvider = new AuthProvider();
        //inciando la classe LocationServices y tomamos el metodo FusedLocationProviderClient y decimos que se muestra en esta actividad this
        mfuLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //inciando la classe geoprovider y lo pasamos el nodo drivers_working de firebase
        mGeofireProvider = new GeofireProvider("drivers_working");
        //inciando la clase de token
        mTokenProvider = new TokenProvider();
        //inciando el variable de txt driverbooking con su id
        mTextViewDriverBooking = findViewById(R.id.textviewdriverbooking);
        //inciando el variable de txt emaildriverbooking con su id
        mTextViewEmailDriverBooking = findViewById(R.id.textviewemaildriverbooking);
        //inciando el variable de txt origen con su id
        mtextVieworigin = findViewById(R.id.textvieworigindriverbooking);
        //inciando el variable de txt destino con su id
        mTextViewdestination = findViewById(R.id.textviewdestinationdriverbooking);
        //inciando la classe clientbookingprovider
        mClientBookingProvider = new ClientBookingProvider();
        ////inciando la classe googleApiProvider eso recibe un contexto que es esta actividad this
        mGoogleApiProvider = new GoogleApiProvider(MapClientBookingActivity.this);
        //inciando la classe de driverprovider
        mDriverProvider = new DriverProvider();
        //inciando el variable de txt statusbooking con su id
        mTextViewStatusBooking = findViewById(R.id.textviewstatusbooking);
        //inciando el variable de imageview con su id
        mimageviewdriverbooking = findViewById(R.id.imageviewdriverbooking);
        //lo pasamos el metro de getMapsAsync recibe un callback que es esta actividad this
        mapFragment.getMapAsync(this);
        //aseguramos que esta inicializado
        if (!Places.isInitialized()) {
            //pasamos al place la inicializacion eso ecibe un contexto y luego lo pasamos el api key de google maps
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }
        //metodo del estado
        getStatus();
        //metodo getclientbooking
        getClientBooking();
    }
    //metodo para tomar el estado en tiempo real y mostrarlo al cliente
    private void getStatus() {
        //nuesta escuchador lo iniciamos con la clase de BookingProvider y tomamos el metodo getstatus eso recibe
        //un id lo tenemos en mAuthProvider y tomamos el id y agregamos un evento de firebas
        mListenerStatus = mClientBookingProvider.getStatus(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //aseguramos que el datos exite
                if (dataSnapshot.exists()) {
                    //declaramos ua variable de status y lo pasamos los que viene en firebase mediante datasnapshot
                    String status = dataSnapshot.getValue().toString();
                    //preguntamos si el estado es igual a aceptado
                    if (status.equals("accept")) {
                        //enviamo en la vista del front ese texto
                        mTextViewStatusBooking.setText("Estado: Aceptado");
                    }
                    //preguntamos si el estado es igual a iniciado
                    if (status.equals("start")) {
                        //enviamo en la vista del front ese texto
                        mTextViewStatusBooking.setText("Estado: Viaje Iniciado");
                        //ejecutamos el metodo de empezar el booking
                        startBooking();
                    }
                    //preguntamos si el estado es igual a finalizado
                    else if (status.equals("finish")) {
                        //enviamo en la vista del front ese texto
                        mTextViewStatusBooking.setText("Estado: Viaje Finalizado");
                        //ejecutamos el metodo de finalizar el booking
                        finishBooking();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
//metodo para cancelar la busqueda
    private void finishBooking() {
        //creamos un intent para enviar el usuario en CalificationDriver
        Intent intent = new Intent(MapClientBookingActivity.this, CalificationDriver.class);
        //iniciamos el intent
        startActivity(intent);
        //terminamos la actividad
        finish();
    }
//metdo para iniciar la busqueda
    private void startBooking() {
        //limpiamos todos los rastros del mapa
        mmap.clear();
        //agregamos el marcador del punto de destino
        mmap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.mappinblue)));
        //marcamos la ruta
        drawRoute(mDestinationLatLng);
    }

    @Override
    //metodo cuando se termine todas las actividades cerramos el escuchado de firebase en tiempo real
    protected void onDestroy() {
        super.onDestroy();
        //preguntamos si nuestro escuchador no esta vacio
        if (mListener != null) {
            //entramos en la clase geofireProvider ejecutamos el metodo getdriverlocation  eso recibe el id del conductor
            //lo tenemos en midDriver y luego retiramos el escuchador
            mGeofireProvider.getDriverLocation(midDriver).removeEventListener(mListener);
        }
        //preguntamos si nuestro escuchador del estado no esta vacio
        if (mListenerStatus != null) {
            //entramos en la clase ClientBookingProvider ejecutamos el metodo getStatus  eso recibe el id
            //lo tenemos en mAuthProvider y luego retiramos el escuchador
            mClientBookingProvider.getStatus(mAuthProvider.getId()).removeEventListener(mListenerStatus);
        }
    }
    //metodo para obtener informacion del conductor y mostrarlo al cliente
    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String destination = dataSnapshot.child("destination").getValue().toString();
                    String origin = dataSnapshot.child("origin").getValue().toString();
                    String idDriver = dataSnapshot.child("idDriver").getValue().toString();
                    midDriver = idDriver;
                    double destinatioLat = Double.parseDouble(dataSnapshot.child("destinationLat").getValue().toString());
                    double destinatioLng = Double.parseDouble(dataSnapshot.child("destinationLng").getValue().toString());

                    double originLat = Double.parseDouble(dataSnapshot.child("originLat").getValue().toString());
                    double originLng = Double.parseDouble(dataSnapshot.child("originLng").getValue().toString());
                    mOriginLatLng = new LatLng(originLat, originLng);
                    mDestinationLatLng = new LatLng(destinatioLat, destinatioLng);
                    mtextVieworigin.setText("recoger en: " + origin);
                    mTextViewdestination.setText("destino: " + destination);
                    mmap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.mappin)));
                    getDriver(idDriver);
                    getDriverLocation(idDriver);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    //metodo para tomar info de los conductores
    private void getDriver(String idDriver) {
        //entramos en la clase de DriverProvider y tomamos el metodo de getDriver eso reciber el idDriver
        //lo tenemos por parametro en el metodo y luego ejecutamos un metodo de firebase
        mDriverProvider.getDriver(idDriver).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //preguntamos si el datos existe
                if (dataSnapshot.exists()) {
                    //creamos un variable de name y lo pasamos lo que viene en firebase mediande de datasapshot
                    String name = dataSnapshot.child("name").getValue().toString();
                    //creamos un variable de email y lo pasamos lo que viene en firebase mediande de datasapshot
                    String email = dataSnapshot.child("email").getValue().toString();
                    //creamos un variable de image lo iniciamos en vacio
                    String image = "";
                    //preguntamos si en el nodo image hay un imagen ya agregado
                    if (dataSnapshot.hasChild("image")){
                        // pasamos a image lo que viene en firebase mediande de datasapshot
                        image = dataSnapshot.child("image").getValue().toString();
                        //invocamos a picasso para processar el imagen y luego enviarlo en la vista
                        Picasso.get().load(image).into(mimageviewdriverbooking);
                    }
                    //enviar lo que hay en variable name en la vista
                    mTextViewDriverBooking.setText(name);
                    //enviar lo que hay en variable email en la vista
                    mTextViewEmailDriverBooking.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //metodo para tomar la position del conductor que fue asignado al cliente en tiempo real
    private void getDriverLocation(String idDriver) {
        //asignamos a nuestro escuchador la clase GeofireProvider y tomamos el metodo getDriverLocation
        //eso recibe el idDriver lo tenemos por parametro del metodo y luego ejecutamos un metodo de firebase
        mListener = mGeofireProvider.getDriverLocation(idDriver).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //aseguramos que el datos existe
                if (dataSnapshot.exists()) {
                    //creamos un variable latitude y lo pasamos lo que viene en firebase por el datasnapshot
                    double lat = Double.parseDouble(dataSnapshot.child("0").getValue().toString());
                    //creamos un variable longitude y lo pasamos lo que viene en firebase por el datasnapshot
                    double lng = Double.parseDouble(dataSnapshot.child("1").getValue().toString());
                    //pasamos a mDriverLatLng el lat y el lng
                    mDriverLatLng = new LatLng(lat, lng);
                    //aseguramos que el macador no esta vacio
                    if (mMarkerDriver != null) {
                        //sacamos el macador
                        mMarkerDriver.remove();
                    }
                    //agregamos el marcador
                    mMarkerDriver = mmap.addMarker(new MarkerOptions()
                            //con la position lat y lng
                            .position(new LatLng(lat, lng))
                            //con el titulo
                            .title("Tu conductor")
                            //con el icono
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.camion_grua)));
                    //aseguremos si es por primera vez hizo la solicitude
                    if (mIsFirstTime) {
                        //lo iniciamos en falso
                        mIsFirstTime = false;
                        //ponemos la animacion de la camara para enviarnos a esa direccion
                        mmap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        //pasamos el latlong del conductor
                                        .target(mDriverLatLng)
                                        //ponemos un zoom de 15f
                                        .zoom(15f)
                                        .build()
                        ));
                        //trazamos la ruta
                        drawRoute(mOriginLatLng);
                    }
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
        mGoogleApiProvider.getDirections(mDriverLatLng, latLng).enqueue(new Callback<String>() {
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
                    //enciar todas las opciones al polilynelist
                    mPolylineOptions.addAll(mpolyllineList);
                    //agregamos los polyline en el mapa
                    mmap.addPolyline(mPolylineOptions);
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


    @Override
    //metodo onmapready para el mapa
    public void onMapReady(GoogleMap googleMap) {
        // pasar al mapa el googlemap
        mmap = googleMap;
        //enviar el tipo de mapa
        mmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //activar que el usuario puede hacer zoom
        mmap.getUiSettings().setZoomControlsEnabled(true);
        //aseguramos que los permisos ya han conseguidos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
          return;
        }
        //enviamos nuestra ubicacion actual al mapa
        mmap.setMyLocationEnabled(true);

    }
}

