package cl.tofcompany.sift.Controllers.Clients;

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
//implementemos el metodo OnMapReadyCallback
public class DetailRequestActivity extends AppCompatActivity implements OnMapReadyCallback {
    //definemos la variable de googlemap
    private GoogleMap mmap;
    //definemos la variable de supportfragment
    private SupportMapFragment mapFragment;
    //definemos una variable double para recibir el extraorigen latitude que vendra del intent extra
    private double mExtraOriginLat;
    //definemos una variable double para recibir el extraorigen longitude que vendra del intent extra
    private double mExtraOriginLng;
    //definemos una variable double para recibir el extradestino latitude que vendra del intent extra
    private double mExtraDestinationLat;
    //definemos una variable double para recibir el extradestino longitude que vendra del intent extra
    private double mExtraDestinationLng;
    //definemos una variable string para recibir en extraorigen que vendra del intent extra
    private String mExtraOrigin;
    //definemos una variable string para recibir en extradestino que vendra del intent extra
    private String mExtraDestination;
   //definemos la variable latlong para el origen latlong
    private LatLng mOriginLatLng;
    //definemos la variable latlong para el destino latlong
    private LatLng mDestinationLatLng;
    //llamamos la clase de googleApiProvider
    private GoogleApiProvider mGoogleApiProvider;
    //creamos una lista de objeto latlong
    private List<LatLng> mpolyllineList;
    //definemos el polineOptions
    private PolylineOptions mPolylineOptions;
    //definemos la variable del buton
    private Button mSolicitarahora;
    // definemos los texview
    private TextView mtextViewOrigin,mtextViewDestino,mtextViewTiempo,mtextViewprice;
    //llamado de la clase InfoProvider
    private InfoProvider mInfoProvider;
    //definemos el SharedPrefences
    private SharedPreferences mSharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_request);
        init();
    }
    //para optimizar el codigo creamos un metodo para esos variables
    private void init(){
        //inciando el variable de supportfragment con su id
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //lo pasamos el metodo de getMapsAsync
        mapFragment.getMapAsync(this);
        //llamamos la clase de nuestro toolbars
        MyToolbar.show(this,"Tus Datos", true);
        //recibiendo el origen latitude de viene del extra
        mExtraOriginLat = getIntent().getDoubleExtra("origin_lat", 0);
        //recibiendo el origen longitude de viene del extra
        mExtraOriginLng = getIntent().getDoubleExtra("origin_lng", 0);
        //recibiendo el destino latitude latitude de viene del extra
        mExtraDestinationLat = getIntent().getDoubleExtra("destination_lat", 0);
        //recibiendo el destino longitude latitude de viene del extra
        mExtraDestinationLng = getIntent().getDoubleExtra("destination_lng", 0);
        //recibiendo el origen  de viene del extra
        mExtraOrigin = getIntent().getStringExtra("origin");
        //recibiendo el destino  de viene del extra
        mExtraDestination = getIntent().getStringExtra("destination");
        //pasamos al origen latlong los extra origenlat y extra origin long
        mOriginLatLng = new LatLng(mExtraOriginLat, mExtraOriginLng);
        //pasamos al destino latlong los extra destinolat y extra destino long
        mDestinationLatLng = new LatLng(mExtraDestinationLat, mExtraDestinationLng);
        //llamamos sharedpreferences y entramos en el metdod getAplication y tomamos el metodo GetShredPrefences
        //eso es para tomar los datos de la tarjeta de credito que registro el cliente porque estan guardando en la memoria del celular
        mSharedPreferences = getApplication().getSharedPreferences("datosTarjetas",MODE_PRIVATE);
        //iniciamos la clase de googleApiprovider y lo decimos que hace referencia a esta actividad
        mGoogleApiProvider = new GoogleApiProvider(DetailRequestActivity.this);
        //inciamos el txt origen con su id
        mtextViewOrigin = findViewById(R.id.txtorigin);
        //inciamos el txt destino con su id
        mtextViewDestino = findViewById(R.id.txtdestination);
        //inciamos el txt tiempo con su id
        mtextViewTiempo = findViewById(R.id.textviewtime);
        //inciamos el txt precio con su id
        mtextViewprice = findViewById(R.id.textviewprice);
        //inciamos el btn solicitarahora con su id
        mSolicitarahora = findViewById(R.id.btnsolicitarahora);
        //enviamos en txt origen el extraOrigen que viene
        mtextViewOrigin.setText(mExtraOrigin);
        //enviamos en txt destino el extraDestino que viene
        mtextViewDestino.setText(mExtraDestination);
        //incimaos la clase de InfoProvider
        mInfoProvider = new InfoProvider();
        //creamos una accion de onclick sobre el btn
        mSolicitarahora.setOnClickListener(view -> goToRequestDriver());
    }
    //metodo para ir al solicitar y confirmar y buscar driver
    private void goToRequestDriver() {
        //creamos un intent
        Intent intent = new Intent(DetailRequestActivity.this, RequestDriverActivity.class);
        //este intent enviara el origenlat
        intent.putExtra("origin_lat", mOriginLatLng.latitude);
        //este intent enviara el origenlong
        intent.putExtra("origin_lng", mOriginLatLng.longitude);
        //este intent enviara el origen
        intent.putExtra("origin", mExtraOrigin);
        //este intent enviara el destino
        intent.putExtra("destination", mExtraDestination);
        //este intent enviara el destinolat
        intent.putExtra("destination_lat", mDestinationLatLng.latitude);
        //este intent enviara el destinolong
        intent.putExtra("destination_lng", mDestinationLatLng.longitude);
        //creamos un valirable que recibre el valos de sharedpreferences
        String typeTajeta = mSharedPreferences.getString("mnumerotarjeta","");
        //validamos si lo que llego es igual a no hay tarjeta
        if (typeTajeta.equals("No hay tarjetas")){
            //llamamos ese metodo
            Metodopago();
          //  Toast.makeText(this, "Debes Agregar un metodo de pago", Toast.LENGTH_SHORT).show();
        }else {
            Log.d("LLEGO","" +typeTajeta);
            //encaso contrario inciamos el intent
            startActivity(intent);
            //terminamos la actividad
            finish();
        }


    }
    //metodo mesaje de alert
    private void Metodopago() {
        //llamando el objeto Aletdialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //ponemos el titulo
        builder.setTitle("Error!");
        //ponemos el mensaje
        builder.setMessage("Debes Agregar un metodo de pago");
        //decimos la accion del boton negativo
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        //decimos la accion del boto positivo
        builder.setPositiveButton("Agregar Tarjeta", (dialog, which) -> {
            //creamos un intent
            Intent intent = new Intent(DetailRequestActivity.this,PagoActivity.class);
            //iniciamos el intent
            startActivity(intent);
        });
        //creamos el dialog
        AlertDialog dialog = builder.create();
        //mostramos el dialos
        dialog.show();

    }

    //metodo para trazar la ruta
    private void drawRoute() {
        //llamamos la clase de GoogleApiProvider y tomamos el metodo getDirections recibe un orienlatlon,
        //un destinolatlng y ejecutamos el metodo enqueue y yn new callback
        mGoogleApiProvider.getDirections(mOriginLatLng, mDestinationLatLng).enqueue(new Callback<String>() {
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
                    //enviar todas las opciones al polilynelist
                    mPolylineOptions.addAll(mpolyllineList);
                    //agregamos los polyline en el mapa
                    mmap.addPolyline(mPolylineOptions);
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
                    //enviamos en la vista el tiempo
                    mtextViewTiempo.setText(durationText + " " +distanceText);
                    //hacemos un spli la distancia en km
                    String [] distanceAndKm = distanceText.split(" ");
                    // convertimos el valos de la distancia en double
                    double distancevalue = Double.parseDouble(distanceAndKm[0]);
                    //hacemos un split de la duracion en min
                    String [] durationAndMins = durationText.split(" ");
                    //convertimos el valor en double
                    double durationvalue = Double.parseDouble(durationAndMins[0]);
                    //calculamos el precio segun el valor de la distancia y el valos de la duracion
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
        //llamamos la clase de InfoProvider y tomamos el metodo getInfo y agregamos un evento de firebase
        mInfoProvider.getInfo().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                //aseguramos que el daot existe en firebase
                if (snapshot.exists()){
                    //inciamos la clase info y lo pasamos lo que viene en firebase mediante el snapshot
                    //y ese valor lo pasamos a la clase de Info
                    Info info = snapshot.getValue(Info.class);
                    //validar info
                    assert info != null;
                    //variable del totaldistancia
                    double totalDistance = distancevalue * info.getKm();
                    //variable total duracion
                    double totalDuration = durationvalue * info.getMin();
                    //variable del total
                    double total = totalDistance + totalDuration;
                    //variavle del minimum por minutos
                    double mintotal = total - 500;
                    //variable del maximum por minutos
                    double maxtotal = total + 500;
                    //enviamos en la vista en minimum que puede pagar hasta el maximum
                    mtextViewprice.setText(mintotal + " - " + maxtotal + " CLP ");
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

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
        //para agregar el punto osea el marcador de origen
        mmap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Origen").icon(BitmapDescriptorFactory.fromResource(R.drawable.mappin)));
        //para agregar el punto osea el marcador de destino
        mmap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.mappinblue)));
        //eso nos da la animacion cuando confirama y hace una animacion de un zoom y luego se retrozede el ubicacion
        mmap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(mOriginLatLng)
                        .zoom(15f)
                        .build()
        ));
        //trazar la ruta
        drawRoute();
    }

}