package cl.tofcompany.sift.Controllers.Clients;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.Menu;
import android.view.MenuItem;
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

import cl.tofcompany.sift.Controllers.MainActivity;
import cl.tofcompany.sift.Providers.AuthProvider;
import cl.tofcompany.sift.Providers.ClientBookingProvider;
import cl.tofcompany.sift.Providers.ClientProvider;
import cl.tofcompany.sift.Providers.GeofireProvider;
import cl.tofcompany.sift.Providers.TokenProvider;
import cl.tofcompany.sift.R;

public class MapsClientActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mmap;
    private SupportMapFragment mapFragment;
    private LocationRequest mLocationRequest = new LocationRequest();
    private FusedLocationProviderClient mfuLocationProviderClient;
    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;
    private Marker mMarker;
    AuthProvider mAuthProvider;
    private GeofireProvider mGeofireProvider;
    private LatLng mCurrentLatLng;
    private List<Marker> mDriversMarkers = new ArrayList<>();
    private boolean mIsFirstTime = true;
    private AutocompleteSupportFragment mAutocomplete;
    private AutocompleteSupportFragment mAutocompleteDestino;
    private PlacesClient mPlacesClient;
    private String mOrigin;
    private LatLng mOriginLatLng;
    private String mDetination;
    private LatLng mDestinationLatLng;
    private Button mButtonRequestDriver;
    private TokenProvider mTokenProvider;
    private GoogleMap.OnCameraIdleListener mCameraListener;
    private DrawerLayout drawerLayout;
    private TextView username, memail;
    private ImageView picture;
    private ClientProvider mClientProvider;
    private ClientBookingProvider mClientBookingProvider;
    private String midClient;
    private View layout;
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {

                    mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    // OBTENER LA LOCALIZACION DEL USUARIO EN TIEMPO REAL
                    mmap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(15f)
                                    .build()
                    ));

                    if (mIsFirstTime) {
                        mIsFirstTime = false;
                        getActiveDrivers();
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
        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("active_drivers");
        mTokenProvider = new TokenProvider();
        mfuLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mButtonRequestDriver = findViewById(R.id.btnRequestDriver);
        mClientBookingProvider = new ClientBookingProvider();
        drawerLayout = findViewById(R.id.drawer_layout);
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
        picture = findViewById(R.id.picture);
        //dar click en la foto para modificar su perfil
        picture.setOnClickListener(v -> gotoUpdateperfilclient());
        username = findViewById(R.id.textviewnamedrwawerclient);
        memail = findViewById(R.id.textviewcorreodrawerclient);
        getClientBooking();
        mClientProvider = new ClientProvider();
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }
        mPlacesClient = Places.createClient(this);
        instanceAutocompleteOrigin();
        instanceAutocompleteDestination();
        onCameraMove();

        mButtonRequestDriver.setOnClickListener(view -> MapsClientActivity.this.requestDriver());
        TextView logoutnavegationdrawer = findViewById(R.id.logoutnavegationdrawer);
        logoutnavegationdrawer.setOnClickListener(v -> logout(v));
        generateToken();

    }

    private void gotoConnectWithSIFT() {
        Intent intent = new Intent(MapsClientActivity.this, ConnectWithSIFT.class);
        startActivity(intent);
    }

    //metodo para compartir mi app
    private void shareAndWin() {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String shareTitle = ("https://play.google/store/apps/details?id=c0m.ready\n" +
                    "Free download");
            String shareBody = ("");
            intent.putExtra(Intent.EXTRA_TEXT , shareTitle);
            intent.putExtra(Intent.EXTRA_SUBJECT , shareBody);
            startActivity(Intent.createChooser(intent , "Share"));

    }

    private void gotoConfiguration() {
        Intent intent = new Intent(MapsClientActivity.this, Configuration.class);
        startActivity(intent);
    }

    private void gotoSecurityCenter() {
        Intent intent = new Intent(MapsClientActivity.this, SecurityCenter.class);
        startActivity(intent);
    }

    private void gotoHistoryClient() {
        Intent intent = new Intent(MapsClientActivity.this,HistoryBookingClient.class);
        startActivity(intent);
    }

    //metodo cuando da click soble la foto perfil para ir al update
    private void gotoUpdateperfilclient() {
        Intent intent = new Intent(MapsClientActivity.this,UpdateProfile.class);
        startActivity(intent);
    }
    private void requestDriver() {
        if (mOriginLatLng != null && mDestinationLatLng != null) {
            Intent intent = new Intent(MapsClientActivity.this, DetailRequestActivity.class);
            intent.putExtra("origin_lat", mOriginLatLng.latitude);
            intent.putExtra("origin_lng", mOriginLatLng.longitude);
            intent.putExtra("destination_lat", mDestinationLatLng.latitude);
            intent.putExtra("destination_lng", mDestinationLatLng.longitude);
            intent.putExtra("origin", mOrigin);
            intent.putExtra("destination", mDetination);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Debe seleccionar el lugar de recogida y el destino", Toast.LENGTH_SHORT).show();
        }

    }
    //metodo para limitar la busqueda solo aparecera segun su region o pais
    private void limitSearch() {
        LatLng northSide = SphericalUtil.computeOffset(mCurrentLatLng, 5000, 0);
        LatLng southSide = SphericalUtil.computeOffset(mCurrentLatLng, 5000, 180);
        mAutocomplete.setCountry("CL");
        mAutocomplete.setLocationBias(RectangularBounds.newInstance(southSide, northSide));
        mAutocompleteDestino.setCountry("CL");
        mAutocompleteDestino.setLocationBias(RectangularBounds.newInstance(southSide, northSide));
    }
    //metodo para cambiar en el buscador la posicion cuando el usuario mueve el mapa
    private void onCameraMove() {
        mCameraListener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                try {
                    Geocoder geocoder = new Geocoder(MapsClientActivity.this);
                    mOriginLatLng = mmap.getCameraPosition().target;
                    List<Address> addressList = geocoder.getFromLocation(mOriginLatLng.latitude, mOriginLatLng.longitude, 1);
                    String city = addressList.get(0).getLocality();
                    String country = addressList.get(0).getCountryName();
                    String address = addressList.get(0).getAddressLine(0);
                    mOrigin = address + " " + city;
                    mAutocomplete.setText(address + " " + city);
                } catch (Exception e) {
                    Log.d("Error: ", "Mensaje error: " + e.getMessage());
                }
            }
        };
    }
    //metodo para autocompletar el buscador
    private void instanceAutocompleteOrigin() {
        mAutocomplete = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placeautocompleteorigin);
        mAutocomplete.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        mAutocomplete.setHint("Lugar de recogida");
        mAutocomplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mOrigin = place.getName();
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
    //metodo para autocompletar el buscador
    private void instanceAutocompleteDestination() {
        mAutocompleteDestino = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placeautocompletedestino);
        mAutocompleteDestino.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        mAutocompleteDestino.setHint("Destino");
        mAutocompleteDestino.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mDetination = place.getName();
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
        mGeofireProvider.getActiveDrivers(mCurrentLatLng, 10).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                // AÑADIREMOS LOS MARCADORES DE LOS CONDUCTORES QUE SE CONECTEN EN LA APLICACION

                for (Marker marker : mDriversMarkers) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            return;
                        }
                    }
                }

                LatLng driverLatLng = new LatLng(location.latitude, location.longitude);
                Marker marker = mmap.addMarker(new MarkerOptions().position(driverLatLng).title("Conductor disponible").icon(BitmapDescriptorFactory.fromResource(R.drawable.camion_grua)));
                marker.setTag(key);
                mDriversMarkers.add(marker);
            }

            @Override
            public void onKeyExited(String key) {
                for (Marker marker : mDriversMarkers) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            marker.remove();
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
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
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
    public void onMapReady(GoogleMap googleMap) {
        mmap = googleMap;
        mmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //mMap.getUiSettings().setZoomControlsEnabled(true);
        mmap.setOnCameraIdleListener(mCameraListener);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);

        startLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (gpsActived()) {
                        mfuLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mmap.setMyLocationEnabled(true);
                    } else {
                        showAlertDialogNOGPS();
                    }
                } else {
                    checkLocationPermissions();
                }
            } else {
                checkLocationPermissions();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mfuLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mmap.setMyLocationEnabled(true);
        }
        else if (requestCode == SETTINGS_REQUEST_CODE && !gpsActived()){
            showAlertDialogNOGPS();
        }
    }
    //metodo que va a mostar un alert dialog que va a permitir al usuario ir en la configuracion para activiar su gps en caso si lo tiene apagado
    private void showAlertDialogNOGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Por favor activa tu ubicacion para continuar")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE);
                    }
                }).create().show();
    }
    //metodo para conocer si el usuario tiene o no el GPS activado
    private boolean gpsActived() {
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true;
        }
        return isActive;
    }
    //metodo para iniciar el escuchador de nuesta ubicacion
    private void startLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {
                    mfuLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    mmap.setMyLocationEnabled(true);
                }
                else {
                    showAlertDialogNOGPS();
                }
            }
            else {
                checkLocationPermissions();
            }
        } else {
            if (gpsActived()) {
                mfuLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mmap.setMyLocationEnabled(true);
            }
            else {
                showAlertDialogNOGPS();
            }
        }
    }
    //metodo para ver si el usuario tiene el gps activado y solicitar permiso
    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Esta aplicacion requiere de los permisos de ubicacion para poder utilizarse")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapsClientActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            }
            else {
                ActivityCompat.requestPermissions(MapsClientActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

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
    }
    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String idClient = dataSnapshot.child("idClient").getValue().toString();
                    midClient = idClient;
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
        mClientProvider.getClient(idClient).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    Log.d("VALOR","Valor name " + name);
                    String email = dataSnapshot.child("email").getValue().toString();
                    String image = "";
                    if (dataSnapshot.hasChild("image")){
                        image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(picture);
                    }
                    username.setText(name);
                    memail.setText(email);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void openDrawer(DrawerLayout drawerLayout) {
        //Open drawer Layout
        drawerLayout.openDrawer(GravityCompat.START);

    }
    void ClickLogo(View view){
        //Close drawer
        closeDrawer(drawerLayout);
    }

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