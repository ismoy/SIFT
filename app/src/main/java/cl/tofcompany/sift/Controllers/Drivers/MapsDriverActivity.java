package cl.tofcompany.sift.Controllers.Drivers;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import cl.tofcompany.sift.Controllers.Logins.MainActivity;
import cl.tofcompany.sift.Providers.AuthProvider;
import cl.tofcompany.sift.Providers.ClientBookingProvider;
import cl.tofcompany.sift.Providers.DriverProvider;
import cl.tofcompany.sift.Providers.GeofireProvider;
import cl.tofcompany.sift.Providers.TokenProvider;
import cl.tofcompany.sift.R;

public class MapsDriverActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mmap;
    private SupportMapFragment mapFragment;
    private GeofireProvider mGeofireProvider;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mfuLocationProviderClient;
    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;
    private Marker mMarker;
    private Button mButtonConnect;
    private boolean mIsConnect = false;
    private LatLng mCurrentLatLng;
    private AuthProvider mAuthProvider;
    private TokenProvider mTokenProvider;
    private ValueEventListener mListener;
    private DrawerLayout drawerLayout;
    DriverProvider mDriverProvider;
    private TextView username, memail;
    private ImageView picture;
    private String midDriver ;
    private ClientBookingProvider mClientBookingProvider;


    LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {

                    mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if (mMarker != null) {
                        mMarker.remove();
                    }

                    mMarker = mmap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(), location.getLongitude())
                            )
                                    .title("Tu posicion")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.camion_grua))
                    );
                    // OBTENER LA LOCALIZACION DEL USUARIO EN TIEMPO REAL
                    mmap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(16f)
                                    .build()
                    ));

                    updateLocation();
                    Log.d("ENTRO", "ACTUALIZANDO PSOICIN");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_driver);
        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("active_drivers");
        mTokenProvider = new TokenProvider();
        mfuLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        drawerLayout = findViewById(R.id.drawer_layout);
        mButtonConnect = findViewById(R.id.btn_connect);
        mDriverProvider = new DriverProvider();
        mClientBookingProvider = new ClientBookingProvider();
        picture = findViewById(R.id.picture);
        //dar click en la foto para modificar su perfil
        picture.setOnClickListener(v -> gotoUpdateperfildriver());
        username = findViewById(R.id.textviewnamedrwawerdriver);
        mButtonConnect.setOnClickListener(view -> {
            if (mIsConnect) {
                disconnect();
            } else {
                startLocation();
            }
        });
        generateToken();
        isDriverWorking();
        ImageView imageView = findViewById(R.id.imagemenu);
        imageView.setOnClickListener(v -> {
            //open drawer
            openDrawer(drawerLayout);
        });

        getDriverBooking();
        //eso es para la opcion de historias deviaje en el navegation drawer
        View layoutHistory = findViewById(R.id.layouthistory);
        layoutHistory.setOnClickListener(v -> gotoHistoryDriver());
       //eso es para el text vien donde esta la foto en la navegacion drawer
        TextView textviewmasinfodrawerdriver = findViewById(R.id.textviewmasinfodrawerdriver);
        textviewmasinfodrawerdriver.setOnClickListener(v -> {
            gotoAcercaDeDriver();
        });
        //eso es para tasa de acepetacion en navegtion drawer
        View layouttasaaceptacion = findViewById(R.id.layouttasaaceptacion);
        layouttasaaceptacion.setOnClickListener(v -> gotoTasaAceptacion());
        //eso es para tasa finalizacion en navegacion drawer
        View layouttasaviajefinalizado = findViewById(R.id.layouttasaviajefinalizado);
        layouttasaviajefinalizado.setOnClickListener(v -> {
            Intent intent = new Intent(MapsDriverActivity.this, TasaFinalizacionServicios.class);
            startActivity(intent);
        });
    }

    private void gotoTasaAceptacion() {
        Intent intent = new Intent(MapsDriverActivity.this, TasadeAceptacion.class);
        startActivity(intent);
    }

    private void gotoAcercaDeDriver() {
        Intent intent = new Intent(MapsDriverActivity.this, AcercadelDriver.class);
        startActivity(intent);
    }

    private void gotoHistoryDriver() {
        Intent intent = new Intent(MapsDriverActivity.this, HistoryBookingDriver.class);
        startActivity(intent);
    }

    private void gotoUpdateperfildriver() {
        Intent intent = new Intent(MapsDriverActivity.this,UpdateProfileDriver.class);
        startActivity(intent);
    }

    private void getDriverBooking() {
        mClientBookingProvider.getClientBooking(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String idDriver = dataSnapshot.child("idDriver").getValue().toString();
                   Log.d("MIIDDRIVER", "tieneeste " + idDriver);
                    System.out.println("MIIDDRIVER " +idDriver);
                    midDriver = idDriver;
                    getDriver(idDriver);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    //metodo para tomar info de los drivers
    private void getDriver(String idDriver) {
        mDriverProvider.getDriver(idDriver).addListenerForSingleValueEvent(new ValueEventListener() {
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
    @Override
    protected void onPause() {
        super.onPause();
        //close drawer
        closeDrawer(drawerLayout);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListener != null) {
            mGeofireProvider.isDriverWorking(mAuthProvider.getId()).removeEventListener(mListener);
        }
    }
    //metodo para saber si el conductor esta trabajando y quitaremos su id en la lista de conductor disponible
    private void isDriverWorking() {
        mListener = mGeofireProvider.isDriverWorking(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    disconnect();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //metodo para guardar la localizacion cada vez se mueve el usuario
    private void updateLocation() {
        if (mAuthProvider.existSession() && mCurrentLatLng != null) {
            mGeofireProvider.savelocation(mAuthProvider.getId(), mCurrentLatLng);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mmap = googleMap;
        mmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mmap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mmap.setMyLocationEnabled(false);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (gpsActived()) {
                        mfuLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
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
        }
        else {
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
    //metodo para conectar y desconectar
    private void disconnect() {
        if (mfuLocationProviderClient != null) {
            mButtonConnect.setText("Conectarse");
            mIsConnect = false;
            mfuLocationProviderClient.removeLocationUpdates(mLocationCallback);
            if (mAuthProvider.existSession()) {
                mGeofireProvider.removelocation(mAuthProvider.getId());
            }
        }
        else {
            Toast.makeText(this, "No te puedes desconectar", Toast.LENGTH_SHORT).show();
        }
    }
    //metodo para iniciar el escuchador de nuesta ubicacion
    private void startLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {
                    mButtonConnect.setText("Desconectarse");
                    mIsConnect = true;
                    mfuLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
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
            }
            else {
                showAlertDialogNOGPS();
            }
        }
    }
    //metodo por si el usuario no nos da el permiso requerido
    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Esta aplicacion requiere de los permisos de ubicacion para poder utilizarse")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapsDriverActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            }
            else {
                ActivityCompat.requestPermissions(MapsDriverActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drivermenu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
        }
        if (item.getItemId() == R.id.action_update) {
            Intent intent = new Intent(MapsDriverActivity.this, UpdateProfileDriver.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.action_history) {
            Intent intent = new Intent(MapsDriverActivity.this, HistoryBookingDriver.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
//metodo para cerrar session
    void logout() {
        disconnect();
        mAuthProvider.logout();
        Intent intent = new Intent(MapsDriverActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    //metodo para generar toquen entre usuario
    void generateToken() {
        mTokenProvider.create(mAuthProvider.getId());
    }
}