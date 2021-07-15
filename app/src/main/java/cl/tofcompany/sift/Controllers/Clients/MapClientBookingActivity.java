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

public class MapClientBookingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mmap;
    private SupportMapFragment mapFragment;
    private LocationRequest mLocationRequest = new LocationRequest();
    private FusedLocationProviderClient mfuLocationProviderClient;
    private Marker mMarkerDriver;
    AuthProvider mAuthProvider;
    private GeofireProvider mGeofireProvider;
    private boolean mIsFirstTime = true;
    private PlacesClient mPlacesClient;
    private String mOrigin;
    private LatLng mOriginLatLng;
    private String mDetination;
    private LatLng mDestinationLatLng;
    private LatLng mDriverLatLng;
    private TokenProvider mTokenProvider;
    private TextView mTextViewDriverBooking;
    private TextView mTextViewEmailDriverBooking;
    private TextView mtextVieworigin;
    private TextView mTextViewdestination;
    private ClientBookingProvider mClientBookingProvider;
    private List<LatLng> mpolyllineList;
    private PolylineOptions mPolylineOptions;
    private GoogleApiProvider mGoogleApiProvider;
    private DriverProvider mDriverProvider;
    private ValueEventListener mListener;
    private String midDriver;
    private TextView mTextViewStatusBooking;
    private ValueEventListener mListenerStatus;
    private CircleImageView mimageviewdriverbooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_client_booking);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mAuthProvider = new AuthProvider();
        mfuLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mGeofireProvider = new GeofireProvider("drivers_working");
        mTokenProvider = new TokenProvider();
        mTextViewDriverBooking = findViewById(R.id.textviewdriverbooking);
        mTextViewEmailDriverBooking = findViewById(R.id.textviewemaildriverbooking);
        mtextVieworigin = findViewById(R.id.textvieworigindriverbooking);
        mTextViewdestination = findViewById(R.id.textviewdestinationdriverbooking);
        mClientBookingProvider = new ClientBookingProvider();
        mGoogleApiProvider = new GoogleApiProvider(MapClientBookingActivity.this);
        mDriverProvider = new DriverProvider();
        mTextViewStatusBooking = findViewById(R.id.textviewstatusbooking);
        mimageviewdriverbooking = findViewById(R.id.imageviewdriverbooking);
        mapFragment.getMapAsync(this);
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }
        getStatus();
        getClientBooking();
    }
    //metodo para tomar el estado en tiempo real y mostrarlo al cliente
    private void getStatus() {
        mListenerStatus = mClientBookingProvider.getStatus(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String status = dataSnapshot.getValue().toString();
                    if (status.equals("accept")) {
                        mTextViewStatusBooking.setText("Estado: Aceptado");
                    }
                    if (status.equals("start")) {
                        mTextViewStatusBooking.setText("Estado: Viaje Iniciado");
                        startBooking();
                    } else if (status.equals("finish")) {
                        mTextViewStatusBooking.setText("Estado: Viaje Finalizado");
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
        Intent intent = new Intent(MapClientBookingActivity.this, CalificationDriver.class);
        startActivity(intent);
        finish();
    }
//metdo para iniciar la busqueda
    private void startBooking() {
        mmap.clear();
        mmap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.mappinblue)));
        drawRoute(mDestinationLatLng);
    }

    @Override
    //metodo cuando se termine todas las actividades cerramos el escuchado de firebase en tiempo real
    protected void onDestroy() {
        super.onDestroy();
        if (mListener != null) {
            mGeofireProvider.getDriverLocation(midDriver).removeEventListener(mListener);
        }
        if (mListenerStatus != null) {
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
        mDriverProvider.getDriver(idDriver).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String email = dataSnapshot.child("email").getValue().toString();
                    String image = "";
                    if (dataSnapshot.hasChild("image")){
                        image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(mimageviewdriverbooking);
                    }
                    mTextViewDriverBooking.setText(name);
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
        mListener = mGeofireProvider.getDriverLocation(idDriver).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    double lat = Double.parseDouble(dataSnapshot.child("0").getValue().toString());
                    double lng = Double.parseDouble(dataSnapshot.child("1").getValue().toString());
                    mDriverLatLng = new LatLng(lat, lng);
                    if (mMarkerDriver != null) {
                        mMarkerDriver.remove();
                    }
                    mMarkerDriver = mmap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lng))
                            .title("Tu conductor")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.camion_grua)));
                    if (mIsFirstTime) {
                        mIsFirstTime = false;
                        mmap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(mDriverLatLng)
                                        .zoom(15f)
                                        .build()
                        ));
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
        mGoogleApiProvider.getDirections(mDriverLatLng, latLng).enqueue(new Callback<String>() {
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
                    mmap.addPolyline(mPolylineOptions);

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


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mmap = googleMap;
        mmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mmap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
          return;
        }
        mmap.setMyLocationEnabled(true);

    }
}

