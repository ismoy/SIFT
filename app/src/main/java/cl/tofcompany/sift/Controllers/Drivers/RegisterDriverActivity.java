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

import cl.tofcompany.sift.Controllers.Logins.Login;
import cl.tofcompany.sift.Model.Driver;
import cl.tofcompany.sift.Providers.AuthProvider;
import cl.tofcompany.sift.Providers.DriverProvider;
import cl.tofcompany.sift.R;
import cl.tofcompany.sift.includes.MyToolbar;

public class RegisterDriverActivity extends AppCompatActivity {


    //definamos cada campos del activity con sus nombres
    EditText musername, mEmail, mPassword, mrepetpassword,mvehicleBrand,mvehiclePlate;
    //variable btn registrar
    Button mRegisterBtn;
    //variable progressDialog
    private ProgressDialog mDialog;
    //authprovider viene de la clase AuthProvider
    AuthProvider mAuthProvider;
    //Clientprovider viene de la clase ClientProvider
     DriverProvider mDriverProvider;
     //variable SharedPreferences
    SharedPreferences mPreferences;
    //variable FirebaseAuth
    FirebaseAuth fAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_driver);
       init();
    }
    private void init(){
        //asignamos el campo  name con su respectivo id
        musername = findViewById(R.id.username);
        //asignamos el campo  email con  su respectivo id
        mEmail = findViewById(R.id.correo);
        //asignamos el campo  password con  su respectivo id
        mPassword = findViewById(R.id.password);
        //asignamos el campo  password2 con  su respectivo id
        mrepetpassword = findViewById(R.id.repeatpassword);
        //asignamos el btn registrar con  su respectivo id
        mRegisterBtn = findViewById(R.id.btn_registrar);
        //asignamos el campo  marca con  su respectivo id
        mvehicleBrand = findViewById(R.id.marcaauto);
        //asignamos el campo  patente con  su respectivo id
        mvehiclePlate = findViewById(R.id.placaauto);
        //iniciamos el dialog
        mDialog = new ProgressDialog(this);
        //iniciamos la clase AuthProvider
        mAuthProvider =  new AuthProvider();
        //iniciamos la clase DriverProvider
        mDriverProvider = new DriverProvider();
        //recibiendo los datos que trae el SharedPreferences
        mPreferences = getApplication().getSharedPreferences("typeUser",MODE_PRIVATE);
        //asignamos el metodo getIntance de firebase
        fAuth = FirebaseAuth.getInstance();
        String TAG;
        //llamado de mi toolbar
        MyToolbar.show(this,"REGISTER",true);
        //agregamos el evento onclick en el boton registrar
        mRegisterBtn.setOnClickListener(v ->
                //metodo para registrar
                clickRegister());


    }
    //metodo para registrar
    private void clickRegister() {
        //definir un variable name para guardar los que trae el campo username
        final String name = musername.getText().toString().trim();
        //definir un variable email para guardar los que trae el campo correo
        final String email = mEmail.getText().toString().trim();
        //definir un variable password para guardar los que trae el campo password
        final String password = mPassword.getText().toString().trim();
        //definir un variable password2 para guardar los que trae el campo password2
        final String password2 = mrepetpassword.getText().toString().trim();
        //definir un variable vehiculeBrand para guardar los que trae el campo marca
        final String vehicleBrand = mvehicleBrand.getText().toString().trim();
        //definir un variable vehiclePlate para guardar los que trae el campo patente
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
    //metodo registrar
    void register(final String name,final String email,String password,String password2,String vehicleBrand,String vehiclePlate) {
        //entramos en lacse AuthProvider ejecutamos el metodo register recibe email y password
        //y luego agregamos el evento addOnCompleteListener
        mAuthProvider.register(email , password).addOnCompleteListener(task -> {
            //hide el dialog
            mDialog.dismiss();
            //aseguramos que el processo sea exitoso y enviamos mensaje al usuario al contrario enviamos error
            if (task.isSuccessful()) {
                //creamos un variable id para sacar el id en firebase
                String id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                //llamando el metodo driver y lo pasamos el id ,el nombre,el email, y los password
                Driver driver = new Driver(id,name,email,password,password2,vehicleBrand,vehiclePlate);
                //aqui registramos el cliente si todos es ok
                create(driver);
                //aqui estamos tomando el usuario que esta registrando con el metodo de firebase getCurrentUser
                    FirebaseUser user = fAuth.getCurrentUser();
                //definemos el idioma cuando vamos a enviar el correo de verificacion en mi caso es español
                    fAuth.setLanguageCode("es");
                //aseguremos que es usuario no esta vacio
                    assert user != null;
                // si la tarrea fue exitosa enviamos un correo de confirmation al usuario para verificar su cuenta
                    user.sendEmailVerification().addOnCompleteListener(task1 -> Toast.makeText(RegisterDriverActivity.this , R.string.Youraccounthasbeencreatedsuccessfully_Checkyouremailtoactivatetheaccount , Toast.LENGTH_LONG).show()).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            String TAG = "";
                            Log.d(TAG , R.string.onFailureWecouldnotverifyyouremail + e.getMessage());
                        }

                    });

            } else {
                //mensaje en case de error
                Toast.makeText(RegisterDriverActivity.this , R.string.Error + Objects.requireNonNull(task.getException()).getMessage() , Toast.LENGTH_LONG).show();
            }
        });
    }  //metodo para crear los drivers en firebase
    void create(Driver driver){
        //llamamos la clase DriverProvider ejecutemos el metodo create eso recibe el driver del modelo y lo pasamos un evento
        mDriverProvider.create(driver).addOnCompleteListener(task -> {
            //aseguramos que la tarea fue exitosa
            if (task.isSuccessful()){
                //creamos un variable user para recibir el usuario que esta registrando
                String user = mPreferences.getString("user","");
                //aseguramos si el usuario es un driver
                if (user.equals("client")){
                    //en caso es un driver creamos un intent
                    Intent intent = new Intent(RegisterDriverActivity.this, Login.class);
                    //esos es para no poder volver al actividad de registro despues de salir
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    //iniciamos el intent
                    startActivity(intent);
                }else{
                    //en caso es un conductor lo enviamos en esos intent
                    Intent intent = new Intent(RegisterDriverActivity.this, Login.class);
                    //esos es para no poder volver al actividad de registro despues de salir
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    //iniciamos el intent
                    startActivity(intent);
                }
            }else {
                //mensaje de error
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