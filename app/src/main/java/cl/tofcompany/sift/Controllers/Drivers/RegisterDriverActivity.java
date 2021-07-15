package cl.tofcompany.sift.Controllers.Drivers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;
import java.util.regex.Pattern;

import cl.tofcompany.sift.Controllers.Login;
import cl.tofcompany.sift.Model.Driver;
import cl.tofcompany.sift.Providers.AuthProvider;
import cl.tofcompany.sift.Providers.DriverProvider;
import cl.tofcompany.sift.R;
import cl.tofcompany.sift.includes.MyToolbar;

public class RegisterDriverActivity extends AppCompatActivity {


    //definamos cada campos del activity con sus nombres
    EditText musername, mEmail, mPassword, mrepetpassword,mvehicleBrand,mvehiclePlate;
    Button mRegisterBtn;
    private ProgressDialog mDialog;
    //authprovider viene de la clase AuthProvider
    AuthProvider mAuthProvider;
    //Clientprovider viene de la clase ClientProvider
     DriverProvider mDriverProvider;
    SharedPreferences mPreferences;
    FirebaseAuth fAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_driver);
        //asignamos cada campos con sus respectivos id
        musername = findViewById(R.id.username);
        mEmail = findViewById(R.id.correo);
        mPassword = findViewById(R.id.password);
        mrepetpassword = findViewById(R.id.repeatpassword);
        mRegisterBtn = findViewById(R.id.btn_registrar);
        mvehicleBrand = findViewById(R.id.marcaauto);
        mvehiclePlate = findViewById(R.id.placaauto);
        mDialog = new ProgressDialog(this);
        mAuthProvider =  new AuthProvider();
        mDriverProvider = new DriverProvider();
        mPreferences = getApplication().getSharedPreferences("typeUser",MODE_PRIVATE);
        fAuth = FirebaseAuth.getInstance();
         String TAG;
        MyToolbar.show(this,"REGISTER",true);
        mRegisterBtn.setOnClickListener(v -> clickRegister());


    }

    private void clickRegister() {
        //definemos los variables de cada campo para poder validarlos
        final String name = musername.getText().toString().trim();
        final String email = mEmail.getText().toString().trim();
        final String password = mPassword.getText().toString().trim();
        final String password2 = mrepetpassword.getText().toString().trim();
        final String vehicleBrand = mvehicleBrand.getText().toString().trim();
        final String vehiclePlate = mvehiclePlate.getText().toString().trim();
        //Validamos el campo nombre para que no esta vacio
        if (TextUtils.isEmpty(name)) {
            musername.setError(getString(R.string.Requiredfield));
            return;
        }
        //Validamos que solo pueden igresar letra
        if (!validarletras(name)) {
            musername.setError(getString(R.string.Onlyallowletters));
            return;
        }
        //Validamos que el campo nombre debe tener minimum 6 caracteres
        if (name.length() <= 6) {
            musername.setError(getString(R.string.Pleaseenteryourfullname));
            return;
        }
        //Validamos el campo email para que no esta vacio
        if (TextUtils.isEmpty(email)) {
            mEmail.setError(getString(R.string.Email_is_required_));
            return;
        }
        //Validamos el campo vehicleBrand para que no esta vacio
        if (TextUtils.isEmpty(vehicleBrand)) {
            mvehicleBrand.setError(getString(R.string.vehicleBrand));
            return;
        }
        //Validamos el campo vehicleBrand para que no esta vacio
        if (TextUtils.isEmpty(vehiclePlate)) {
            mvehiclePlate.setError(getString(R.string.vehiclePlate));
            return;
        }
        if (!validaremail(email)) {
            mEmail.setError(getString(R.string.invalid_email));
            return;
        }
        //Validamos el campo password para que no esta vacio
        if (TextUtils.isEmpty(password)) {
            mPassword.setError(getString(R.string.Password_is_required_));
            return;
        }
        //Validamos el campo confirmar password para que no esta vacio
        if (TextUtils.isEmpty(password2)) {
            mrepetpassword.setError(getString(R.string.Password_is_required_));
            return;
        }
        //Validamos el campo password para que la contraseña sea minimum 6 caracteres
        if (password.length() < 6) {
            mPassword.setError(getString(R.string.The_password_must_be_at_least_6_characters_long));
            return;
        }
        //Validamos que el campo confirmar contraseña sea igual al contraseña ingresado anteriormente
        if (password.equals(password2)) {
            mDialog.setMessage(getString(R.string.Loading___));
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();

            //despues de validar los campos enviamos los datos en la realtime database de una vez
            register(name,email,password,password2,vehicleBrand,vehiclePlate);
        } else {
            mrepetpassword.setError(getString(R.string.Passwordsdonotmatch));
        }
       /* Intent i = new Intent(this , MainActivity.class);
        startActivity(i);*/
    }

    void register(final String name,final String email,String password,String password2,String vehicleBrand,String vehiclePlate) {
        mAuthProvider.register(email , password).addOnCompleteListener(task -> {
            mDialog.dismiss();
            //aseguramos que el processo sea exitoso y enviamos mensaje al usuario al contrario enviamos error
            if (task.isSuccessful()) {

                String id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                Driver driver = new Driver(id,name,email,password,password2,vehicleBrand,vehiclePlate);
                create(driver);
                    // si la tarrea fue exitosa enviamos un correo de confirmation al usuario para verificar su cuenta
                    FirebaseUser user = fAuth.getCurrentUser();
                    fAuth.setLanguageCode("es");
                    assert user != null;
                    user.sendEmailVerification().addOnCompleteListener(task1 -> Toast.makeText(RegisterDriverActivity.this , R.string.Youraccounthasbeencreatedsuccessfully_Checkyouremailtoactivatetheaccount , Toast.LENGTH_LONG).show()).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            String TAG = "";
                            Log.d(TAG , R.string.onFailureWecouldnotverifyyouremail + e.getMessage());
                        }

                    });

            } else {
                Toast.makeText(RegisterDriverActivity.this , R.string.Error + Objects.requireNonNull(task.getException()).getMessage() , Toast.LENGTH_LONG).show();
            }
        });
    }
    void create(Driver driver){
        mDriverProvider.create(driver).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                String user = mPreferences.getString("user","");
                if (user.equals("client")){
                    Intent intent = new Intent(RegisterDriverActivity.this, Login.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(RegisterDriverActivity.this, Login.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }else {
                Toast.makeText(RegisterDriverActivity.this, R.string.Error + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    //Validacion regex para los campos
    public static boolean validarletras(String datos) {
        return datos.matches("[a-zA-Z ]*");
    }

    //Validacion Patterns para el campo email
    private boolean validaremail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }
}