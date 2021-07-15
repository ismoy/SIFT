package cl.tofcompany.sift.Controllers.Drivers;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
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

public class MapDriverBookingActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private AuthProvider mAuthProvider;
    private GeofireProvider mGeofireProvider;
    private TokenProvider mTokenProvider;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;
    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;
    private Marker mMarker;
    private LatLng mCurrentLatLng;
    private TextView mTextViewClientBooking;
    private TextView mTextViewEmailClientBooking;
    private String mExtraClientId;
    private ClientProvider mcClientProvider;
    private ClientBookingProvider mClientBookingProvider;
    private List<LatLng> mpolyllineList;
    private PolylineOptions mPolylineOptions;
    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;
    private GoogleApiProvider mGoogleApiProvider;
    private TextView mtextVieworigin;
    private TextView mTextViewdestination;
    private boolean mIsFirstTime = true;
    private Button mButtonstarbooking;
    private Button mButtonendbooking;
    private boolean mIsCloseToClient = false;
    private boolean mRideStart = false;
    private NotificationProvider mNotificationProvider;
    private ImageView mimageviewclientbooking;
    private InfoProvider mInfoProvider;
    private TextView mTextViewTime;
    private TextView mTextViewkm;
    private Info mInfo;
    double mDistanceMeters  = 1;
    int mMinutes = 0;
    int mSeconds = 0;
    boolean mSecondsIsOver = false;
    Handler mHandler = new Handler();
    Location mpreviewLocation = new Location("");

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mSeconds++;
            if (!mSecondsIsOver){
                mTextViewTime.setText(mSeconds + " Seg ");
            }else {
                mTextViewTime.setText(mMinutes + "Min " + mSeconds);
            }
            if (mSeconds == 59){
                mSeconds = 0;
                mSecondsIsOver = true;
                mMinutes++;
            }
          mHandler.postDelayed(runnable,1000);
        }
    };
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {

                    mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if (mMarker != null) {
                        mMarker.remove();
                    }
                    if (mRideStart){
                        mDistanceMeters = mDistanceMeters + mpreviewLocation.distanceTo(location);
                        Log.d("ENTRO", "Distancia recorrido: " +mDistanceMeters);
                        mTextViewkm.setText(String.format("%.1f", mDistanceMeters) + " KM ");
                    }
                    mpreviewLocation = location;
                    mMarker = mMap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(), location.getLongitude())
                            )
                                    .title("Tu posicion")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.camion_grua))
                    );
                    // OBTENER LA LOCALIZACION DEL USUARIO EN TIEMPO REAL
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(16f)
                                    .build()
                    ));

                    updateLocation();

                    if (mIsFirstTime) {
                        mIsFirstTime = false;
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

        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("drivers_working");
        mTokenProvider = new TokenProvider();
        mcClientProvider = new ClientProvider();
        mClientBookingProvider = new ClientBookingProvider();
        mNotificationProvider = new NotificationProvider();


        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mTextViewClientBooking = findViewById(R.id.textviewclientbooking);
        mTextViewEmailClientBooking = findViewById(R.id.textviewemailclientbooking);
        mtextVieworigin = findViewById(R.id.textvieworiginclientbooking);
        mTextViewdestination = findViewById(R.id.textviewdestinationclientbooking);
        mButtonstarbooking = findViewById(R.id.btnstartbooking);
        mButtonendbooking = findViewById(R.id.btnendtbooking);
        mimageviewclientbooking = findViewById(R.id.imageviewclientbooking);
        mExtraClientId = getIntent().getStringExtra("idClient");
        mGoogleApiProvider = new GoogleApiProvider(MapDriverBookingActivity.this);
        mTextViewTime = findViewById(R.id.textviewtime);
        mInfoProvider = new InfoProvider();
        mTextViewkm = findViewById(R.id.textviewkm);
        getInfo();
        getClient();

        mButtonstarbooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsCloseToClient) {
                    startBooking();
                } else {
                    Toast.makeText(MapDriverBookingActivity.this, "Debes estar mas cerca a la posicion de recogida", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mButtonendbooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishBooking();
            }
        });
    }
//metodo para calcular el ride
    public void calculateRide(){
        if (mMinutes == 0){
            mMinutes = 1;
        }
        double priceMin = mMinutes * mInfo.getMin();
        double priceKm = (mDistanceMeters / 1000) * mInfo.getKm();
        Log.d("Valores","Min total: " + mMinutes);
        Log.d("Valores","Km total: " + (mDistanceMeters / 1000));
        double total = priceMin + priceKm;
        mClientBookingProvider.updatePrice(mExtraClientId,total).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                mClientBookingProvider.updateStatus(mExtraClientId, "finish");
                Intent intent = new Intent(MapDriverBookingActivity.this, CalificationClient.class);
                intent.putExtra("idClient", mExtraClientId);
                intent.putExtra("price", total);
                startActivity(intent);
                finish();
            }
        });

    }
    //metodo para mostrar el info de minutos y segundos recolrido
    private void getInfo() {
        mInfoProvider.getInfo().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
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
        mClientBookingProvider.updateIdHistoryBooking(mExtraClientId).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                sendNotification("Viaje finalizado");
                if (mFusedLocation != null) {
                    mFusedLocation.removeLocationUpdates(mLocationCallback);
                }
                mGeofireProvider.removelocation(mAuthProvider.getId());
                if (mHandler != null) {
                    mHandler.removeCallbacks(runnable);
                }
                calculateRide();
            }
        });

    }
    //metodo para iniciar un viaje
    private void startBooking() {
        mClientBookingProvider.updateStatus(mExtraClientId, "start");
        mButtonstarbooking.setVisibility(View.GONE);
        mButtonendbooking.setVisibility(View.VISIBLE);
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.mappinblue)));
        drawRoute(mDestinationLatLng);
        sendNotification("Viaje iniciado");
        mRideStart = true;
        mHandler.postDelayed(runnable,1000);
    }
    //metodo para obtener cuantas distancias hay entre el conductor y el cliente
    private double getDistanceBetween(LatLng clientLatLng, LatLng driverLatLng) {
        double distance = 0;
        Location clientLocation = new Location("");
        Location driverLocation = new Location("");
        clientLocation.setLatitude(clientLatLng.latitude);
        clientLocation.setLongitude(clientLatLng.longitude);
        driverLocation.setLatitude(driverLatLng.latitude);
        driverLocation.setLongitude(driverLatLng.longitude);
        distance = clientLocation.distanceTo(driverLocation);
        return distance;
    }
    //metodo para obtener informacion de la solicitud del viaje
    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String destination = dataSnapshot.child("destination").getValue().toString();
                    String origin = dataSnapshot.child("origin").getValue().toString();
                    double destinatioLat = Double.parseDouble(dataSnapshot.child("destinationLat").getValue().toString());
                    double destinatioLng = Double.parseDouble(dataSnapshot.child("destinationLng").getValue().toString());

                    double originLat = Double.parseDouble(dataSnapshot.child("originLat").getValue().toString());
                    double originLng = Double.parseDouble(dataSnapshot.child("originLng").getValue().toString());
                    mOriginLatLng = new LatLng(originLat, originLng);
                    mDestinationLatLng = new LatLng(destinatioLat, destinatioLng);
                    mtextVieworigin.setText("recoger en: " + origin);
                    mTextViewdestination.setText("destino: " + destination);
                    mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.mappin)));
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
        mGoogleApiProvider.getDirections(mCurrentLatLng, latLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {

                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");
                    mpolyllineList = DecodePoints.decodePoly(points);
                    mPolylineOptions = new PolylineOptions();
                    mPolylineOptions.color(Color.DKGRAY);
                    mPolylineOptions.width(13f);
                    mPolylineOptions.startCap(new SquareCap());
                    mPolylineOptions.jointType(JointType.ROUND);
                    mPolylineOptions.addAll(mpolyllineList);
                    mMap.addPolyline(mPolylineOptions);

                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanceText = distance.getString("text");
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
    private void getClient() {
        mcClientProvider.getClient(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                   String email = dataSnapshot.child("email").getValue().toString();
                    String name = dataSnapshot.child("name").getValue().toString();
                    String image = "";
                    if (dataSnapshot.hasChild("image")) {
                        image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(mimageviewclientbooking);
                    }
                    mTextViewClientBooking.setText(name);
                    //metodo para obtener las informaciones del cliente
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
        if (mAuthProvider.existSession() && mCurrentLatLng != null) {
            mGeofireProvider.savelocation(mAuthProvider.getId(), mCurrentLatLng);
            if (!mIsCloseToClient) {
                if (mOriginLatLng != null && mCurrentLatLng != null) {
                    double distance = getDistanceBetween(mOriginLatLng, mCurrentLatLng); // METROS
                    if (distance <= 200) {
                        //mButtonStartBooking.setEnabled(true);
                        mIsCloseToClient = true;
                        Toast.makeText(this, "Estas cerca a la posicion de recogida", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
         return;
        }
        mMap.setMyLocationEnabled(false);

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
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
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
            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
        else {
            showAlertDialogNOGPS();
        }
    }

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

    private boolean gpsActived() {
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true;
        }
        return isActive;
    }

    private void disconnect() {

        if (mFusedLocation != null) {
            mFusedLocation.removeLocationUpdates(mLocationCallback);
            if (mAuthProvider.existSession()) {
                mGeofireProvider.removelocation(mAuthProvider.getId());
            }
        }
        else {
            Toast.makeText(this, "No te puedes desconectar", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {

                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
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
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            }
            else {
                showAlertDialogNOGPS();
            }
        }
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Esta aplicacion requiere de los permisos de ubicacion para poder utilizarse")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapDriverBookingActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            }
            else {
                ActivityCompat.requestPermissions(MapDriverBookingActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    private void sendNotification(final String status) {
        mTokenProvider.getToken(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String token = dataSnapshot.child("token").getValue().toString();
                    Map<String, String> map = new HashMap<>();
                    map.put("title", "ESTADO DE TU VIAJE");
                    map.put("body",
                            "Tu estado del viaje es: " + status
                    );
                    FCMBody fcmBody = new FCMBody(token, "high", "4500s", map);
                    mNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body() != null) {
                                if (response.body().getSuccess() != 1) {
                                    Toast.makeText(MapDriverBookingActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
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
                    Toast.makeText(MapDriverBookingActivity.this, "No se pudo enviar la notificacion porque el conductor no tiene un token de sesion", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



}