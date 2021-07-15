package cl.tofcompany.sift.Controllers.Clients;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import cl.tofcompany.sift.Model.Info;
import cl.tofcompany.sift.Providers.GoogleApiProvider;
import cl.tofcompany.sift.Providers.InfoProvider;
import cl.tofcompany.sift.R;
import cl.tofcompany.sift.Utils.DecodePoints;
import cl.tofcompany.sift.includes.MyToolbar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailRequestActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mmap;
    private SupportMapFragment mapFragment;
    private double mExtraOriginLat;
    private double mExtraOriginLng;
    private double mExtraDestinationLat;
    private double mExtraDestinationLng;
    private String mExtraOrigin;
    private String mExtraDestination;
    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;
    private GoogleApiProvider mGoogleApiProvider;
    private List<LatLng> mpolyllineList;
    private PolylineOptions mPolylineOptions;
    private Button mSolicitarahora;
    private TextView mtextViewOrigin,mtextViewDestino,mtextViewTiempo,mtextViewprice;
    private InfoProvider mInfoProvider;
    private SharedPreferences mSharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_request);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        MyToolbar.show(this,"Tus Datos", true);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mExtraOriginLat = getIntent().getDoubleExtra("origin_lat", 0);
        mExtraOriginLng = getIntent().getDoubleExtra("origin_lng", 0);
        mExtraDestinationLat = getIntent().getDoubleExtra("destination_lat", 0);
        mExtraDestinationLng = getIntent().getDoubleExtra("destination_lng", 0);
        mExtraOrigin = getIntent().getStringExtra("origin");
        mExtraDestination = getIntent().getStringExtra("destination");

        mOriginLatLng = new LatLng(mExtraOriginLat, mExtraOriginLng);
        mDestinationLatLng = new LatLng(mExtraDestinationLat, mExtraDestinationLng);

        mGoogleApiProvider = new GoogleApiProvider(DetailRequestActivity.this);
        mSharedPreferences = getApplication().getSharedPreferences("datosTarjetas",MODE_PRIVATE);
        mtextViewOrigin = findViewById(R.id.txtorigin);
        mtextViewDestino = findViewById(R.id.txtdestination);
        mtextViewTiempo = findViewById(R.id.textviewtime);
        mtextViewprice = findViewById(R.id.textviewprice);
        mSolicitarahora = findViewById(R.id.btnsolicitarahora);
        mtextViewOrigin.setText(mExtraOrigin);
        mtextViewDestino.setText(mExtraDestination);
         mInfoProvider = new InfoProvider();
        mSolicitarahora.setOnClickListener(view -> goToRequestDriver());

    }
    //metodo para ir al solicitar y confirmar y buscar driver
    private void goToRequestDriver() {
        Intent intent = new Intent(DetailRequestActivity.this, RequestDriverActivity.class);
        intent.putExtra("origin_lat", mOriginLatLng.latitude);
        intent.putExtra("origin_lng", mOriginLatLng.longitude);
        intent.putExtra("origin", mExtraOrigin);
        intent.putExtra("destination", mExtraDestination);
        intent.putExtra("destination_lat", mDestinationLatLng.latitude);
        intent.putExtra("destination_lng", mDestinationLatLng.longitude);
        String typeTajeta = mSharedPreferences.getString("mnumerotarjeta","");
        if (typeTajeta.equals("No hay tarjetas")){
            Metodopago();
           // Toast.makeText(this, "Debes Agregar un metodo de pago", Toast.LENGTH_SHORT).show();
        }else {
            Log.d("LLEGO","" +typeTajeta);
            startActivity(intent);
            finish();
        }


    }
    //metodo mesaje de alert
    private void Metodopago() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error!");
        builder.setMessage("Debes Agregar un metodo de pago");
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton("Agregar Tarjeta", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(DetailRequestActivity.this,PagoActivity.class);
                startActivity(intent);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    //metodo para trazar la ruta
    private void drawRoute() {
        mGoogleApiProvider.getDirections(mOriginLatLng, mDestinationLatLng).enqueue(new Callback<String>() {
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
                    JSONArray legs =  route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanceText = distance.getString("text");
                    String durationText = duration.getString("text");
                    mtextViewTiempo.setText(durationText + " " +distanceText);

                    String [] distanceAndKm = distanceText.split(" ");
                    double distancevalue = Double.parseDouble(distanceAndKm[0]);
                    String [] durationAndMins = durationText.split(" ");
                    double durationvalue = Double.parseDouble(durationAndMins[0]);
                    calculatePrice(distancevalue,durationvalue);

                } catch(Exception e) {
                    Log.d("Error", "Error encontrado " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }
//metodo para calcular el precio segun la distancia y el minuto
    private void calculatePrice(double distancevalue, double durationvalue) {
        mInfoProvider.getInfo().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Info info = snapshot.getValue(Info.class);
                    assert info != null;
                    double totalDistance = distancevalue * info.getKm();
                    double totalDuration = durationvalue * info.getMin();
                    double total = totalDistance + totalDuration;
                    double mintotal = total - 20;
                    double maxtotal = total + 20;
                    mtextViewprice.setText(mintotal + " - " + maxtotal + " CLP ");
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mmap = googleMap;
        mmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mmap.getUiSettings().setZoomControlsEnabled(true);

        mmap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Origen").icon(BitmapDescriptorFactory.fromResource(R.drawable.mappin)));
        mmap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.mappinblue)));

        mmap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(mOriginLatLng)
                        .zoom(15f)
                        .build()
        ));

        drawRoute();
    }

}