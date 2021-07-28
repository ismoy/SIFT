package cl.tofcompany.sift.Controllers.Clients;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import cl.tofcompany.sift.R;
import cl.tofcompany.sift.includes.MyToolbar;

public class WalletClient extends AppCompatActivity {
    //variable TextView numerotarjeta
    private TextView mmerodetarjeta;
    //variable TextView fecha vencimiento
    private TextView mfechavencimiento;
    //variable TextView cvv
    private TextView mcodigoseguridad;
    //variable TextView nombretarjeta
    private TextView mnombretarjeta;
    //variable View layoutpagocongoogle
    private View layoutpagocongoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_client);
        init();
    }
    //aqui esta las inicializaciones de los variables y layout
   private void init(){
       //lammando a mi toolbar
       MyToolbar.show(this, "", true);
       //iniciando el view de layoutefectivo con su id
       View layoutefectivo = findViewById(R.id.layoutefectivo);
       //agregamos un onclick sobre el layout
       layoutefectivo.setOnClickListener(v -> {
           //creamos un intent
           Intent intent = new Intent(WalletClient.this, PagoEnEfectivoClient.class);
           //iniciamos el intent
           startActivity(intent);
       });
       //iniciando el view de layoutvisaclient con su id
       View layoutvisaclient = findViewById(R.id.layoutvisaclient);
       //agregamos un onclik sobre el layout
       layoutvisaclient.setOnClickListener(v -> {
           //recibir datos de la tarjeta mediante SharedPreferences
           SharedPreferences preferencias = getSharedPreferences("datosTarjetas", Context.MODE_PRIVATE);
           //invocamos el editos de SharedPreferences
           SharedPreferences.Editor editor = preferencias.edit();
           //enviamos el numero de tarjeta recibids
           editor.putString("mnumerotarjeta", mmerodetarjeta.getText().toString());
           //enviamos el nombre de tarjeta recibids
           editor.putString("mnombretarjeta", mnombretarjeta.getText().toString());
           //enviamos el fechavencimiento de tarjeta recibids
           editor.putString("mfechavencimiento", mfechavencimiento.getText().toString());
           //enviamos el cvv de tarjeta recibids
           editor.putString("mcodigoseguridad", mcodigoseguridad.getText().toString());
           //aplicamos los cambios
           editor.apply();
           //creamos el intent donde van a recibir esos datos
           Intent intent = new Intent(WalletClient.this, TarjetaAgregadoClient.class);
           //iniciamos el intent
           startActivity(intent);
       });
       //iniciando el view de layoutagregartarjetas con su id
       View layoutagregartarjetas = findViewById(R.id.layoutagregartarjetas);
       //agregamos un onclick sobre el layout
       layoutagregartarjetas.setOnClickListener(v -> {
           //creamos un intent
           Intent intent = new Intent(WalletClient.this, PagoActivity.class);
           //iniciamos el intent
           startActivity(intent);
       });
       //iniciando el TextView de agregartarjeta con su id
       TextView textagregartarjetas = findViewById(R.id.textagregartarjetas);
       //agregamos un onclik sobre el TextView
       textagregartarjetas.setOnClickListener(v -> {
           //creamos un intent
           Intent intent = new Intent(WalletClient.this, PagoActivity.class);
           //iniciamos el intent
           startActivity(intent);
       });

       //iniciando el view de layoutcupones con su id
       View layoutcupones = findViewById(R.id.layoutcupones);
       //agregamos un onclick sobre el layout
       layoutcupones.setOnClickListener(v -> {
           //creamos un intent
           Intent intent = new Intent(WalletClient.this, CuponesDisponibleClient.class);
           //iniciamos el intent
           startActivity(intent);
       });
       //iniciamos el numero de tarjeta con su id
       mmerodetarjeta = findViewById(R.id.tarjetayaagregada);
       //iniciamos el nombre de tarjeta con su id
       mnombretarjeta = findViewById(R.id.nombretarjetadesing);
       //iniciamos el cvv de tarjeta con su id
       mcodigoseguridad = findViewById(R.id.codigoseguridadtarjetadesing);
       //iniciamos la fechavencimiento de tarjeta con su id
       mfechavencimiento = findViewById(R.id.fechavencimientodesing);
       //iniciando el view de layoutpagocongoogle con su id
       layoutpagocongoogle = findViewById(R.id.layoutpagocongoogle);
       //agregamos un onclick sobre el layout
       layoutpagocongoogle.setOnClickListener(v -> {
           //mensaje disponibilidad
           Toast.makeText(WalletClient.this, "Proximamente", Toast.LENGTH_SHORT).show();
           // startActivity(new Intent(WalletClient.this,itempagarcongoogle.class));
       });
       //metdo que recibe los datos de la tarjeta
       recibirdatosdetarjetas();
   }
      //metodo para recibir los datos de la tarjeta
    private void recibirdatosdetarjetas() {
        //recibir los datos de la tarjeta mediante SharedPreferences
        SharedPreferences preferencias = getSharedPreferences("datosTarjetas", Context.MODE_PRIVATE);
        //crear una variable llegotarjeta y lo pasamos el numero de tarjeta que proviene de preferencias
        // y tb lo pasamos un valor default por si no hay en mi caso puse No hay tarjetas igual puedes dejarlo vacio
        String llegotarjeta = preferencias.getString("mnumerotarjeta", "No hay tarjetas");
        //crear una variable llegofechavencimiento y lo pasamos la fechavencimiento de la tarjeta que proviene de preferencias
        // y tb lo pasamos un valor default en mi caso esta vacio
        String llegofechavencimiento = preferencias.getString("mfechavencimiento", "");
        //crear una variable llegocodigoseguridad y lo pasamos el cvv de la tarjeta que proviene de preferencias
        // y tb lo pasamos un valor default en mi caso esta vacio
        String llegocodigoseguridad = preferencias.getString("mcodigoseguridad", "");
        //crear una variable llegonombre y lo pasamos el nombre de la tarjeta que proviene de preferencias
        // y tb lo pasamos un valor default en mi caso esta vacio
        String llegonombre = preferencias.getString("mnombretarjeta", "");
        //invocamos el editor
        SharedPreferences.Editor editor = preferencias.edit();
        //aplicamos el cambio
        editor.apply();
        //enviamos en la vista lo que recibe llegotarjeta
       mmerodetarjeta.setText(llegotarjeta);
        //enviamos en la vista lo que recibe llegofechavencimiento
        mfechavencimiento.setText(llegofechavencimiento);
        //enviamos en la vista lo que recibe llegonombre
        mnombretarjeta.setText(llegonombre);
        //enviamos en la vista lo que recibe llegocodigoseguridad
        mcodigoseguridad.setText(llegocodigoseguridad);


    }
}