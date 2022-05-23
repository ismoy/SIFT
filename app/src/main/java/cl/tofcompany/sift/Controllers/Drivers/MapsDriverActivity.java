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
//implementamos el OnMapReadyCallback
public class MapsDriverActivity extends AppCompatActivity implements OnMapReadyCallback {
    //variable del google map
    private GoogleMap mmap;
    //variable del supportfragment
    private SupportMapFragment mapFragment;
    //variable de la clase GeofireProvider
    private GeofireProvider mGeofireProvider;
    //variable de la clase LocationRequest
    private LocationRequest mLocationRequest;
    //variable de la clase FusedLocationProviderClient
    private FusedLocationProviderClient mfuLocationProviderClient;
    //variable definido para inicializar el codigo de la locacion en 1
    private final static int LOCATION_REQUEST_CODE = 1;
    //variable definido para inicializar el settings code en 2
    private final static int SETTINGS_REQUEST_CODE = 2;
    //variable del marcador del driver
    private Marker mMarker;
    //variable del btn conectar
    private Button mButtonConnect;
    //variable boleana par saber si se conecto o no lo iniciamos en false
    private boolean mIsConnect = false;
    //variable para tomar la latitude y la longitude en tiempo real
    private LatLng mCurrentLatLng;
    //variable de la clase AuthProvider
    private AuthProvider mAuthProvider;
    //variable de la clase TokenProvider
    private TokenProvider mTokenProvider;
    //variable Listener para escuchar un evento de firebase
    private ValueEventListener mListener;
    //variable de DrawerLayout
    private DrawerLayout drawerLayout;
    //variable de la clase DriverProvider
    DriverProvider mDriverProvider;
    //variable TextView username y email
    private TextView username, memail;
    //variable ImageView picture
    private ImageView picture;
    //variable para guardar el id del conductor
    private String midDriver ;
    //variable de la clase ClientBookingProvider
    private ClientBookingProvider mClientBookingProvider;
    //llamamos el objeto LocationCallback y lo pasamos el evento de LocationResult
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            //permite de tomar la locacion exacta del usuario
            for (Location location : locationResult.getLocations()) {
                //aseguremos que no esta vacio
                if (getApplicationContext() != null) {
                    //pasamos al mCurrentLatLng la latitude y la longitudes del usuario
                    mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    //aseguramos que el marcador no esta vacio
                    if (mMarker != null) {
                        //si no esta vacio eliminamos el marcador
                        mMarker.remove();
                    }
                    //agregamos el marcador en la posicion
                    mMarker = mmap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(), location.getLongitude())
                            )
                            //titulo del marcador
                                    .title("Tu posicion")
                            //icono del marcador
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.camion_grua))
                    );
                    // OBTENER LA LOCALIZACION DEL USUARIO EN TIEMPO REAL
                    mmap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(16f)
                                    .build()
                    ));
                    //metodo para actualizar la ubicacion
                    updateLocation();
                    Log.d("ENTRO", "ACTUALIZANDO POSICION");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_driver);
       init();
    }
    private void init(){
        //iniciamos la clase AuthProvider
        mAuthProvider = new AuthProvider();
        //iniciamos la clase TokenProvider
        mTokenProvider = new TokenProvider();
        //iniciamos la clase FusedLocationProvider
        mfuLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //iniciamos el supportfragment con su id
        mapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        //llamar el async eso recibe un callback que es this o este contexto
        mapFragment.getMapAsync(this);
        //iniciamos el drawerLayout con su id
        drawerLayout = findViewById(R.id.drawer_layout);
        //iniciamos el btn conectar con su id
        mButtonConnect = findViewById(R.id.btn_connect);
        //iniciamos la clase DriverProvider
        mDriverProvider = new DriverProvider();
        //iniciamos la clase ClientBookingProvider
        mClientBookingProvider = new ClientBookingProvider();
        //iniciamos el ImageView con su id
        picture = findViewById(R.id.picture);
        //dar click en la foto para modificar su perfil
        //agregamos un evento onclik en la foto
        picture.setOnClickListener(v ->
                //metodo para ir al activity de actualizar perfil
                gotoUpdateperfildriver());
        //iniciamos el name con su id
        username = findViewById(R.id.textviewnamedrwawerdriver);
        //agregamos un evento onclick sobre l boton conectar
        mButtonConnect.setOnClickListener(view -> {
            //preguntar si se esta conectado
            if (mIsConnect) {
                //si esta conectado llamamos el metodo desconectar para cambiar el buton
                disconnect();
            } else {
                //metodo para empezar la localizacion
                startLocation();
            }
        });
        //metodo para general el token
        generateToken();
        //metodo para saber si el conductor esta trabajando
        isDriverWorking();
        //iniciamos el ImageView del navegacion drawer con su id
        ImageView imageView = findViewById(R.id.imagemenu);
        //agregamos un evento onclick sobre el icono
        imageView.setOnClickListener(v -> {
            //open drawer
            openDrawer(drawerLayout);
        });
        //metodo para saber si el conductor ya tomado el viaje
        getDriverBooking();
        //eso es para la opcion de historias del viaje en el navegation drawer
        View layoutHistory = findViewById(R.id.layouthistory);
        layoutHistory.setOnClickListener(v ->
                //metodo para ir al actividad de historydriver
                gotoHistoryDriver());
        //eso es para el text vien donde esta la foto en la navegacion drawer
        TextView textviewmasinfodrawerdriver = findViewById(R.id.textviewmasinfodrawerdriver);
        textviewmasinfodrawerdriver.setOnClickListener(v -> {
            //metodo para ir en acercadeGriver activity
            gotoAcercaDeDriver();
        });
        //eso es para tasa de acepetacion en navegtion drawer
        View layouttasaaceptacion = findViewById(R.id.layouttasaaceptacion);
        layouttasaaceptacion.setOnClickListener(v ->
                //metodo para ir en la actividad de tasa aceptacion
                gotoTasaAceptacion());
        //eso es para tasa finalizacion en navegacion drawer
        View layouttasaviajefinalizado = findViewById(R.id.layouttasaviajefinalizado);
        layouttasaviajefinalizado.setOnClickListener(v -> {
            //crear un intent
            Intent intent = new Intent(MapsDriverActivity.this, TasaFinalizacionServicios.class);
            //iniciar el intent
            startActivity(intent);
        });
        //eso es para logout en navegtion drawer
        View layoutlogout = findViewById(R.id.layoutlogout);
       layoutlogout.setOnClickListener(v -> logout(v));
    }
    //metodo tasa aceptacion
    private void gotoTasaAceptacion() {
        //crear un intent
        Intent intent = new Intent(MapsDriverActivity.this, TasadeAceptacion.class);
        //iniciar el intent
        startActivity(intent);
    }
     //metodo AcercaDriver
    private void gotoAcercaDeDriver() {
        //crear intent
        Intent intent = new Intent(MapsDriverActivity.this, AcercadelDriver.class);
        //iniciar intent
        startActivity(intent);
    }
    //metodo HistoryDriver
    private void gotoHistoryDriver() {
        //crear intent
        Intent intent = new Intent(MapsDriverActivity.this, HistoryBookingDriver.class);
        //iniciar intent
        startActivity(intent);
    }
    //metodo UpdatePerfilDriver
    private void gotoUpdateperfildriver() {
        //crear intent
        Intent intent = new Intent(MapsDriverActivity.this,UpdateProfileDriver.class);
        //iniciar intent
        startActivity(intent);
    }
    //metodo para tomar el id  del Driver que se acepto el viaje
    private void getDriverBooking() {
        //entramos en la clase ClientBooking ejecutamos el metodo getClientBooking eso recibe
        //el id lo tenemos en mAuthProvider
        mClientBookingProvider.getClientBooking(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //asegurar que el datos existe
                if (dataSnapshot.exists()) {
                    //crear un variable para guardar el id proviene de firebase mediante dataSnapshot.child("idDriver")
                    String idDriver = dataSnapshot.child("idDriver").getValue().toString();
                   Log.d("MIIDDRIVER", "tieneeste " + idDriver);
                   //pasamos a mi idDriver el idDriver que se encuentro en firebase
                    midDriver = idDriver;
                    //metodo para tomar los datos de ese Driver eso recibe el IdDriver que viene de firebase
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
        //entramos en la clase DriverProvider ejecutamos el metodo getDriver eso recibe un id
        //lo tenemos en idDriver que recibe en el parametro
        mDriverProvider.getDriver(idDriver).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //asegurar que los datos existe
                if (dataSnapshot.exists()) {
                    //definir un variable para guardar el nombre proviene de firebase mediante dataSnapshot.child("name")
                    String name = dataSnapshot.child("name").getValue().toString();
                    Log.d("VALOR","Valor name " + name);
                    //definir un variable para guardar el email proviene de firebase mediante dataSnapshot.child("email")
                    String email = dataSnapshot.child("email").getValue().toString();
                    //definir el variable image y lo iniciamos en vacio
                    String image = "";
                    //preguntar si el nodo en firebase ya tiene un imagen
                    if (dataSnapshot.hasChild("image")){
                        //pasamos al variable image la imagen  proviene de firebase mediante dataSnapshot.child("image")
                        image = dataSnapshot.child("image").getValue().toString();
                        //lo enviamos al picasso para procesar el imagen y luego enviarlo en la vista
                        Picasso.get().load(image).into(picture);
                    }
                    //enviar el nombre en la vista
                    username.setText(name);
                    //enviar el email en la vista
                    memail.setText(email);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    //metodo para abrir la navegacion lateral
    public static void openDrawer(DrawerLayout drawerLayout) {
        //Open drawer Layout
        drawerLayout.openDrawer(GravityCompat.START);

    }
    /*void ClickLogo(View view){
        //Close drawer
        closeDrawer(drawerLayout);
    }*/
    //metodo para cerrar la navegacion lateral
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
        if (mListener!= null) {
            mGeofireProvider.isDriverWorking(mAuthProvider.getId()).removeEventListener(mListener);
            Log.e("IDDRIVERS " , "su id " +mAuthProvider.getId());
        }
    }
    //metodo para saber si el conductor esta trabajando y quitaremos su id en la lista de conductor disponible
    private void isDriverWorking() {
        //entramos en la clase GeofireProvider ejecutamos en metodo isDriverWorking eso recibe
        //un id lo tenemos en mAuthProvider
        mListener = mGeofireProvider.isDriverWorking(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //asegurar que los datos existe
                if (dataSnapshot.exists()) {
                    //desconectamos al conductor para no puede recibir mas solicitudes miestras tiene
                    //un viaje en curso
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
        //preguntar si la session existe y mCurrentLatLng no esta vacio
        if (mAuthProvider.existSession() && mCurrentLatLng != null) {
            //entramos en la clase GeofireProvider ejecutamos el metodo savelocation eso recibe
            //un id lo tenemos en mAuthProvider tb recibe la latitude y la longitude lo tenemos
            //en mCurrentLatLng
            mGeofireProvider.savelocation(mAuthProvider.getId(), mCurrentLatLng);
        }
    }

    @Override
    //metodo onMapReady recibe el googleMap
    public void onMapReady(GoogleMap googleMap) {
        // pasar al mapa el googlemap
        mmap = googleMap;
        //enviar el tipo de mapa
        mmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //activar la opcion para hacer zoom en pantalla
        mmap.getUiSettings().setZoomControlsEnabled(true);
        //chequear los permisos del manifest
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //desactivar el punto de la ubicacion
        mmap.setMyLocationEnabled(false);
        //iniciar el LocationRequest
        mLocationRequest = new LocationRequest();
        //seteamos una interval 1000ms en mi caso para buscar
        mLocationRequest.setInterval(1000);
        //seteamos una interval 1000ms en mi caso para buscar
        mLocationRequest.setFastestInterval(1000);
        //la prioridad es alta
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //iniciamos la location
        mLocationRequest.setSmallestDisplacement(5);
    }

    @Override
    //metodo para saber si el usuario ha dado los permisos
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //igualamos el requestCode a nuestro variable arriba
        if (requestCode == LOCATION_REQUEST_CODE) {
            //aseguramos que de verdad nos a dado los permisiones
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //hacemos un chequeo en el manifest para los permisos
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //aseguramos que el gps del celular de usuario esta activado
                    if (gpsActived()) {
                        //si esta activado enviamos el cliente en la ubicacion donde esta
                        mfuLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    } else {
                        // en caso contrario mostramos ese mensaje
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
        // y tb que el gps esta activado
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived()) {
            //chequeamos en el manifest si de verdad tenemos el permiso
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //enviamos el usuario en la ubication donde esta en el mapa
            mfuLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
        else {
            //en caso contrario mostramos el mensaje
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
        //iniciamos si esta activo en falso
        boolean isActive = false;
        //pasamos al locationManager el context
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //aseguramos que el gps de verdad esta habilitado
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // y pasamos nuestro variable  en verdadero
            isActive = true;
        }
        //retornamos esta activo
        return isActive;
    }
    //metodo para conectar y desconectar
    private void disconnect() {
        //saber si mfuLocationProviderClient no esta vacia
        if (mfuLocationProviderClient != null) {
            //ponemos ese texto en el button
            mButtonConnect.setText("Conectarse");
            //y ponemos isconnect en false
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
        //asegurar que la version de sdk es mayo al version android Marshmallow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //aseguremos en el manifest si tenemos los permisos
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //saber si el gps esta activado
                if (gpsActived()) {
                    //cambiamos el texto del botton en desconectar
                    mButtonConnect.setText("Desconectarse");
                    //y isconnect se pone en true
                    mIsConnect = true;
                    //y enviamos el conductor en la posicion que dio el gps
                    mfuLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                }
                else {
                    //mesaje alert
                    showAlertDialogNOGPS();
                }
            }
            else {
                //chequear permiso
                checkLocationPermissions();
            }
        } else {
            //saber si el gps esta activado
            if (gpsActived()) {
                //si esta activado llevar al usuario en la ubicacion que dio el gps
                mfuLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            }
            else {
                //alert gps no activado
                showAlertDialogNOGPS();
            }
        }
    }
    //metodo para ver si el usuario dio los permisos
    private void checkLocationPermissions() {
        //aseguremos en el manifest si tenemos los permisos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        //titulo del mensaje
                        .setTitle("Proporciona los permisos para continuar")
                        //cuerpo del mensaje
                        .setMessage("Esta aplicacion requiere de los permisos de ubicacion " +
                                "para poder utilizarse")
                        //accion boton positivo
                        .setPositiveButton("OK", (dialogInterface, i) ->
                                ActivityCompat.requestPermissions(MapsDriverActivity.this,
                                        new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                                        LOCATION_REQUEST_CODE))
                        .create()
                        .show();
            }
            else {
                ActivityCompat.requestPermissions(MapsDriverActivity.this,
                        new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_REQUEST_CODE);
            }
        }
    }

  /*  @Override
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
    }*/
//metodo para cerrar session
    /*void logout() {
        disconnect();
        mAuthProvider.logout();
        Intent intent = new Intent(MapsDriverActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }*/
  public  void logout(View activity) {
      //inicialize alert dialog
      Alertsignout();

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
                    disconnect();
                    mAuthProvider.logout();
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
                    startActivity(new Intent(getApplicationContext(), MapsDriverActivity.class));
                });

        // Showing Alert Dialog
        alertDialog2.show();


    }

    //metodo para generar toquen entre usuario
    void generateToken() {
        mTokenProvider.create(mAuthProvider.getId());
    }
}