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
    private TextView mnmerodetarjeta;
    private TextView mfechavencimiento;
    private TextView mcodigoseguridad;
    private TextView mnombretarjeta;
    private View layoutpagocongoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_client);
        MyToolbar.show(this, "", true);
        View layoutefectivo = findViewById(R.id.layoutefectivo);
        layoutefectivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WalletClient.this, PagoEnEfectivoClient.class);
                startActivity(intent);
            }
        });
        View layoutvisaclient = findViewById(R.id.layoutvisaclient);
        layoutvisaclient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferencias = getSharedPreferences("datosTarjetas", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferencias.edit();
                editor.putString("mnumerotarjeta", mnmerodetarjeta.getText().toString());
                editor.putString("mnombretarjeta", mnombretarjeta.getText().toString());
                editor.putString("mfechavencimiento", mfechavencimiento.getText().toString());
                editor.putString("mcodigoseguridad", mcodigoseguridad.getText().toString());
                editor.commit();
                Intent intent = new Intent(WalletClient.this, TarjetaAgregadoClient.class);
                startActivity(intent);
            }
        });
        View layoutagregartarjetas = findViewById(R.id.layoutagregartarjetas);
        layoutagregartarjetas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WalletClient.this, PagoActivity.class);
                startActivity(intent);
            }
        });
        TextView textagregartarjetas = findViewById(R.id.textagregartarjetas);
        textagregartarjetas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WalletClient.this, PagoActivity.class);
                startActivity(intent);
            }
        });


        View layoutcupones = findViewById(R.id.layoutcupones);
        layoutcupones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WalletClient.this, CuponesDisponibleClient.class);
                startActivity(intent);
            }
        });
        mnmerodetarjeta = findViewById(R.id.tarjetayaagregada);
        mnombretarjeta = findViewById(R.id.nombretarjetadesing);
        mcodigoseguridad = findViewById(R.id.codigoseguridadtarjetadesing);
        mfechavencimiento = findViewById(R.id.fechavencimientodesing);
        recibirdatosdetarjetas();
        init();
    }

    private void init() {
        layoutpagocongoogle = findViewById(R.id.layoutpagocongoogle);
        layoutpagocongoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               Toast.makeText(WalletClient.this, "Proximamente", Toast.LENGTH_SHORT).show();
                // startActivity(new Intent(WalletClient.this,itempagarcongoogle.class));
            }
        });
    }

    private void recibirdatosdetarjetas() {
        SharedPreferences preferencias = getSharedPreferences("datosTarjetas", Context.MODE_PRIVATE);
        String llegotarjeta = preferencias.getString("mnumerotarjeta", "No hay tarjetas");
        String llegofechavencimiento = preferencias.getString("mfechavencimiento", "");
        String llegocodigoseguridad = preferencias.getString("mcodigoseguridad", "");
        String llegonombre = preferencias.getString("mnombretarjeta", "");
        SharedPreferences.Editor editor = preferencias.edit();
        editor.apply();
        mnmerodetarjeta.setText(llegotarjeta);
        mfechavencimiento.setText(llegofechavencimiento);
        mnombretarjeta.setText(llegonombre);
        mcodigoseguridad.setText(llegocodigoseguridad);


    }
}