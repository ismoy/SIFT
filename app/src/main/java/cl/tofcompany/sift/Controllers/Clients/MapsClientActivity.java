package cl.tofcompany.sift.Controllers.Clients;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
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
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cl.tofcompany.sift.Controllers.Logins.MainActivity;
import cl.tofcompany.sift.Providers.AuthProvider;
import cl.tofcompany.sift.Providers.ClientBookingProvider;
import cl.tofcompany.sift.Providers.ClientProvider;
import cl.tofcompany.sift.Providers.GeofireProvider;
import cl.tofcompany.sift.Providers.TokenProvider;
import cl.tofcompany.sift.R;
//implementamos el OnMapReadyCallback
public class MapsClientActivity extends AppCompatActivity implements OnMapReadyCallback {
    //variable del google map
    private GoogleMap mmap;
    //variable del supportfragment
    private SupportMapFragment mapFragment;
    //variable del LocationRequest
    private LocationRequest mLocationRequest = new LocationRequest();
    //variable del fusedlocationproviderclient
    private FusedLocationProviderClient mfuLocationProviderClient;
    //definemos una variable intero de location_request_code con su valor
    private final static int LOCATION_REQUEST_CODE = 1;
    //definemos una variable intero de sttings_request_code con su valor
    private final static int SETTINGS_REQUEST_CODE = 2;
    //variable del marker
    private Marker mMarker;
    //variable del Authprovider
    AuthProvider mAuthProvider;
    //variable del geofireprovider
    private GeofireProvider mGeofireProvider;
    //variable del LatLng
    private LatLng mCurrentLatLng;
    //un array de la lista de los marcadores
    private List<Marker> mDriversMarkers = new ArrayList<>();
    //variable booleana para saber si entro por primera vez
    private boolean mIsFirstTime = true;
    //variable para autocompletar el campo de busqueda del origen cuando entra en la app o cambia su ubicacion
    private AutocompleteSupportFragment mAutocomplete;
    //variable para autocompletar el campo de busqueda del destino cuando entra en la app o cambia su ubicacion pero lo he desactivado por mi proposito
    private AutocompleteSupportFragment mAutocompleteDestino;
    //variable de PlacesClient
    private PlacesClient mPlacesClient;
    //variable de origin
    private String mOrigin;
    //variable de originLatLng
    private LatLng mOriginLatLng;
    //variable de destino
    private String mDetination;
    //variable de destino LatLong
    private LatLng mDestinationLatLng;
    //variable de btn request driver
    private Button mButtonRequestDriver;
    //variable de la clase TokenProvider
    private TokenProvider mTokenProvider;
    //variable de google map con su evento
    private GoogleMap.OnCameraIdleListener mCameraListener;
    //variable de drawerLayout
    private DrawerLayout drawerLayout;
    //variable txt name y email
    private TextView username, memail;
    //variable imagen
    private ImageView picture;
    //variable clase clientProvider
    private ClientProvider mClientProvider;
    //variable clase ClientBookingProvider
    private ClientBookingProvider mClientBookingProvider;
    // variable para el id del cliente
    private String midClient;
    // variable de la vista de layout
    private View layout;
    //llamamos el objeto LocationCallback y lo pasamos el evento de LocationResult
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            //permite de tomar la locacion exacta del usuario
            for (Location location : locationResult.getLocations()) {
                //aseguremos que no esta vacio
                if (getApplicationContext() != null) {
                    //pasamos al LatLong la latitude y la longitudes del usuario
                    mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    // OBTENER LA LOCALIZACION DEL USUARIO EN TIEMPO REAL
                    mmap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(15f)
                                    .build()
                    ));
                    //aseguremos si es por primera vez que se conecto
                    if (mIsFirstTime) {
                        //lo iniciamos en falso
                        mIsFirstTime = false;
                        //y activamos el metdodo que el conductor esta disponible
                        getActiveDrivers();
                        //para limitar la busqueda de ciudades
                        limitSearch();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_client);
        init();

    }
    //metodo para las initialization de los variable
    private void init(){
        //inciando la clase authprovider
        mAuthProvider = new AuthProvider();
        //inciando la classe geoprovider y lo pasamos el nodo active_driver de firebase
        mGeofireProvider = new GeofireProvider("active_drivers");
        //inciando la clase de token
        mTokenProvider = new TokenProvider();
        //inciando la clase LocationServices y tomamos el metodo FusedLocationProviderClient y decimos que se muestra en esta actividad this
        mfuLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //inciando el variable de supportfragment con su id
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //lo pasamos el metro de getMapsAsync recibe un callback que es esta actividad this
        mapFragment.getMapAsync(this);
        //iniciando el variable de btn reuestDriver con su id
        mButtonRequestDriver = findViewById(R.id.btnRequestDriver);
        // iniciando la clase ClientBookingProvider
        mClientBookingProvider = new ClientBookingProvider();
        //iniciando el drawerLayout con su id
        drawerLayout = findViewById(R.id.drawer_layout);
        //iniciando el layout con su id
        layout = findViewById(R.id.layoutmisservicios);
        //eso es para la opcion de historias deviaje en el navegation drawer
        layout.setOnClickListener(v -> gotoHistoryClient());
        //eso es para la opcion de message  en el navegation drawer
        View layoutmessage = findViewById(R.id.layoutmessage);
        layoutmessage.setOnClickListener(v -> {
            Toast.makeText(this, "Comming Soon", Toast.LENGTH_SHORT).show();
        });
        //eso es para la opcion de securitycenter  en el navegation drawer
        View layoutsecuritycenter = findViewById(R.id.layoutsecuritycenter);
        layoutsecuritycenter.setOnClickListener(v -> gotoSecurityCenter());
        //eso es para la opcion de el icono menu del navegation drawer
        ImageView imageView = findViewById(R.id.imagemenu);
        imageView.setOnClickListener(v -> {
            //open drawer
            openDrawer(drawerLayout);
        });
        //eso es para la opcion de help  en el navegation drawer
        View layouthelp = findViewById(R.id.layouthelp);
        layouthelp.setOnClickListener(v -> {
            Toast.makeText(this, "Comming Soon", Toast.LENGTH_SHORT).show();
        });
        //eso es para la opcion de configuration  en el navegation drawer
        View layoutconfiguration = findViewById(R.id.layoutconfiguration);
        layoutconfiguration.setOnClickListener(v -> {
            gotoConfiguration();
        });
        //eso es para la opcion de share and win  en el navegation drawer
        View layoutshareandwin = findViewById(R.id.layoutshareandwin);
        layoutshareandwin.setOnClickListener(v -> {
            shareAndWin();
        });
        //eso es para la opcion de wallet  en el navegation drawer
        View layoutwallet = findViewById(R.id.layoutwallet);
        layoutwallet.setOnClickListener(v -> Clickwallet(v));
        //eso es para la opcion de connect  en el navegation drawer
        View layoutconnectwithsift = findViewById(R.id.layoutconnectwithsift);
        layoutconnectwithsift.setOnClickListener(v -> {
            gotoConnectWithSIFT();
        });
        //eso es para la opcion de promotinal  en el navegation drawer
        View layoutpromotionalcode = findViewById(R.id.layoutpromotionalcode);
        layoutpromotionalcode.setOnClickListener(v -> {
            Toast.makeText(this, "Comming Soon", Toast.LENGTH_SHORT).show();
        });
        //iniciamos el picture con su id
        picture = findViewById(R.id.picture);
        //dar click en la foto para modificar su perfil
        picture.setOnClickListener(v -> gotoUpdateperfilclient());
        //iniciamos el name c  su id
        username = findViewById(R.id.textviewnamedrwawerclient);
        //iniciamos el email con su id
        memail = findViewById(R.id.textviewcorreodrawerclient);
        //metodo para tomar los datos del cliente
        getClientBooking();
        //iniciamos la clase de ClientProvider
        mClientProvider = new ClientProvider();
        //aseguremos que esta inicializado el places
        if (!Places.isInitialized()) {
            //pasamos al place la inicializacion eso recibe un contexto y luego lo pasamos el api key de google maps
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }
        //pasamos al mplacesClient el lugar y lo creamos en este contexto this
        mPlacesClient = Places.createClient(this);
        //para el autocomplite del origen
        instanceAutocompleteOrigin();
        //para el autocomplite del destino
        instanceAutocompleteDestination();
        //para cuando mueve la camara
        onCameraMove();
        //creamos un evento on click en el boton del reuestDriver
        mButtonRequestDriver.setOnClickListener(view -> MapsClientActivity.this.requestDriver());
        //iniciamos el variable  de logout con si id
        TextView logoutnavegationdrawer = findViewById(R.id.logoutnavegationdrawer);
        //agregamos un evento on click a esta option
        logoutnavegationdrawer.setOnClickListener(v -> logout(v));
        //generamos el token
        generateToken();
    }
     //metodo para la opcion de connect con sift
    private void gotoConnectWithSIFT() {
        //creamos un intent
        Intent intent = new Intent(MapsClientActivity.this, ConnectWithSIFT.class);
        //iniciamos el intent
        startActivity(intent);
    }

    //metodo para compartir mi app
    private void shareAndWin() {
        //creamos un intent
            Intent intent = new Intent(Intent.ACTION_SEND);
            //decimos que tipo de intent sera
            intent.setType("text/plain");
            //creamos un variable shareTittle y lo pasamos el link de google play de la app
            String shareTitle = ("https://play.google/store/apps/details?id=c0m.ready\n" +
                    "Free download");
            //para poner una descripcion de la app
            String shareBody = ("");
            //enviamos el shareTitle a traves de un extra
            intent.putExtra(Intent.EXTRA_TEXT , shareTitle);
           //enviamos el shareBody a traves de un extra
            intent.putExtra(Intent.EXTRA_SUBJECT , shareBody);
            //inciamos el intent con el metodo createChooser recibe el intent y un titulo
            startActivity(Intent.createChooser(intent , "Share"));

    }
    //metodo para ir en la configuration
    private void gotoConfiguration() {
        //creamos un intent
        Intent intent = new Intent(MapsClientActivity.this, Configuration.class);
        //iniciamos el intent
        startActivity(intent);
    }
    // metodo para ir en la opcion de seguridad
    private void gotoSecurityCenter() {
        //creamos un intent
        Intent intent = new Intent(MapsClientActivity.this, SecurityCenter.class);
        //iniciamos el intent
        startActivity(intent);
    }
   //metodo para la opcion para ir en las historias
    private void gotoHistoryClient() {
        //creamos el intent
        Intent intent = new Intent(MapsClientActivity.this,HistoryBookingClient.class);
        //iniciamos el intent
        startActivity(intent);
    }

    //metodo cuando da click soble la foto perfil para ir al update
    private void gotoUpdateperfilclient() {
        //creamos un intent
        Intent intent = new Intent(MapsClientActivity.this,UpdateProfile.class);
        //iniciamos el intent
        startActivity(intent);
    }
    //metodo para el requestDriver
    private void requestDriver() {
        //aseguremos que el originLatLng no esta vacio tampoco el destinoLatLng
        if (mOriginLatLng != null && mDestinationLatLng != null) {
            //creamos un intent
            Intent intent = new Intent(MapsClientActivity.this, DetailRequestActivity.class);
            //esos intent va con esos datos por medio de un putExtra
            //enviar la latitude del origen
            intent.putExtra("origin_lat", mOriginLatLng.latitude);
            //enviar la longitude del origen
            intent.putExtra("origin_lng", mOriginLatLng.longitude);
            //enviar la latitude del destino
            intent.putExtra("destination_lat", mDestinationLatLng.latitude);
            //enviar la longitude del destino
            intent.putExtra("destination_lng", mDestinationLatLng.longitude);
            //enviar el origen
            intent.putExtra("origin", mOrigin);
            //enviar el destino
            intent.putExtra("destination", mDetination);
            //iniciamos el intent
            startActivity(intent);
        } else {
            //en caso contrario decimos al usuario de seleccionar el lugar
            Toast.makeText(this, "Debe seleccionar el lugar de recogida y el destino", Toast.LENGTH_SHORT).show();
        }

    }
    //metodo para limitar la busqueda solo aparecera segun su region o pais
    private void limitSearch() {
        //mcurrentLatLng me va a dar la ubicacion actual del usuario y agrego una limitamos de un rango de 5km
        LatLng northSide = SphericalUtil.computeOffset(mCurrentLatLng, 5000, 0);
        //mcurrentLatLng me va a dar la ubicacion actual del usuario y agrego una limitamos de un rango de 5km
        LatLng southSide = SphericalUtil.computeOffset(mCurrentLatLng, 5000, 180);
        //el auto completo del buscador debe ser de pais chile
        mAutocomplete.setCountry("CL");
        //lo pasamos el southside y el north sidde
        mAutocomplete.setLocationBias(RectangularBounds.newInstance(southSide, northSide));
        //el auto completo del buscador debe ser de pais chile en mi caso esta desactivado
        mAutocompleteDestino.setCountry("CL");
        //lo pasamos el southside y el north sidde
        mAutocompleteDestino.setLocationBias(RectangularBounds.newInstance(southSide, northSide));
    }
    //metodo para cambiar en el buscador la posicion cuando el usuario mueve el mapa
    private void onCameraMove() {
        mCameraListener = () -> {
            try {
                //llamamos el objeto de Geocoder eso recibe un contexto que es este this
                Geocoder geocoder = new Geocoder(MapsClientActivity.this);
                //pasamos añ OriginLatLong la position de la camara del mapa
                mOriginLatLng = mmap.getCameraPosition().target;
                //un arraylist de los datos de geocoder esos es requerido por el api
                List<Address> addressList = geocoder.getFromLocation(mOriginLatLng.latitude, mOriginLatLng.longitude, 1);
                //creamos un variable para la ciudad
                String city = addressList.get(0).getLocality();
                //creamos un variable para la pais
                String country = addressList.get(0).getCountryName();
                //creamos un variable para la direccion
                String address = addressList.get(0).getAddressLine(0);
                //pasamos al origen la dirrecion mas la ciudad
                mOrigin = address + " " + city;
                //pasamos al autocomplete de origen la dirrecion y la ciudad
                mAutocomplete.setText(address + " " + city);
                //aviso si desean poner el autocomplete en el destino aqui tb tienen que pasar el valor
                //al autocompletedestino en mi app no es necesario x eso esta desactivado
            } catch (Exception e) {
                Log.d("Error: ", "Mensaje error: " + e.getMessage());
            }
        };
    }
    //metodo para autocompletar del buscador de origen
    private void instanceAutocompleteOrigin() {
        //mautocomplete recibe el autocompletesupportfragment
        mAutocomplete = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placeautocompleteorigin);
        //en el mautocomplete seteamos el arraylist que teniamos arriba con los datos
        mAutocomplete.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        //en el hint ponemos ese texto
        mAutocomplete.setHint("Lugar de recogida");
        //aqui seteamos en tiempo real la ubicacion cada vez el usuario mueve el mapa ojo ese es un evento
        mAutocomplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                //pasamos el nombre al origin
                mOrigin = place.getName();
                //pasemos el LatLng al originLatLng
                mOriginLatLng = place.getLatLng();
                Log.d("PLACE", "Name: " + mOrigin);
                Log.d("PLACE", "Lat: " + mOriginLatLng.latitude);
                Log.d("PLACE", "Lng: " + mOriginLatLng.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });
    }
    //metodo para autocompletar el buscador del destino
    private void instanceAutocompleteDestination() {
        //mautocomplete recibe el autocompletesupportfragment
        mAutocompleteDestino = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placeautocompletedestino);
        //en el mautocomplete seteamos el arraylist que teniamos arriba con los datos
        mAutocompleteDestino.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        //en el hint ponemos ese texto
        mAutocompleteDestino.setHint("Destino");
        //aqui seteamos en tiempo real la ubicacion cada vez el usuario mueve el mapa ojo ese es un evento
        mAutocompleteDestino.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                //pasamos el nombre al destino
                mDetination = place.getName();
                //pasmos el LatLng al destinoLatLng
                mDestinationLatLng = place.getLatLng();
                Log.d("PLACE", "Name: " + mDetination);
                Log.d("PLACE", "Lat: " + mDestinationLatLng.latitude);
                Log.d("PLACE", "Lng: " + mDestinationLatLng.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });
    }
    //metodo para mostrar los drivers activos en la pantalla del cliente
    private void getActiveDrivers() {
        //entramos en la clase de GeofireProvider y ejecutamos el metodo de getActiveDrivers eso
        //recibe una latitud y una longitud lo tenemos en mCurrentLatLng tb recibe un radio en mi cado es de 10km
        //y agregamos el evento de addGeoQueryEventListener
        mGeofireProvider.getActiveDrivers(mCurrentLatLng, 10).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                // AÑADIREMOS LOS MARCADORES DE LOS CONDUCTORES QUE SE CONECTEN EN LA APLICACION
                for (Marker marker : mDriversMarkers) {
                    //aseguramos que no viene vacion el macador
                    if (marker.getTag() != null) {
                        //pasamos el key al marcador
                        if (marker.getTag().equals(key)) {
                            return;
                        }
                    }
                }
                //pasamos la latitud y la longitud al driverLatLng
                LatLng driverLatLng = new LatLng(location.latitude, location.longitude);
                //agregamos el auto en el lugar del punto de ubicacion y agregamos un titulo
                Marker marker = mmap.addMarker(new MarkerOptions().position(driverLatLng).title("Conductor disponible").icon(BitmapDescriptorFactory.fromResource(R.drawable.camion_grua)));
                //enviamos el key al tag
                marker.setTag(key);
                //agregamos el marcador del conductor osea el autito
                mDriversMarkers.add(marker);
            }
            @Override
            public void onKeyExited(String key) {
                for (Marker marker : mDriversMarkers) {
                    //aseguremos que no esta vacio
                    if (marker.getTag() != null) {
                        //pasamos el key para saber si existe
                        if (marker.getTag().equals(key)) {
                            //y aqui lo eliminamos
                            marker.remove();
                            // eliminamos el marcador del conductor
                            mDriversMarkers.remove(marker);
                            return;
                        }
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                // ACTUALIZAR LA POSICION DE CADA CONDUCTOR
                for (Marker marker : mDriversMarkers) {
                    //aseguramos que no esta vacio
                    if (marker.getTag() != null) {
                        //pasamos el key para saber si existe
                        if (marker.getTag().equals(key)) {
                            //enviamos la posicion al marcador recibe la latitude y la longitude
                            marker.setPosition(new LatLng(location.latitude, location.longitude));
                        }
                    }
                }
            }
            @Override
            public void onGeoQueryReady() {

            }
            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    //metodo on map ready
    public void onMapReady(GoogleMap googleMap) {
        // pasar al mapa el googlemap
        mmap = googleMap;
        //enviar el tipo de mapa
        mmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //mMap.getUiSettings().setZoomControlsEnabled(true);
        //ejecutamos el evento setOnCameraIdleListener recibe el  mCameraListener
        mmap.setOnCameraIdleListener(mCameraListener);
        //iniciamos el objeto  locationRequest
        mLocationRequest = new LocationRequest();
        //seteamos una interval 1000ms en mi caso para buscar
        mLocationRequest.setInterval(1000);
        //seteamos una interval 100ms en mi caso para buscar
        mLocationRequest.setFastestInterval(1000);
        //la prioridad es alta
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //aqui solicitaremos el conductor empezara en un rango de 1000 ms y cuando llega a 5 segundo
        //si no encontra a nadie te avisara y el evento quedara escuchando por si conecto un conductor
        mLocationRequest.setSmallestDisplacement(5);
        //iniciamos la location al encontrar driver
        startLocation();
    }

    @Override
    //metodo para saber si el usuario ha dado los permisos
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //igualamos el requestCode a nuestro variable arriba
        if (requestCode == LOCATION_REQUEST_CODE) {
            //aseguramos que de verdad nos a dado los permisiones
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //asemos un chequeo en e manifest para los permisos
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //aseguramos que el gpsdel celular de usuario esta activado
                    if (gpsActived()) {
                        //si esta activado enviamos el cliente en la ubicacion donde esta
                        mfuLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                       //activamos el puntito azul de la ubicacion de google maps
                        mmap.setMyLocationEnabled(true);
                    } else {
                        // en caso contrario mostramos esee mesaje
                        showAlertDialogNOGPS();
                    }
                } else {
                    //metodo check location permissions
                    checkLocationPermissions();
                }
            } else {
                //metodo check location permissions
                checkLocationPermissions();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //aseguremos que el requestCode es igual al estado de nuesta variable arriba
        // y tb que el gps esta activsdo
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived()) {
            //chequeamos en el manifest si de verdad tenemos la position
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //enviamos el usuario en la ubication donde esta en el mapa
            mfuLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            //activamos el puntido de ubicacion
            mmap.setMyLocationEnabled(true);
        }
        //en caso contrario mostramos el mensaje
        else if (requestCode == SETTINGS_REQUEST_CODE && !gpsActived()){
            //metdod para el mensaje cuando no tiene gps activado
            showAlertDialogNOGPS();
        }
    }
    //metodo que va a mostar un alert dialog que va a permitir al usuario ir en la configuracion para activiar su gps en caso si lo tiene apagado
    private void showAlertDialogNOGPS() {
        //cureamos un builder en este contexto this
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // el mensaje que va a ver el usuario
        builder.setMessage("Por favor activa tu ubicacion para continuar")
                //opcion que ponemos en el boton positivo
                .setPositiveButton("Configuraciones", (dialogInterface, i) -> startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE)).create().show();
    }
    //metodo para conocer si el usuario tiene o no el GPS activado
    private boolean gpsActived() {
        //iniciamos si esta activo en falso
        boolean isActive = false;
        //pasamos al locationManager el context
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //aseguramos que el gps de verdad esta habilitado
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // y pasamos nuestro esta acitivo en verdadero
            isActive = true;
        }
        //retornamos esta activo
        return isActive;
    }
    //metodo para iniciar el escuchador de nuesta ubicacion
    private void startLocation() {
        //asegurar que la version de sdk es mayo al version android Marshmallow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //aseguremos en el manifest si tenemos los permisos
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //saber si el gps esta activado
                if (gpsActived()) {
                    //enviamos la ubicacion que nos dio el gps
                    mfuLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    //activamos el punto de location
                    mmap.setMyLocationEnabled(true);
                }
                else {
                    //caso contrario mostramos mensaje
                    showAlertDialogNOGPS();
                }
            }
            else {
                //verificar los permisos
                checkLocationPermissions();
            }
        } else {
            //saber si el gps esta activado
            if (gpsActived()) {
                //enviamos la ubicacion que nos dio el gps
                mfuLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                //activamos el punto de location
                mmap.setMyLocationEnabled(true);
            }
            else {
                showAlertDialogNOGPS();
            }
        }
    }
    //metodo para ver si el usuario tiene el gps activado y solicitar permiso
    private void checkLocationPermissions() {
        //aseguremos en el manifest si tenemos los permisos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        //titulo del mensaje
                        .setTitle("Proporciona los permisos para continuar")
                        //cuerpo del mensaje
                        .setMessage("Esta aplicacion requiere de los permisos de ubicacion para poder utilizarse")
                        //accion boton positivo
                        .setPositiveButton("OK", (dialogInterface, i) -> ActivityCompat.requestPermissions(MapsClientActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE))
                        .create()
                        .show();
            }
            else {
                ActivityCompat.requestPermissions(MapsClientActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }
/*
esos es para la opcion de menu no lo estoy usando ahora asi que comentado por el momento
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.clientmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            Alertsignout();
        }
        if (item.getItemId() == R.id.action_update) {
            Intent intent = new Intent(MapsClientActivity.this, UpdateProfile.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.action_history) {
            Intent intent = new Intent(MapsClientActivity.this, HistoryBookingClient.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }*/
    //metodo para tomar los datos de cliente booking
    private void getClientBooking() {
        //entramos en la clase de ClientBookingProvider ejecutamos el metodo getClientBooking
        //eso recibe un id lo tenemos en mAuthProvider y luego ejecutamos el evento addListenerForSingleValueEvent
        mClientBookingProvider.getClientBooking(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //aseguramos que el dato existe
                if (dataSnapshot.exists()) {
                    //creamos un variable de idClient lo pasamos el id que viene el nodo de idClient de
                    //firebase mediante el dataSnapshot
                    String idClient = dataSnapshot.child("idClient").getValue().toString();
                    //aqui pasamos anuestra variable global midClient el idClient que viene en firebase
                    midClient = idClient;
                    //ejecutamos el metodo getClient eso recibe el idClient
                    getClient(idClient);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    //metodo para tomar info de los clientes
    private void getClient(String idClient) {
        //entramos en la clase de ClientProvider ejecutamos el metodo getClient
        //eso recibe un id lo tenemos en idClient y luego ejecutamos el evento addListenerForSingleValueEvent
        mClientProvider.getClient(idClient).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //aseguremos que el dato existe
                if (dataSnapshot.exists()) {
                    //creamos un variable name y lo pasamos lo que viene en firebase mediante dataSnapshot
                    String name = dataSnapshot.child("name").getValue().toString();
                    Log.d("VALOR","Valor name " + name);
                    //creamos un variable email y lo pasamos lo que viene en firebase mediante dataSnapshot
                    String email = dataSnapshot.child("email").getValue().toString();
                    //creamos un variable image lo iniciamos en vacio
                    String image = "";
                    //aseguremos que el nodo image tiene imagen
                    if (dataSnapshot.hasChild("image")){
                        //enviamos al variable image lo que viene en firebase mediante dataSnapshot
                        image = dataSnapshot.child("image").getValue().toString();
                        //lo pasamos a picaso el image para procesar y luego mostrarlo en la vista
                        Picasso.get().load(image).into(picture);
                    }
                    //enviamos el name en la vista
                    username.setText(name);
                    //enviamos el email en la vista
                    memail.setText(email);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //metodo para abrir el navegation drawer
    public static void openDrawer(DrawerLayout drawerLayout) {
        //Open drawer Layout
        drawerLayout.openDrawer(GravityCompat.START);

    }
    void ClickLogo(View view){
        //Close drawer
        closeDrawer(drawerLayout);
    }
    //metodo para cerrar el navegation drawer
    public static void closeDrawer(DrawerLayout drawerLayout) {
        //Close drawer layout
        //check condition
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            //when drawer is open
            //close drawer
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }
   /* void ClickHome(View view){
        //recreate activity
        recreate();
    }*/

    void Clicshare(View view){
        //redirect activity
        // redirectActivity(this);
    }
    void Clickwallet(View view){
        //redirect activity
        redirectActivity(this,WalletClient.class);
    }

    void Clicklogout(View view){
        //cloe app
        // logout();
    }

    public  void logout(View activity) {
        //inicialize alert dialog
        Alertsignout();

    }

    public static void redirectActivity(Activity activity,Class aClass) {
        //initialize intent
        Intent intent = new Intent(activity,aClass);
        //set flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        activity.startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //close drawer
        closeDrawer(drawerLayout);
    }

    //metodo para cerrar session con alert
    public void Alertsignout() {
        AlertDialog.Builder alertDialog2 = new
                AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog2.setTitle(R.string.confirm_signout);

        // Setting Dialog Message
        alertDialog2.setMessage(R.string.text_confirm);

        // Setting Positive "Yes" Btn
        alertDialog2.setPositiveButton(R.string.yes,
                (dialog , which) -> {
                    // Write your code here to execute after dialog
                    mAuthProvider.logout();
                    finish();
                    Intent i = new Intent(getApplicationContext() ,
                            MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                });

        // Setting Negative "NO" Btn
        alertDialog2.setNegativeButton(R.string.no ,
                (dialog , which) -> {
                    // Write your code here to execute after dialog
                    Toast.makeText(getApplicationContext() ,
                            R.string.noaction, Toast.LENGTH_SHORT)
                            .show();
                    dialog.cancel();
                    startActivity(new Intent(getApplicationContext(),MapsClientActivity.class));
                });

        // Showing Alert Dialog
        alertDialog2.show();


    }


    //metodo para generar token entre usuario
    void generateToken() {
        mTokenProvider.create(mAuthProvider.getId());
    }
}