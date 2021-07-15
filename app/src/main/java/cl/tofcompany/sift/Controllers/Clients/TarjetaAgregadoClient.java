package cl.tofcompany.sift.Controllers.Clients;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import cl.tofcompany.sift.R;
import cl.tofcompany.sift.includes.MyToolbar;

public class TarjetaAgregadoClient extends AppCompatActivity {
private TextView mnumerotarjeta;
private TextView mfechavencimiento;
private TextView mcodigoseguridad;
private TextView mnombretarjeta;
private Button meliminartarjeta;
private Button mactualizartarjeta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tarjeta_agregado_client);
        MyToolbar.show(this,"",true);
        mnumerotarjeta = findViewById(R.id.numerotarjeta);
        mfechavencimiento = findViewById(R.id.fechavencimiento);
        mcodigoseguridad = findViewById(R.id.codigoseguridad);
        mnombretarjeta = findViewById(R.id.nombretarjeta);
        meliminartarjeta = findViewById(R.id.eliminartarjeta);
        mactualizartarjeta = findViewById(R.id.actualizartarjeta);
        meliminartarjeta.setOnClickListener(v -> {
            SharedPreferences preferencias=getSharedPreferences("datosTarjetas", Context.MODE_PRIVATE);
            String typeTajeta = preferencias.getString("mnumerotarjeta","");
            if (typeTajeta.equals("No hay tarjetas")){
                MensajeAlert();
              //  Toast.makeText(TarjetaAgregadoClient.this, "No tienes tarjeta agregado", Toast.LENGTH_SHORT).show();
            }else {
                AlertDelete();
            }
        });
        recibirdatos();
        mactualizartarjeta.setOnClickListener(v -> {
            MessageAlertActualizar();
        });
    }

    private void MensajeAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Eliminar Tarjeta");
        builder.setMessage("No tienes tarjeta agregada no se puede Eliminar ");
        builder.setPositiveButton("OK", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void MessageAlertActualizar() {
        SharedPreferences preferencias = getSharedPreferences("datosTarjetas", Context.MODE_PRIVATE);
        String typeTajeta = preferencias.getString("mnumerotarjeta", "");
        if (typeTajeta.equals("No hay tarjetas")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Actualizar Tarjeta");
            builder.setMessage("No tienes tarjeta agregada no se puede Actualizar ");
            builder.setPositiveButton("OK", null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }else {
            Intent intent = new Intent(TarjetaAgregadoClient.this,PagoActivity.class);
            startActivity(intent);
        }
    }

    private void recibirdatos() {
        SharedPreferences preferencias=getSharedPreferences("datosTarjetas", Context.MODE_PRIVATE);
        String llegotarjeta = preferencias.getString("mnumerotarjeta","");
        String llegofechavencimiento = preferencias.getString("mfechavencimiento","");
        String llegocodigoseguridad = preferencias.getString("mcodigoseguridad","");
        String llegonombre = preferencias.getString("mnombretarjeta","");
        SharedPreferences.Editor editor=preferencias.edit();
        editor.apply();
        mnumerotarjeta.setText(llegotarjeta);
        mfechavencimiento.setText(llegofechavencimiento);
        mcodigoseguridad.setText(llegocodigoseguridad);
        mnombretarjeta.setText(llegonombre);

    }

    //metodo para eliminar la tarjeta con alert
    public void AlertDelete() {
        AlertDialog.Builder alertDialog2 = new
                AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog2.setTitle("estas sefuro de eliminar la tarjeta?");

        // Setting Dialog Message
        alertDialog2.setMessage("Al aceptar se eliminara su tarjeta como metodo de pago ");

        // Setting Positive "Yes" Btn
        alertDialog2.setPositiveButton(R.string.yes,
                (dialog , which) -> {
                    // Write your code here to execute after dialog
                    eliminarTarjeta();
                });

        // Setting Negative "NO" Btn
        alertDialog2.setNegativeButton(R.string.no ,
                (dialog , which) -> {
                    // Write your code here to execute after dialog
                    Toast.makeText(getApplicationContext() ,
                            R.string.noaction, Toast.LENGTH_SHORT)
                            .show();
                    dialog.cancel();
                    startActivity(new Intent(getApplicationContext(),TarjetaAgregadoClient.class));
                });

        // Showing Alert Dialog
        alertDialog2.show();


    }
    private void eliminarTarjeta() {
        SharedPreferences preferencias=getSharedPreferences("datosTarjetas", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferencias.edit();
        editor.remove("mnumerotarjeta");
        editor.remove("mfechavencimiento");
        editor.remove("mcodigoseguridad");
        editor.remove("mnombretarjeta");
        editor.apply();
        editor.clear();
        Message();
        startActivity(new Intent(getApplicationContext(),TarjetaAgregadoClient.class));
    }
    private void Message() {
        Toast.makeText(this, "Tarjeta Eliminada", Toast.LENGTH_SHORT).show();
    }
}