package cl.tofcompany.sift.Controllers.Clients;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import cl.tofcompany.sift.R;
import cl.tofcompany.sift.includes.MyToolbar;

public class TarjetaAgregadoClient extends AppCompatActivity {
    //variable textView numerotarjeta
private TextView mnumerotarjeta;
    //variable textView fechavencimiento
private TextView mfechavencimiento;
    //variable textView cvv
private TextView mcodigoseguridad;
    //variable textView nombretarjeta
private TextView mnombretarjeta;
    //variable btn eliminartarjeta
private Button meliminartarjeta;
    //variable btn actualizartarjeta
private Button mactualizartarjeta;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tarjeta_agregado_client);
       init();

    }

    //las inicializaciones de los variables estan aqui
    private void init(){
        //llamamos mi toolbar
        MyToolbar.show(this,"",true);
        //iniciamos mnumerotarjeta con su id
        mnumerotarjeta = findViewById(R.id.numerotarjeta);
        //iniciamos mfechavencimiento con su id
        mfechavencimiento = findViewById(R.id.fechavencimiento);
        //iniciamos mcodigoseguridad con su id
        mcodigoseguridad = findViewById(R.id.codigoseguridad);
        //iniciamos mnombretarjeta con su id
        mnombretarjeta = findViewById(R.id.nombretarjeta);
        //iniciamos meliminartarjeta con su id
        meliminartarjeta = findViewById(R.id.eliminartarjeta);
        //iniciamos mactualizartarjeta con su id
        mactualizartarjeta = findViewById(R.id.actualizartarjeta);
        //agregamos el evento onclick en el boton eliminar
        meliminartarjeta.setOnClickListener(v -> {
            //recibiendo los datos ya guardados en SharedPreferences
            SharedPreferences preferencias=getSharedPreferences("datosTarjetas", Context.MODE_PRIVATE);
            //creamos un variable typeTajeta y lo pasamos los que viene en preferencias
            String typeTajeta = preferencias.getString("mnumerotarjeta","");
            //saber si hay tarjeta agregada o no
            if (typeTajeta.equals("No hay tarjetas")){
                //mensaje alert
                MensajeAlert();
                //  Toast.makeText(TarjetaAgregadoClient.this, "No tienes tarjeta agregado", Toast.LENGTH_SHORT).show();
            }else {
                //mensaje alert
                AlertDelete();
            }
        });
        //los datos recibidos estan en ese metodo
        recibirdatos();
        //agregamos el evento onclick en el boton actualizar
        mactualizartarjeta.setOnClickListener(v -> {
            //alert mensaje
            MessageAlertActualizar();

        });
    }
    private void MensajeAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //titulo
        builder.setTitle("Eliminar Tarjeta");
        //cuerpo
        builder.setMessage("No tienes tarjeta agregada no se puede Eliminar ");
        //boton postivo
        builder.setPositiveButton("OK", null);
        //creamos el dialog
        AlertDialog dialog = builder.create();
        //mostramos el dialog
        dialog.show();
    }
    //metodo para alert actualizar tarjeta
    private void MessageAlertActualizar() {
        //recibir los datos de la tarjeta
        SharedPreferences preferencias = getSharedPreferences("datosTarjetas", Context.MODE_PRIVATE);
        //creamos un variable typeTajeta lo pasamos lo que proviene de preferencias
        String typeTajeta = preferencias.getString("mnumerotarjeta", "");
        //saber si ya hay tarjeta agregada o no
        if (typeTajeta.equals("No hay tarjetas")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            //titlulo
            builder.setTitle("Actualizar Tarjeta");
            //cuerpo
            builder.setMessage("No tienes tarjeta agregada no se puede Actualizar ");
            //positive buton
            builder.setPositiveButton("OK", null);
            //crear el dialog
            AlertDialog dialog = builder.create();
            //mostrar el dialog
            dialog.show();
        }else {
            //en caso que ya tiene tarjeta agregada creamos un intent
            Intent intent = new Intent(TarjetaAgregadoClient.this,PagoActivity.class);
            //iniciamos el intent
            startActivity(intent);
        }
    }
     //metodo para recibir los datos de la tarjeta
    private void recibirdatos() {
        //recibir los datos de la tarjeta
        SharedPreferences preferencias=getSharedPreferences("datosTarjetas", Context.MODE_PRIVATE);
        //crear una variable llegotarjeta para guardar lo que proviene de preferencias
        String llegotarjeta = preferencias.getString("mnumerotarjeta","");
        //crear una variable llegofechavencimiento para guardar lo que proviene de preferencias
        String llegofechavencimiento = preferencias.getString("mfechavencimiento","");
        //crear una variable llegocodigoseguridad para guardar lo que proviene de preferencias
        String llegocodigoseguridad = preferencias.getString("mcodigoseguridad","");
        //crear una variable llegonombre para guardar lo que proviene de preferencias
        String llegonombre = preferencias.getString("mnombretarjeta","");
        //llamamos el edito de SharedPreferences
        SharedPreferences.Editor editor=preferencias.edit();
        //aplicamos los cambios
        editor.apply();
        //enviamos en la vista el llegotarjeta
        mnumerotarjeta.setText(llegotarjeta);
        Log.d("NUMBERS ","numero mi tarjeta  " +llegotarjeta);
        //enviamos en la vista el llegofechavencimiento
        mfechavencimiento.setText(llegofechavencimiento);
        //enviamos en la vista el llegocodigoseguridad
        mcodigoseguridad.setText(llegocodigoseguridad);
        //enviamos en la vista el llegonombre
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
    //metodo para eliminar la tarjeta
    private void eliminarTarjeta() {
        //recibir los datos de SharedPreferences
        SharedPreferences preferencias=getSharedPreferences("datosTarjetas", Context.MODE_PRIVATE);
        //invocamos el editor
        SharedPreferences.Editor editor=preferencias.edit();
        //Aviso cuando eliminamos aca eliminamos los datos de la tarjeta completo de todos lados
        //si se fijan estoy eliminando y el parametro esta entre comillas son exactamante
        //lo mismo que estan en la actividad de PagoActivity cuando lo estabamos enviandolo
        //asi que pila en eso
        //es mejor saber mas sobre la funcionalidad de  SharedPreferences
        //eliminamos mnumerotarjeta
        editor.remove("mnumerotarjeta");
        //eliminamos mfechavencimiento
        editor.remove("mfechavencimiento");
        //eliminamos mcodigoseguridad
        editor.remove("mcodigoseguridad");
        //eliminamos mnombretarjeta
        editor.remove("mnombretarjeta");
        //aplicamos cambios
        editor.apply();
        //limpiamos todas la tareas
        editor.clear();
        //enviar un mensaje que se elimino
        Message();
        //actualizamos la actividad
        startActivity(new Intent(getApplicationContext(),TarjetaAgregadoClient.class));
    }
    //metodo para el mensaje de eliminacion
    private void Message() {
        Toast.makeText(this, "Tarjeta Eliminada", Toast.LENGTH_SHORT).show();
    }
}