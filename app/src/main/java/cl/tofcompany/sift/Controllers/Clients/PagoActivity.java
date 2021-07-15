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
    private Button mbtnagregar;
    CardForm cardForm;
    AlertDialog.Builder builder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pago);
        mbtnagregar = findViewById(R.id.btnagregar);
        cardForm = findViewById(R.id.card_form);
        cardForm.cardRequired(true)
                .cardholderName(CardForm.FIELD_REQUIRED)
                .expirationRequired(true)
                .cvvRequired(true)
                .maskCardNumber(true)
                .postalCodeRequired(false)
                .mobileNumberRequired(true)
                .mobileNumberExplanation("SMS is required on this number")
                .setup(PagoActivity.this);
        cardForm.getCvvEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        MyToolbar.show(this, "", true);
          mbtnagregar.setOnClickListener(v -> {
        if (cardForm.isValid()){
            builder = new AlertDialog.Builder(PagoActivity.this);
            builder.setTitle("Confirmar antes de agregar");
            builder.setMessage("Numero de la Tarjeta: " + cardForm.getCardNumber() +"\n"+
                    "Nombre del Titular: " + cardForm.getCardholderName() +"\"+"+
                    "Fecha Exp: " + cardForm.getExpirationDateEditText().getText().toString() + "\" +"+
                    "CVV: " + cardForm.getCvv() + "\" +"+
                    "Codigo Postal: " + cardForm.getPostalCode() + "\" +" +
                    "Numero celular : " + cardForm.getMobileNumber());
            builder.setPositiveButton("Confirmar", (dialog, which) -> {
                dialog.dismiss();
                SharedPreferences preferencias = getSharedPreferences("datosTarjetas", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferencias.edit();
                editor.putString("mnombretarjeta", cardForm.getCardholderName());
                editor.putString("mnumerotarjeta",cardForm.getCardNumber());
                editor.putString("mfechavencimiento",cardForm.getExpirationMonth()+ "/"+cardForm.getExpirationYear());
                editor.putString("mcodigoseguridad", cardForm.getCvv());
                editor.apply();
                Intent intent = new Intent(PagoActivity.this,WalletClient.class);
                startActivity(intent);
                Toast.makeText(PagoActivity.this, "Tu Tarjeta fue Agregado Con Exito", Toast.LENGTH_SHORT).show();
            });
            builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }else {
          //  Toast.makeText(PagoActivity.this, "Por favor completa el formulario", Toast.LENGTH_SHORT).show();
            SaveFailedCard();
        }
        });
    }
    private void SaveFailedCard() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error!");
        builder.setMessage("Error al guardar su tarjeta");
        builder.setPositiveButton("OK",null);
        AlertDialog dialog = builder.create();
        dialog.show();

    }
}