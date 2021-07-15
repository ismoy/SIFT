package cl.tofcompany.sift.Controllers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import cl.tofcompany.sift.R;


public class ForgotPassword extends AppCompatActivity {

    //email text
    private EditText mEmail;
    //resetpassword imv
    private ImageView mresetpassword;
    //variables
    private String email;
    private FirebaseAuth mAuth;
    private ProgressDialog mDialog;
    private ImageView mcancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        //firebaseAuth
        mAuth = FirebaseAuth.getInstance();
        //progres dialog
        mDialog = new ProgressDialog(this);
        //email text
        mEmail = findViewById(R.id.correo);
        //mresetpassword imv
        mresetpassword = findViewById(R.id.btnrecover);
        //btncancel
        mcancel = findViewById(R.id.cancel);

        mresetpassword.setOnClickListener(v -> {
            //metodo para el accion del boton reset esta abajo
            ActionResetPassword();
        });

        mcancel.setOnClickListener(v -> {
            Intent i = new Intent(ForgotPassword.this , MainActivity.class);
            startActivity(i);
        });

    }

    public void resetPassword() {
        //codigo para enviar el mensaje en español al cliente
        mAuth.setLanguageCode("es");
        //aqui enviamos el correo de restablecer contraseña
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ForgotPassword.this , R.string.Anemailhasbeensenttoresetyourpasswordcheckyourinbox , Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ForgotPassword.this , R.string.Theresetpasswordemailcouldnotbesent , Toast.LENGTH_LONG).show();
            }
            mDialog.dismiss();
        });
    }

    //metodo para action resetpassword
    public void ActionResetPassword() {
        email = mEmail.getText().toString();
        if (!email.isEmpty()) {
            mDialog.setMessage(getString(R.string.Loading___));
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();
            resetPassword();
        } else {
            mEmail.setError(getString(R.string.Pleaseenteryouremail));
        }

    }

}