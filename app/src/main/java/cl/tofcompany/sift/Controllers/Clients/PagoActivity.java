package cl.tofcompany.sift.Controllers.Clients;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.braintreepayments.cardform.view.CardForm;

import cl.tofcompany.sift.R;
import cl.tofcompany.sift.includes.MyToolbar;

public class PagoActivity extends AppCompatActivity {
    //variable btn agregar
    private Button mbtnagregar;
    //variable de la libreria cardForm
    CardForm cardForm;
    //variable de alert dialog
    AlertDialog.Builder builder;
    //variable para cifrado de numero tarjeta
    String cifrado = "************";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pago);
        //iniciamos en btn agregar con su id
        mbtnagregar = findViewById(R.id.btnagregar);
        //iniciamos en carform con su id
        cardForm = findViewById(R.id.card_form);
        //decimos al carque que activa el card requerido
        cardForm.cardRequired(true)
                // tb necesitamos el campo de nombre
                .cardholderName(CardForm.FIELD_REQUIRED)
                //tb el campo de fecha expiracion
                .expirationRequired(true)
                //tb el campo de cvv
                .cvvRequired(true)
                //despues de ingresar el numero poneme el numero en estrellas
                .maskCardNumber(true)
                //desactive el campo de codigo postal
                .postalCodeRequired(false)
                //tb necesito el campo del numero celular
                .mobileNumberRequired(true)
                //tb eso es un text view
                .mobileNumberExplanation("SMS is required on this number")
                //ese setup me muestra el formulario de cardform en esta actividad this
                .setup(PagoActivity.this);
        //en el verificacion de cvv decimos que el edit text se comporta como un input tipo password
        cardForm.getCvvEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        //activamos nuestra tollbar
        MyToolbar.show(this, "", true);
        //agremamos un evento de on click en el boton de agregar
          mbtnagregar.setOnClickListener(v -> {
              //validamos primero si el formolario es valido isvalid me va a leer que si todos los campos no estan vacio
              //tb va a validad si el numero de tarjeta es correcta osea los datos en general
        if (cardForm.isValid()){
            //creamos un alert con los daros antes de confirmar
            builder = new AlertDialog.Builder(PagoActivity.this);
            //mostrar el titulo
            builder.setTitle("Confirmar antes de agregar");
            //aqui estan los mensajes que mostrarn
            builder.setMessage("Numero de la Tarjeta: " + cardForm.getCardNumber() +"\n"+
                    "Nombre del Titular: " + cardForm.getCardholderName() +"\"+"+
                    "Fecha Exp: " + cardForm.getExpirationDateEditText().getText().toString() + "\" +"+
                    "CVV: " + cardForm.getCvv() + "\" +"+
                    "Codigo Postal: " + cardForm.getPostalCode() + "\" +" +
                    "Numero celular : " + cardForm.getMobileNumber());
            //boton positivo
            builder.setPositiveButton("Confirmar", (dialog, which) -> {
                //ocultar alert
                dialog.dismiss();
                // llamar mi SharedPreferences para enviar los datos de la tarjeta y guardarlo en la memoria del celular
                SharedPreferences preferencias = getSharedPreferences("datosTarjetas", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferencias.edit();
                //enviamos el mnombretarjeta
                editor.putString("mnombretarjeta", cardForm.getCardholderName());
                //enviamos el mnumerotarjeta
                editor.putString("mnumerotarjeta",cifrado+cardForm.getCardNumber().substring(cardForm.getCardNumber().length() -4));
                //enviamos el mfechavencimiento
                editor.putString("mfechavencimiento",cardForm.getExpirationMonth()+ "/"+cardForm.getExpirationYear());
                //enviamos el mcodigoseguridad
                editor.putString("mcodigoseguridad", cardForm.getCvv());
                //aplicamos con un commit
                editor.apply();
                //creamos el intent
                Intent intent = new Intent(PagoActivity.this,WalletClient.class);
                //iniciamos el intent
                startActivity(intent);
                //mensaje de success
                Toast.makeText(PagoActivity.this, "Tu Tarjeta fue Agregado Con Exito", Toast.LENGTH_SHORT).show();
            });
            //boton negativo
            builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
            //creamos el alert
            AlertDialog alertDialog = builder.create();
            //mostramos el alert
            alertDialog.show();
        }else {
          //  Toast.makeText(PagoActivity.this, "Por favor completa el formulario", Toast.LENGTH_SHORT).show();
            //metodo alert de error al guardar tarjeta
            SaveFailedCard();
        }
        });
    }
    //metodo alert de error al guardar tarjeta
    private void SaveFailedCard() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //titulo mensaje
        builder.setTitle("Error!");
        //cuerpo mensaje
        builder.setMessage("Error al guardar su tarjeta");
        //boton positivo
        builder.setPositiveButton("OK",null);
        //creamos el dialog
        AlertDialog dialog = builder.create();
        //mostramos el dialog
        dialog.show();

    }
}